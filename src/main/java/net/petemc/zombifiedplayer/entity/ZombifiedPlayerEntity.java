package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.CuriosDeathDrops;
import net.petemc.zombifiedplayer.util.CuriosUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class ZombifiedPlayerEntity extends Zombie {
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
            Identifier.fromNamespaceAndPath("zombifiedplayer", "entities/zombified_player_pocket"));

    private static final String FROM_PLAYER_DEATH_KEY = "FromPlayerDeath";

    /** Armor slots of the player, transferred into the same slots of the zombified player. */
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public GameProfile gameProfile;

    /**
     * True for zombified players created by a player death. Only they keep the special rules for the player's items:
     * everything drops intact, no despawning in peaceful, no pocket loot. Natural spawns, spawn eggs, spawners and
     * /summon without NBT behave like zombies (finalizeSpawn sets false).
     */
    private boolean fromPlayerDeath = false;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);
    public final List<ItemStack> curiosItems = new ArrayList<>();
    /** Accessories does not exist for Minecraft 26.1 yet, the list only keeps what was saved. */
    public final List<ItemStack> accessoriesItems = new ArrayList<>();

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
        // Covers every way of creating the entity (death, /summon, spawn egg, spawner)
        this.setCanBreakDoors(true);
    }

    /**
     * Registered once at mod loading: always the normal values. The config option makeTheZombifiedPlayersStronger
     * is applied per entity in applyConfig(), so a change in the config screen applies without a restart.
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
    public boolean canBreakDoors() {
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
    protected void customServerAiStep(@NotNull ServerLevel level) {
        super.customServerAiStep(level);
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
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue(0.0);
    }

    /**
     * Profile of the dead player, synced to the clients for the skin (also for zombified players already in the world).
     * The UUID as a string (empty: no profile), there is no serializer for an optional UUID anymore.
     */
    private static final EntityDataAccessor<String> DATA_PROFILE_ID =
            SynchedEntityData.defineId(ZombifiedPlayerEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_PROFILE_NAME =
            SynchedEntityData.defineId(ZombifiedPlayerEntity.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFILE_ID, "");
        builder.define(DATA_PROFILE_NAME, "");
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (this.level().isClientSide() && (DATA_PROFILE_ID.equals(key) || DATA_PROFILE_NAME.equals(key))) {
            // without a profile (e.g. /summon without NBT and no random names) the renderer falls back to the default skin
            String id = this.entityData.get(DATA_PROFILE_ID);
            this.gameProfile = id.isEmpty() ? null : new GameProfile(UUID.fromString(id), this.entityData.get(DATA_PROFILE_NAME));
        }
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_PROFILE_NAME, gameProfile == null || gameProfile.name() == null ? "" : gameProfile.name());
            this.entityData.set(DATA_PROFILE_ID, gameProfile == null || gameProfile.id() == null ? "" : gameProfile.id().toString());
        }
    }

    /**
     * Called for natural spawns, /summon without NBT, spawn eggs and spawners, not for zombified players from a player death.
     * Without a profile, one of the configured player names is picked so it gets that player's skin.
     */
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty,
                                        @NotNull EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        // /summon and spawners load the (otherwise empty) NBT first, which counts as an old save without the marker
        this.fromPlayerDeath = false;
        applyConfig();
        this.setHealth(this.getMaxHealth());
        // Vanilla zombie: random equipment by local difficulty, can pick up loot, leader bonus etc.
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (this.gameProfile == null) {
            boolean natural = reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
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
     * From a player death never removed in peaceful: the zombified player carries the dead player's items,
     * despawning would delete them. It cannot attack players in peaceful anyway (persistent, so the distance
     * despawn does not apply either). Others like zombies.
     */
    @Override
    public void checkDespawn() {
        if (this.fromPlayerDeath && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.checkDespawn();
    }

    /**
     * From a player death vanilla must never drop the equipment by itself: it only does so by chance and
     * applies random durability damage to items with a drop chance <= 1.
     * All items of the player are dropped intact in dropEquipment().
     * Others keep the zombie drop chances (random equipment from finalizeSpawn, picked up items always drop).
     */
    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource source, boolean recentlyHit) {
        if (!this.fromPlayerDeath) {
            super.dropCustomDeathLoot(level, source, recentlyHit);
        }
    }

    /**
     * Pocket finds for zombified players that do not come from a player death (respects doMobLoot like any mob loot).
     * The pocket loot table includes the normal zombified player loot (rotten flesh).
     */
    @Override
    protected void dropFromLootTable(@NotNull ServerLevel level, @NotNull DamageSource source, boolean playerKilled) {
        if (this.fromPlayerDeath) {
            super.dropFromLootTable(level, source, playerKilled);
        } else {
            this.dropFromLootTable(level, source, playerKilled, POCKET_LOOT_TABLE);
        }
    }

    /**
     * The items belong to the player, so they are dropped like a player's inventory:
     * always, independent of the doMobLoot game rule (dropCustomDeathLoot is skipped when it is off).
     * The rotten flesh comes from the loot table (regular mob loot, respects doMobLoot).
     */
    @Override
    protected void dropEquipment(@NotNull ServerLevel level) {
        super.dropEquipment(level);

        if (!this.fromPlayerDeath) {
            // equipment is dropped by chance in dropCustomDeathLoot like for zombies
            dropInventory(level);
            return;
        }

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(level, itemStack);
                this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
        }
        dropInventory(level);
    }

    public void dropInventory(ServerLevel level) {
        for (int i = 0; i < this.main.size(); i++) {
            if (!this.main.get(i).isEmpty()) {
                this.spawnAtLocation(level, this.main.get(i));
                this.main.set(i, ItemStack.EMPTY);
            }
        }
        dropList(level, this.curiosItems);
        dropList(level, this.accessoriesItems);
    }

    private void dropList(ServerLevel level, List<ItemStack> items) {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                this.spawnAtLocation(level, item);
            }
        }
        items.clear();
    }

    public static ZombifiedPlayerEntity spawnZombifiedPlayer(Player player) {
        DamageSource source = player.getLastDamageSource();
        return spawnZombifiedPlayer(player, source != null ? source : player.damageSources().generic());
    }

    /** source: the cause of the death (drop rules of accessories can depend on it, once Accessories exists for 26.x) */
    public static ZombifiedPlayerEntity spawnZombifiedPlayer(Player player, DamageSource source) {
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.level() instanceof ServerLevel serverLevel) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER.get(), serverLevel);
            zombifiedPlayer.fromPlayerDeath = true;
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            if (MainConfig.getDisplayNameTagForZombifiedPlayer()) {
                Component name = Component.literal("Zombified " + player.getName().getString());
                zombifiedPlayer.setCustomName(name);
            }
            zombifiedPlayer.setPos(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.applyConfig();
            zombifiedPlayer.setHealth(zombifiedPlayer.getMaxHealth());
            zombifiedPlayer.setPersistenceRequired();
            boolean keepsInventory = playerKeepsInventory(serverLevel);
            zombifiedPlayer.transferInventory(player, keepsInventory);
            if (CuriosUtil.isCuriosLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
                // Curios applies its drop rules when the player drops the loot (also with keepInventory: "always drop" items)
                CuriosDeathDrops.expectDrops(player, zombifiedPlayer);
            }
            // Accessories is not available for Minecraft 26.1 yet (AccessoriesUtil is commented out)
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

    public void transferInventory(Player playerEntity, boolean playerKeepsInventory) {
        if (playerKeepsInventory) {
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

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (EnchantmentHelper.has(playerEntity.getItemBySlot(slot), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.setItemSlot(slot, ItemStack.EMPTY);
            } else {
                if (MainConfig.getTransferArmorToZombifiedPlayer()) {
                    // Put the item into the same armor slot directly. equipItemIfPossible() returns EMPTY
                    // (and the item is lost) when the item is not wearable in that slot.
                    this.setItemSlot(slot, playerEntity.getItemBySlot(slot).copyAndClear());
                }
            }
        }

        NonNullList<ItemStack> items = playerEntity.getInventory().getNonEquipmentItems();
        for (int i = 0; i < items.size() && i < this.main.size(); i++) {
            if (!items.get(i).isEmpty()) {
                if (EnchantmentHelper.has(items.get(i), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    items.set(i, ItemStack.EMPTY);
                    this.main.set(i, ItemStack.EMPTY);
                }
                if (MainConfig.getTransferInventoryToZombifiedPlayer()) {
                    this.main.set(i, items.get(i).copyAndClear());
                }
            }
        }
    }

    private static boolean playerKeepsInventory(ServerLevel level) {
        return level.getGameRules().get(GameRules.KEEP_INVENTORY) && !MainConfig.getIgnoreKeepInventory();
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        if (gameProfile != null) {
            valueOutput.putString("gameProfileUUID", gameProfile.id().toString());
            valueOutput.putString("gameProfileName", gameProfile.name());
        }
        valueOutput.putBoolean(FROM_PLAYER_DEATH_KEY, this.fromPlayerDeath);
        // Save Zombified Player inventory
        this.saveListItems(valueOutput.list("Inventory", ItemStackWithSlot.CODEC), this.main);
        this.saveListItems(valueOutput.list("CuriosItems", ItemStackWithSlot.CODEC), this.curiosItems);
        this.saveListItems(valueOutput.list("AccessoriesItems", ItemStackWithSlot.CODEC), this.accessoriesItems);
    }

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        // optional: /summon without NBT has no profile. Saved as a string, /summon also takes the int array form
        valueInput.read("gameProfileUUID", UUIDUtil.LENIENT_CODEC).ifPresent(uuid ->
                setGameProfile(new GameProfile(uuid, valueInput.getStringOr("gameProfileName", ""))));
        // Saves from before 2.0.0 have no marker: back then every zombified player was treated as one from a player death
        this.fromPlayerDeath = valueInput.getBooleanOr(FROM_PLAYER_DEATH_KEY, true);
        this.loadInventory(valueInput.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
        this.loadListItems(valueInput.listOrEmpty("CuriosItems", ItemStackWithSlot.CODEC), this.curiosItems);
        this.loadListItems(valueInput.listOrEmpty("AccessoriesItems", ItemStackWithSlot.CODEC), this.accessoriesItems);
        applyConfig();
        // /summon or spawners with NBT (no finalizeSpawn): without a saved health the entity still has the health
        // it was created with, before applyConfig raised the max health (makeTheZombifiedPlayersStronger)
        if (Float.isNaN(valueInput.getFloatOr("Health", Float.NaN))) {
            this.setHealth(this.getMaxHealth());
        }
    }

    public void loadInventory(ValueInput.TypedInputList<ItemStackWithSlot> list) {
        this.main.clear();
        for (ItemStackWithSlot itemStackWithSlot : list) {
            if (itemStackWithSlot.isValidInContainer(this.main.size())) {
                this.main.set(itemStackWithSlot.slot(), itemStackWithSlot.stack());
            }
        }
    }

    public void saveListItems(ValueOutput.TypedOutputList<ItemStackWithSlot> list, List<ItemStack> items) {
        for (int i = 0; i < items.size(); ++i) {
            ItemStack itemstack = items.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new ItemStackWithSlot(i, itemstack));
            }
        }
    }

    public void loadListItems(ValueInput.TypedInputList<ItemStackWithSlot> list, List<ItemStack> items) {
        items.clear();
        for (ItemStackWithSlot itemStackWithSlot : list) {
            items.add(itemStackWithSlot.stack());
        }
    }
}
