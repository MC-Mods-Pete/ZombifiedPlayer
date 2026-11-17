package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.AccessoriesUtil;
import net.petemc.zombifiedplayer.util.CuriosUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ZombifiedPlayerEntity extends Zombie implements IEntityAdditionalSpawnData {
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

    public GameProfile gameProfile;
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

    private void setBaseValue(Attribute attribute, double value) {
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
    public float getStandingEyeHeight(@NotNull Pose pPose, @NotNull EntityDimensions pSize) {
        if (MainConfig.getUseCustomEyeHeight()) {
            return MainConfig.getCustomEyeHeight();
        }
        return super.getStandingEyeHeight(pPose, pSize);
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
     * Never in peaceful: the zombified player carries the dead player's items,
     * despawning would delete them. It cannot attack players in peaceful anyway.
     */
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    /**
     * Vanilla must never drop the equipment by itself: it only does so by chance and
     * applies random durability damage to items with a drop chance <= 1.
     * All items of the player are dropped intact in dropEquipment().
     */
    @Override
    protected float getEquipmentDropChance(@NotNull EquipmentSlot pSlot) {
        return 0.0F;
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pDamageSource, int pLooting, boolean pHitByPlayer) {
        super.dropCustomDeathLoot(pDamageSource, pLooting, pHitByPlayer);

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
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.level() instanceof ServerLevel serverLevel) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER.get(), serverLevel);
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
                    EquipmentSlot armorSlot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                    this.setItemSlot(armorSlot, playerEntity.getInventory().armor.get(i).copyAndClear());
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

        if (CuriosUtil.isCuriosLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
            List<ItemStack> playerCuriosItems = CuriosUtil.getCuriosItemsAndClear(playerEntity);
            this.curiosItems.addAll(playerCuriosItems);
        }

        if (AccessoriesUtil.isAccessoriesLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
            List<ItemStack> playerAccessoriesItems = AccessoriesUtil.getAccessoriesItemsAndClear(playerEntity);
            this.accessoriesItems.addAll(playerAccessoriesItems);
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        if (gameProfile != null) {
            buffer.writeUUID(gameProfile.getId());
            buffer.writeUtf(gameProfile.getName());
        }
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        try {
            UUID playerUUID = additionalData.readUUID();
            String playerName = additionalData.readUtf();
            gameProfile = new GameProfile(playerUUID, playerName);
        } catch (Exception ex) {
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putUUID("gameProfileUUID", gameProfile.getId());
        nbt.putString("gameProfileName", gameProfile.getName());
        nbt.put("Inventory", this.writeInventoryToNbt(new ListTag()));
        // Store Curios items
        nbt.put("CuriosItems", CuriosUtil.writeCuriosItemsToNbt(this.curiosItems));
        nbt.put("AccessoriesItems", AccessoriesUtil.writeAccessoriesItemsToNbt(this.accessoriesItems));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        UUID gpUUID = nbt.getUUID("gameProfileUUID");
        String gpName = nbt.getString("gameProfileName");
        setGameProfile(new GameProfile(gpUUID, gpName));
        ListTag nbtList = nbt.getList("Inventory", Tag.TAG_COMPOUND);
        this.readInventoryFromNbt(nbtList);
        // Load Curios items
        if (nbt.contains("CuriosItems")) {
            ListTag curiosNbt = nbt.getList("CuriosItems", Tag.TAG_COMPOUND);
            this.curiosItems.clear();
            this.curiosItems.addAll(CuriosUtil.readCuriosItemsFromNbt(curiosNbt));
        }
        if (nbt.contains("AccessoriesItems")) {
            ListTag accessoriesNbt = nbt.getList("AccessoriesItems", Tag.TAG_COMPOUND);
            this.accessoriesItems.clear();
            this.accessoriesItems.addAll(AccessoriesUtil.readAccessoriesItemsFromNbt(accessoriesNbt));
        }
        applyConfig();
    }

    public ListTag writeInventoryToNbt(ListTag nbtList) {
        int i;
        CompoundTag nbtCompound;
        for(i = 0; i < this.main.size(); ++i) {
            if (!this.main.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) i);
                this.main.get(i).save(nbtCompound);
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