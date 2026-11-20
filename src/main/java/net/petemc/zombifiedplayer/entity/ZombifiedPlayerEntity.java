package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.AccessoriesUtil;
import net.petemc.zombifiedplayer.util.CuriosDeathDrops;
import net.petemc.zombifiedplayer.util.CuriosUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ZombifiedPlayerEntity extends Zombie implements IEntityExtension, IEntityWithComplexSpawn {
    /** UUIDs of players for whom a corpse should be suppressed (set by spawnZombifiedPlayer). */
    private static final Set<UUID> SKIP_CORPSE_PLAYERS = new HashSet<>();
    /** UUIDs of players for whom a gravestone should be suppressed (set by spawnZombifiedPlayer). */
    private static final Set<UUID> SKIP_GRAVESTONE_PLAYERS = new HashSet<>();

    public static boolean shouldSkipCorpse(UUID playerUUID) {
        return SKIP_CORPSE_PLAYERS.contains(playerUUID);
    }

    public static void removeFromSkipCorpse(UUID playerUUID) {
        SKIP_CORPSE_PLAYERS.remove(playerUUID);
    }

    public static boolean shouldSkipGravestone(UUID playerUUID) {
        return SKIP_GRAVESTONE_PLAYERS.contains(playerUUID);
    }

    public static void removeFromSkipGravestone(UUID playerUUID) {
        SKIP_GRAVESTONE_PLAYERS.remove(playerUUID);
    }

    /** Extra loot ("pocket finds") of zombified players that do not come from a player death, adjustable by datapack. */
    public static final ResourceKey<LootTable> POCKET_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath("zombifiedplayer", "entities/zombified_player_pocket"));

    private static final String FROM_PLAYER_DEATH_KEY = "FromPlayerDeath";

    public GameProfile gameProfile;

    /**
     * True for zombified players created by a player death. Only they keep the special rules for the player's items:
     * everything drops intact, no despawning in peaceful, no pocket loot. Natural spawns, spawn eggs, spawners and
     * /summon without NBT behave like zombies (finalizeSpawn sets false).
     */
    private boolean fromPlayerDeath = false;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);
    public final List<ItemStack> curiosItems = new ArrayList<>();
    public final List<ItemStack> accessoriesItems = new ArrayList<>();

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        // Covers every way of creating the entity (death, /summon, spawn egg, spawner)
        this.setCanBreakDoors(true);
    }

    /**
     * Registered once at mod loading, before the server config is loaded: always the normal values.
     * The config option makeTheZombifiedPlayersStronger is applied per entity in applyConfig().
     */
    public static AttributeSupplier.@NotNull Builder createAttributes() {
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

    private void setBaseValue(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = this.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
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
    public void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue((double)0.0F);
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    /**
     * Called for natural spawns, /summon without NBT, spawn eggs and spawners, not for zombified players from a player death.
     * Without a profile, one of the configured player names is picked so it gets that player's skin.
     */
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData) {
        // /summon and spawners load the (otherwise empty) NBT first, which counts as an old save without the marker
        this.fromPlayerDeath = false;
        applyConfig();
        this.setHealth(this.getMaxHealth());
        // Vanilla zombie: random equipment by local difficulty, can pick up loot, leader bonus etc.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
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
    protected float getEquipmentDropChance(@NotNull EquipmentSlot pSlot) {
        return this.fromPlayerDeath ? 0.0F : super.getEquipmentDropChance(pSlot);
    }

    /** Pocket finds for zombified players that do not come from a player death (respects doMobLoot like any mob loot). */
    @Override
    protected @NotNull ResourceKey<LootTable> getDefaultLootTable() {
        return this.fromPlayerDeath ? super.getDefaultLootTable() : POCKET_LOOT_TABLE;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel serverLevel, @NotNull DamageSource pDamageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, pDamageSource, recentlyHit);

        // drop rotten flesh (regular mob loot, respects the doMobLoot game rule)
        RandomSource random = this.getRandom();
        ItemStack rottenFleshStack = new ItemStack(Items.ROTTEN_FLESH, random.nextIntBetweenInclusive(1,3));
        this.spawnAtLocation(rottenFleshStack);
    }

    /**
     * The items belong to the player, so they are dropped like a player's inventory:
     * always, independent of the doMobLoot game rule (dropCustomDeathLoot is skipped when it is false).
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
        //super.dropInventory();
        for (int i = 0; i < this.main.size(); i++) {
            if (!this.main.get(i).isEmpty()) {
                this.spawnAtLocation(this.main.get(i));
                this.main.set(i, ItemStack.EMPTY);
            }
        }
        
        for (ItemStack curiosItem : this.curiosItems) {
            if (!curiosItem.isEmpty()) {
                this.spawnAtLocation(curiosItem);
            }
        }
        this.curiosItems.clear();

        for (ItemStack accessoriesItem : this.accessoriesItems) {
            if (!accessoriesItem.isEmpty()) {
                this.spawnAtLocation(accessoriesItem);
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
        if (player.level() instanceof ServerLevel serverLevel) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER.get(), serverLevel);
            zombifiedPlayer.fromPlayerDeath = true;
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            //zombifiedPlayer.storeGameProfile(player.getGameProfile());
            if (MainConfig.getDisplayNameTagForZombifiedPlayer()) {
                Component name = Component.literal("Zombified " + player.getName().getString());
                zombifiedPlayer.setCustomName(name);
            }
            zombifiedPlayer.setPos(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.applyConfig();
            zombifiedPlayer.setHealth(zombifiedPlayer.getMaxHealth());
            zombifiedPlayer.setPersistenceRequired();
            zombifiedPlayer.transferInventory(player);
            if (CuriosUtil.isCuriosLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
                // Curios applies its drop rules when the player drops the loot (also with keepInventory: "always drop" items)
                CuriosDeathDrops.expectDrops(player, zombifiedPlayer);
            }
            if (AccessoriesUtil.isAccessoriesLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
                // also with keepInventory: accessories with the rule DROP go to the zombified player
                zombifiedPlayer.accessoriesItems.addAll(AccessoriesUtil.takeDroppedAccessories(player, source, playerKeepsInventory(player)));
            }
            serverLevel.addFreshEntity(zombifiedPlayer);
            // Prevent the corpse mod from spawning a corpse for this death
            if (ModCompatibility.isCorpseLoaded() && MainConfig.getCorpseCompatibility()) {
                SKIP_CORPSE_PLAYERS.add(player.getUUID());
            }
            // Prevent the gravestone mod from spawning a gravestone for this death
            if (ModCompatibility.isGravestoneLoaded() && MainConfig.getGravestoneCompatibility()) {
                SKIP_GRAVESTONE_PLAYERS.add(player.getUUID());
            }
        }
        return zombifiedPlayer;
    }

    public void transferInventory(Player playerEntity) {
        if (playerKeepsInventory(playerEntity)) {
            // Like vanilla keepInventory: the player keeps everything, including items with Curse of Vanishing
            return;
        }

        if (EnchantmentHelper.has(playerEntity.getMainHandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, playerEntity.getMainHandItem().copyAndClear());
            }
        }

        if (EnchantmentHelper.has(playerEntity.getOffhandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.OFF_HAND, playerEntity.getOffhandItem().copyAndClear());
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.has(playerEntity.getInventory().armor.get(i), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
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
                if (EnchantmentHelper.has(playerEntity.getInventory().items.get(i), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
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
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && slot.getIndex() == index) {
                return slot;
            }
        }
        throw new IllegalArgumentException("No armor slot with index " + index);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public void writeSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(gameProfile != null);
        if (gameProfile != null) {
            buffer.writeUUID(gameProfile.getId());
            buffer.writeUtf(gameProfile.getName());
        }
    }

    @Override
    public void readSpawnData(@NotNull RegistryFriendlyByteBuf additionalData) {
        // without a profile (e.g. /summon without NBT) the renderer falls back to the default skin
        if (additionalData.readBoolean()) {
            UUID playerUUID = additionalData.readUUID();
            String playerName = additionalData.readUtf();
            gameProfile = new GameProfile(playerUUID, playerName);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        if (gameProfile != null) {
            nbt.putUUID("gameProfileUUID", gameProfile.getId());
            nbt.putString("gameProfileName", gameProfile.getName());
        }
        nbt.putBoolean(FROM_PLAYER_DEATH_KEY, this.fromPlayerDeath);
        nbt.put("Inventory", this.writeInventoryToNbt(new ListTag()));
        // Store Curios items
        nbt.put("CuriosItems", CuriosUtil.writeCuriosItemsToNbt(this, this.curiosItems, new ListTag()));
        nbt.put("AccessoriesItems", AccessoriesUtil.writeAccessoriesItemsToNbt(this, this.accessoriesItems,  new ListTag()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        // optional: /summon without NBT has no profile
        if (nbt.hasUUID("gameProfileUUID")) {
            setGameProfile(new GameProfile(nbt.getUUID("gameProfileUUID"), nbt.getString("gameProfileName")));
        }
        // Saves from before 2.0.0 have no marker: back then every zombified player was treated as one from a player death
        this.fromPlayerDeath = !nbt.contains(FROM_PLAYER_DEATH_KEY) || nbt.getBoolean(FROM_PLAYER_DEATH_KEY);
        ListTag nbtList = nbt.getList("Inventory", Tag.TAG_COMPOUND);
        this.readInventoryFromNbt(nbtList);
        // Load Curios items
        if (nbt.contains("CuriosItems")) {
            ListTag curiosNbt = nbt.getList("CuriosItems", Tag.TAG_COMPOUND);
            this.curiosItems.clear();
            this.curiosItems.addAll(CuriosUtil.readCuriosItemsFromNbt(this, curiosNbt));
        }
        if (nbt.contains("AccessoriesItems")) {
            ListTag accessoriesNbt = nbt.getList("AccessoriesItems", Tag.TAG_COMPOUND);
            this.accessoriesItems.clear();
            this.accessoriesItems.addAll(AccessoriesUtil.readAccessoriesItemsFromNbt(this, accessoriesNbt));
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
            if (!this.main.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) i);
                nbtList.add(this.main.get(i).save(this.registryAccess(), nbtCompound));
            }
        }
        return nbtList;
    }

    public void readInventoryFromNbt(ListTag nbtList) {
        this.main.clear();

        for(int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.parse(this.registryAccess(), nbtCompound).orElse(ItemStack.EMPTY);
            if (j >= 0 && j < this.main.size()) {
                this.main.set(j, itemStack);
            }
        }
    }
}
