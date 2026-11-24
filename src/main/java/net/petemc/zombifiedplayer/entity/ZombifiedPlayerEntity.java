package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.AccessoriesUtil;
import net.petemc.zombifiedplayer.util.TrinketsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class ZombifiedPlayerEntity extends Zombie {
    public GameProfile gameProfile;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);
    public final List<ItemStack> trinketsItems = new ArrayList<>();
    public final List<ItemStack> accessoriesItems = new ArrayList<>();

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.Builder createZombifiedPlayerAttributes() {
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
    protected void randomizeReinforcementsChance() {
        Objects.requireNonNull(this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).setBaseValue(0.0F);
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    /*
    public void storeGameProfile(GameProfile gameProfile) {
        if (!this.getWorld().isClient) {
            GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(this.getUuid(), this.getWorld());
            gameProfileState.gameProfileUUID = gameProfile.getId();
            gameProfileState.gameProfileName = gameProfile.getName();
            ZombifiedPlayer.LOGGER.info("Storing GameProfile info for {}, {}, {}",this.getUuid().toString(),gameProfileState.gameProfileUUID.toString(),gameProfileState.gameProfileName);
        }
    }
     */

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropCustomDeathLoot(source, lootingMultiplier, allowDrops);

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
        dropEquipment();
    }

    public void dropEquipment() {
        super.dropEquipment();
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
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.level() instanceof ServerLevel serverWorld) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER, serverWorld);
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            //zombifiedPlayer.storeGameProfile(player.getGameProfile());
            if (MainConfig.getDisplayNameTagForZombifiedPlayer()) {
                Component name = Component.nullToEmpty("Zombified " + player.getScoreboardName());
                zombifiedPlayer.setCustomName(name);
            }
            zombifiedPlayer.setPos(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.setPersistenceRequired();
            zombifiedPlayer.transferInventory(player);
            serverWorld.addFreshEntity(zombifiedPlayer);
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
                    this.equipItemIfPossible(playerEntity.getInventory().armor.get(i).copyAndClear());
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

        if (TrinketsUtil.isTrinketsLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
            List<ItemStack> playerTrinketItems = TrinketsUtil.getTrinketItemsAndClear(playerEntity);
            this.trinketsItems.addAll(playerTrinketItems);
        }

        if (AccessoriesUtil.isAccessoriesLoaded() && MainConfig.getTransferCuriosOrTrinketItemsToZombifiedPlayer()) {
            List<ItemStack> playerAccessoryItems = AccessoriesUtil.getAccessoriesItemsAndClear(playerEntity);
            this.accessoriesItems.addAll(playerAccessoryItems);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putUUID("gameProfileUUID", gameProfile.getId());
        nbt.putString("gameProfileName", gameProfile.getName());
        nbt.put("Inventory", this.writeInventoryToNbt(new ListTag()));
        // Store Trinket items
        nbt.put("TrinketItems", TrinketsUtil.writeTrinketItemsToNbt(this.trinketsItems));
        nbt.put("AccessoriesItems", AccessoriesUtil.writeAccessoriesItemsToNbt(this.accessoriesItems));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        UUID gpUUID = nbt.getUUID("gameProfileUUID");
        String gpName = nbt.getString("gameProfileName");
        setGameProfile(new GameProfile(gpUUID, gpName));
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
