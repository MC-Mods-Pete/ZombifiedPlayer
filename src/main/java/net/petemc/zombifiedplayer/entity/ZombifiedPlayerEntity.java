package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.AccessoriesUtil;
import net.petemc.zombifiedplayer.util.TrinketsUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


public class ZombifiedPlayerEntity extends Zombie {
    /** Extra loot ("pocket finds") of zombified players that do not come from a player death, adjustable by datapack. */
    public static final ResourceLocation POCKET_LOOT_TABLE = new ResourceLocation("zombifiedplayer", "entities/zombified_player_pocket");

    private static final String FROM_PLAYER_DEATH_KEY = "FromPlayerDeath";

    public GameProfile gameProfile;

    /**
     * True for zombified players created by a player death. Only they keep the special rules for the player's items:
     * everything drops intact, no despawning in peaceful, no pocket loot. Natural spawns, spawn eggs, spawners and
     * /summon without NBT behave like zombies (finalizeSpawn sets false).
     */
    private boolean fromPlayerDeath = false;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);
    public final List<ItemStack> trinketsItems = new ArrayList<>();
    public final List<ItemStack> accessoriesItems = new ArrayList<>();

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level world) {
        super(entityType, world);
        // Covers every way of creating the entity (death, /summon, spawn egg, spawner)
        this.setCanBreakDoors(true);
    }

    /**
     * Registered once at mod loading: always the normal values. The config option makeTheZombifiedPlayersStronger
     * is applied per entity in applyConfig(), so a change in the config screen applies without a restart.
     */
    public static AttributeSupplier.Builder createZombifiedPlayerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.23f)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    /**
     * Door breaking on hard difficulty like vanilla zombies, with zombifiedPlayersCanAlwaysBreakDoors on every difficulty.
     * The goal itself still needs the game rule mobGriefing.
     */
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, ZombifiedPlayerEntity::canBreakDoorsOnDifficulty);
    private boolean breakDoorsGoalAdded = false;

    /**
     * Applies the config options that belong to the entity. Called when it spawns and when it is loaded,
     * so changed options also apply to zombified players that already exist.
     */
    private void applyConfig() {
        boolean stronger = MainConfig.getMakeTheZombifiedPlayersStronger();
        setBaseValue(Attributes.MAX_HEALTH, stronger ? 40.0 : 20.0);
        setBaseValue(Attributes.FOLLOW_RANGE, stronger ? 50.0 : 40.0);
        setBaseValue(Attributes.MOVEMENT_SPEED, stronger ? 0.29f : 0.23f);
        setBaseValue(Attributes.ATTACK_DAMAGE, stronger ? 4.0 : 2.0);
        setBaseValue(Attributes.ARMOR, stronger ? 4.0 : 2.0);
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    private static boolean canBreakDoorsOnDifficulty(Difficulty difficulty) {
        return difficulty == Difficulty.HARD || MainConfig.getZombifiedPlayersCanAlwaysBreakDoors();
    }

    private void setBaseValue(Attribute attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, (double)1.0F, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, (double)1.0F, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, (double)1.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
    }

    @Override
    public float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        if (MainConfig.getUseCustomEyeHeight()) {
            return MainConfig.getCustomEyeHeight();
        }
        return super.getStandingEyeHeight(pose, dimensions);
    }

    @Override
    public boolean fireImmune() {
        return MainConfig.getMakeTheZombifiedPlayersImmuneToFire();
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    /** Whether door breaking is possible right now (difficulty and config, both can change at any time). */
    @Override
    public boolean canBreakDoors()
    {
        return this.breakDoorsGoalAdded && canBreakDoorsOnDifficulty(this.level().getDifficulty());
    }

    /**
     * Replaces the vanilla zombie logic. Vanilla calls this with a random value on spawn (finalizeSpawn, e.g. /summon)
     * and with the saved value on load. For zombified players only difficulty and config decide,
     * so the value is ignored and our goal is always present.
     */
    @Override
    public void setCanBreakDoors(boolean ignored) {
        if (!this.breakDoorsGoalAdded && GoalUtils.hasGroundPathNavigation(this)) {
            this.goalSelector.addGoal(1, this.breakDoorsGoal);
            this.breakDoorsGoalAdded = true;
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        // Only plan paths through doors while they can be broken, otherwise walk around them like a normal zombie
        if (this.breakDoorsGoalAdded) {
            ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(this.canBreakDoors());
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue(0.0F);
    }

    /** Profile of the dead player, synced to the clients for the skin (also for zombified players already in the world). */
    private static final EntityDataAccessor<Optional<UUID>> DATA_PROFILE_ID =
            SynchedEntityData.defineId(ZombifiedPlayerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> DATA_PROFILE_NAME =
            SynchedEntityData.defineId(ZombifiedPlayerEntity.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PROFILE_ID, Optional.empty());
        this.entityData.define(DATA_PROFILE_NAME, "");
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide() && (DATA_PROFILE_ID.equals(key) || DATA_PROFILE_NAME.equals(key))) {
            // without a profile (e.g. /summon without NBT) the renderer falls back to the default skin
            this.gameProfile = this.entityData.get(DATA_PROFILE_ID)
                    .map(id -> new GameProfile(id, this.entityData.get(DATA_PROFILE_NAME)))
                    .orElse(null);
        }
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROFILE_NAME, gameProfile == null || gameProfile.getName() == null ? "" : gameProfile.getName());
            this.entityData.set(DATA_PROFILE_ID, gameProfile == null ? Optional.empty() : Optional.ofNullable(gameProfile.getId()));
        }
    }
    /**
     * Called for natural spawns, /summon without NBT, spawn eggs and spawners, not for zombified players from a player death.
     * Without a profile, one of the configured player names is picked so it gets that player's skin.
     */
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        // /summon and spawners load the (otherwise empty) NBT first, which counts as an old save without the marker
        this.fromPlayerDeath = false;
        applyConfig();
        this.setHealth(this.getMaxHealth());
        // Vanilla zombie: random equipment by local difficulty, can pick up loot, leader bonus etc.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        if (this.gameProfile == null) {
            boolean natural = reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION;
            assignRandomSkinProfile(!natural || MainConfig.getDisplayNameTagForNaturalZombifiedPlayers());
        }
        return result;
    }

    public boolean isFromPlayerDeath() {
        return fromPlayerDeath;
    }

    private void assignRandomSkinProfile(boolean nameTag) {
        List<String> names = MainConfig.getRandomSkinPlayerNames();
        if (names.isEmpty()) {
            return;
        }
        String name = names.get(this.random.nextInt(names.size()));
        // Offline UUID: the same name always gets the same UUID, the client caches the skin per UUID and looks it up by name
        setGameProfile(new GameProfile(UUIDUtil.createOfflinePlayerUUID(name), name));
        if (nameTag && MainConfig.getDisplayNameTagForZombifiedPlayer()) {
            setCustomName(Component.literal("Zombified " + name));
        }
    }

    /** Zombified players are always adults (vanilla makes some spawned zombies babies). */
    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    /**
     * From a player death never in peaceful: the zombified player carries the dead player's items,
     * despawning would delete them. It cannot attack players in peaceful anyway. Others like zombies.
     */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return !this.fromPlayerDeath;
    }

    /**
     * From a player death vanilla must never drop the equipment by itself: it only does so by chance and
     * applies random durability damage to items with a drop chance <= 1.
     * All items of the player are dropped intact in dropEquipment().
     * Others keep the zombie drop chances (random equipment from finalizeSpawn, picked up items always drop).
     */
    @Override
    protected float getEquipmentDropChance(EquipmentSlot pSlot) {
        return this.fromPlayerDeath ? 0.0F : super.getEquipmentDropChance(pSlot);
    }

    /**
     * Pocket finds for zombified players that do not come from a player death (respects doMobLoot like any mob loot).
     * The pocket loot table includes the normal zombified player loot (rotten flesh).
     */
    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.fromPlayerDeath ? super.getDefaultLootTable() : POCKET_LOOT_TABLE;
    }

    /**
     * The items belong to the player, so they are dropped like a player's inventory:
     * always, independent of the doMobLoot game rule (dropCustomDeathLoot is skipped when it is false).
     * The rotten flesh comes from the loot table (regular mob loot, respects doMobLoot).
     */
    @Override
    protected void dropEquipment() {
        super.dropEquipment();

        if (!this.fromPlayerDeath) {
            // equipment is dropped by chance in dropCustomDeathLoot like for zombies
            dropInventory();
            return;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(itemStack);
                this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
        dropInventory();
    }

    public void dropInventory() {
        for (int i = 0; i < this.main.size(); i++) {
            if (!this.main.get(i).isEmpty()) {
                this.spawnAtLocation(this.main.get(i));
                this.main.set(i, ItemStack.EMPTY);
            }
        }

        for (ItemStack curiosItem : this.trinketsItems) {
            if (!curiosItem.isEmpty()) {
                this.spawnAtLocation(curiosItem);
            }
        }
        this.trinketsItems.clear();

        for (ItemStack accessoryItem : this.accessoriesItems) {
            if (!accessoryItem.isEmpty()) {
                this.spawnAtLocation(accessoryItem);
            }
        }
        this.accessoriesItems.clear();
    }

    public static ZombifiedPlayerEntity spawnZombifiedPlayer(Player player) {
        DamageSource source = player.getLastDamageSource();
        return spawnZombifiedPlayer(player, source != null ? source : player.damageSources().generic());
    }

    /** source: the cause of the death (drop rules of accessories can depend on it) */
    public static ZombifiedPlayerEntity spawnZombifiedPlayer(Player player, DamageSource source) {
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.level() instanceof ServerLevel serverWorld) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER, serverWorld);
            zombifiedPlayer.fromPlayerDeath = true;
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            //zombifiedPlayer.storeGameProfile(player.getGameProfile());
            if (MainConfig.getDisplayNameTagForZombifiedPlayer()) {
                Component name = Component.nullToEmpty("Zombified " + player.getScoreboardName());
                zombifiedPlayer.setCustomName(name);
            }
            zombifiedPlayer.setPos(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.applyConfig();
            zombifiedPlayer.setHealth(zombifiedPlayer.getMaxHealth());
            zombifiedPlayer.setPersistenceRequired();
            zombifiedPlayer.transferInventory(player);
            if (TrinketsUtil.isTrinketsLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
                // also with keepInventory: trinkets with the rule DROP go to the zombified player
                zombifiedPlayer.trinketsItems.addAll(TrinketsUtil.takeDroppedTrinkets(player, playerKeepsInventory(player)));
            }
            if (AccessoriesUtil.isAccessoriesLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
                // also with keepInventory: accessories with the rule DROP go to the zombified player
                zombifiedPlayer.accessoriesItems.addAll(AccessoriesUtil.takeDroppedAccessories(player, source, playerKeepsInventory(player)));
            }
            serverWorld.addFreshEntity(zombifiedPlayer);
        }
        return zombifiedPlayer;
    }

    public void transferInventory(Player playerEntity) {
        if (playerKeepsInventory(playerEntity)) {
            // Like vanilla keepInventory: the player keeps everything, including items with Curse of Vanishing
            return;
        }

        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getMainHandItem())) {
            playerEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, playerEntity.getMainHandItem().copyAndClear());
            }
        }

        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getOffhandItem())) {
            playerEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.OFF_HAND, playerEntity.getOffhandItem().copyAndClear());
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.hasVanishingCurse(playerEntity.getInventory().armor.get(i))) {
                playerEntity.getInventory().armor.set(i, ItemStack.EMPTY);
            } else {
                if (MainConfig.getTransferArmorToZombifiedPlayer()) {
                    // Put the item into the same armor slot directly. equipItemIfPossible() returns EMPTY
                    // (and the item is lost) when the item is not wearable in that slot.
                    this.setItemSlot(armorSlot(i), playerEntity.getInventory().armor.get(i).copyAndClear());
                }
            }
        }

        for (int i = 0; i < playerEntity.getInventory().items.size(); i++) {
            if (!playerEntity.getInventory().items.get(i).isEmpty()) {
                if (EnchantmentHelper.hasVanishingCurse(playerEntity.getInventory().items.get(i))) {
                    playerEntity.getInventory().items.set(i, ItemStack.EMPTY);
                    this.main.set(i, ItemStack.EMPTY);
                }
                if (MainConfig.getTransferInventoryToZombifiedPlayer()) {
                    this.main.set(i, playerEntity.getInventory().items.get(i).copyAndClear());
                }
            }
        }
    }

    private static boolean playerKeepsInventory(Player player) {
        return player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !MainConfig.getIgnoreKeepInventory();
    }

    /** Armor slot for an index of the player's armor list (0 = feet ... 3 = head). */
    private static EquipmentSlot armorSlot(int index) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && slot.getIndex() == index) {
                return slot;
            }
        }
        throw new IllegalArgumentException("No armor slot with index " + index);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (gameProfile != null) {
            nbt.putUUID("gameProfileUUID", gameProfile.getId());
            nbt.putString("gameProfileName", gameProfile.getName());
        }
        nbt.putBoolean(FROM_PLAYER_DEATH_KEY, this.fromPlayerDeath);
        nbt.put("Inventory", this.writeInventoryToNbt(new ListTag()));
        // Store Trinket items
        nbt.put("TrinketItems", TrinketsUtil.writeTrinketItemsToNbt(this.trinketsItems));
        nbt.put("AccessoriesItems", AccessoriesUtil.writeAccessoriesItemsToNbt(this.accessoriesItems));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        // optional: /summon without NBT has no profile
        if (nbt.hasUUID("gameProfileUUID")) {
            setGameProfile(new GameProfile(nbt.getUUID("gameProfileUUID"), nbt.getString("gameProfileName")));
        }
        // Saves from before 2.0.0 have no marker: back then every zombified player was treated as one from a player death
        this.fromPlayerDeath = !nbt.contains(FROM_PLAYER_DEATH_KEY) || nbt.getBoolean(FROM_PLAYER_DEATH_KEY);
        ListTag nbtList = nbt.getList("Inventory", Tag.TAG_COMPOUND);
        this.readInventoryFromNbt(nbtList);
        // Load Trinket items
        if (nbt.contains("TrinketItems")) {
            ListTag curiosNbt = nbt.getList("TrinketItems", Tag.TAG_COMPOUND);
            this.trinketsItems.clear();
            this.trinketsItems.addAll(TrinketsUtil.readTrinketItemsFromNbt(curiosNbt));
        }
        if (nbt.contains("AccessoriesItems")) {
            ListTag accessoriesNbt = nbt.getList("AccessoriesItems", Tag.TAG_COMPOUND);
            this.accessoriesItems.clear();
            this.accessoriesItems.addAll(AccessoriesUtil.readAccessoriesItemsFromNbt(accessoriesNbt));
        }
        applyConfig();
        // /summon or spawners with NBT (no finalizeSpawn): without a saved health the entity still has the health
        // it was created with, before applyConfig raised the max health (makeTheZombifiedPlayersStronger)
        if (!nbt.contains("Health", Tag.TAG_ANY_NUMERIC)) {
            this.setHealth(this.getMaxHealth());
        }
    }

    public ListTag writeInventoryToNbt(ListTag nbtList) {
        int i;
        CompoundTag nbtCompound;
        for(i = 0; i < this.main.size(); ++i) {
            if (!((ItemStack)this.main.get(i)).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)i);
                ((ItemStack)this.main.get(i)).save(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }
        return nbtList;
    }

    public void readInventoryFromNbt(ListTag nbtList) {
        this.main.clear();

        for(int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.of(nbtCompound);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.main.size()) {
                    this.main.set(j, itemStack);
                }
            }
        }
    }
}
