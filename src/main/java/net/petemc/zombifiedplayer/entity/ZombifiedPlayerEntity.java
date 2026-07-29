package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.AccessoriesUtil;
import net.petemc.zombifiedplayer.util.CuriosUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.jetbrains.annotations.NotNull;

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

    public GameProfile gameProfile;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);
    public final List<ItemStack> curiosItems = new ArrayList<>();
    public final List<ItemStack> accessoriesItems = new ArrayList<>();

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MainConfig.getMakeTheZombifiedPlayersStronger() ? 40.0 : 20.0)
                .add(Attributes.FOLLOW_RANGE, MainConfig.getMakeTheZombifiedPlayersStronger() ? 50.0 : 40.0)
                .add(Attributes.MOVEMENT_SPEED, MainConfig.getMakeTheZombifiedPlayersStronger() ? 0.29f : 0.23f)
                .add(Attributes.ATTACK_DAMAGE, MainConfig.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
                .add(Attributes.ARMOR, MainConfig.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
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

    @Override
    public boolean canBreakDoors()
    {
        return MainConfig.getZombifiedPlayersCanBreakDoors();
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

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel serverLevel, @NotNull DamageSource pDamageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(serverLevel, pDamageSource, recentlyHit);

        // drop rotten flesh
        RandomSource random = this.getRandom();
        ItemStack rottenFleshStack = new ItemStack(Items.ROTTEN_FLESH, random.nextIntBetweenInclusive(1,3));
        this.spawnAtLocation(rottenFleshStack);

        // drop equipment and inventory
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            this.spawnAtLocation(itemStack);
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
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
        if (EnchantmentHelper.has(playerEntity.getMainHandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, playerEntity.getMainHandItem().copyAndClear());
            }
        }

        if (EnchantmentHelper.has(playerEntity.getMainHandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainAndOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.OFF_HAND, playerEntity.getOffhandItem().copyAndClear());
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.has(playerEntity.getMainHandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.getInventory().armor.set(i, ItemStack.EMPTY);
            } else {
                if (MainConfig.getTransferArmorToZombifiedPlayer()) {
                    this.equipItemIfPossible(playerEntity.getInventory().armor.get(i).copyAndClear());
                }
            }
        }

        for (int i = 0; i < playerEntity.getInventory().items.size(); i++) {
            if (!playerEntity.getInventory().items.get(i).isEmpty()) {
                if (EnchantmentHelper.has(playerEntity.getMainHandItem(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
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
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity);
    }

    @Override
    public void writeSpawnData(@NotNull RegistryFriendlyByteBuf buffer) {
        if (gameProfile != null) {
            buffer.writeUUID(gameProfile.getId());
            buffer.writeUtf(gameProfile.getName());
        }
    }

    @Override
    public void readSpawnData(@NotNull RegistryFriendlyByteBuf additionalData) {
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
        nbt.put("CuriosItems", CuriosUtil.writeCuriosItemsToNbt(this, this.curiosItems, new ListTag()));
        nbt.put("AccessoriesItems", AccessoriesUtil.writeAccessoriesItemsToNbt(this, this.accessoriesItems,  new ListTag()));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        UUID gpUUID = nbt.getUUID("gameProfileUUID");
        String gpName = nbt.getString("gameProfileName");
        gameProfile = new GameProfile(gpUUID, gpName);
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
