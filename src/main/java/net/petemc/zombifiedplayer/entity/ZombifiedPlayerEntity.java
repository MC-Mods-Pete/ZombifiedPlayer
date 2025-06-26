package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.util.Map;
import java.util.Objects;


public class ZombifiedPlayerEntity extends ZombieEntity {
    public GameProfile gameProfile;
    public final DefaultedList<ItemStack> main = DefaultedList.ofSize(36, ItemStack.EMPTY);
    public static final Int2ObjectMap<EquipmentSlot> EQUIPMENT_SLOTS;

    public ZombifiedPlayerEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createZombifiedPlayerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.MAX_HEALTH, MainConfig.getMakeTheZombifiedPlayersStronger() ? 40.0 : 20.0)
                .add(EntityAttributes.FOLLOW_RANGE, MainConfig.getMakeTheZombifiedPlayersStronger() ? 50.0 : 40.0)
                .add(EntityAttributes.MOVEMENT_SPEED, MainConfig.getMakeTheZombifiedPlayersStronger() ? 0.29f : 0.23f)
                .add(EntityAttributes.ATTACK_DAMAGE, MainConfig.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
                .add(EntityAttributes.ARMOR, MainConfig.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
                .add(EntityAttributes.SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initCustomGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new ZombieAttackGoal(this, (double)1.0F, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, (double)1.0F, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, (double)1.0F));
        this.targetSelector.add(2, new ActiveTargetGoal(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal(this, IronGolemEntity.class, true));
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity) this, entityTrackerEntry);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    public boolean isFireImmune() {
        return MainConfig.getMakeTheZombifiedPlayersImmuneToFire();
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    public boolean canBreakDoors()
    {
        return MainConfig.getZombifiedPlayersCanBreakDoors();
    }

    @Override
    protected boolean canConvertInWater() {
        return false;
    }

    @Override
    protected void initAttributes() {
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS)).setBaseValue(0.0F);
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public void storeGameProfile(GameProfile gameProfile) {
        if (!this.getWorld().isClient) {
            if (ZombifiedPlayer.serverState == null) {
                ZombifiedPlayer.LOGGER.warn("Persistant State still null!");
            }
            if (ZombifiedPlayer.serverState != null) {
                ZombifiedPlayer.serverState.gameProfiles.put(this.getUuid(), gameProfile.getId().toString() + ":" + gameProfile.getName());
                ZombifiedPlayer.serverState.markDirty();
                ZombifiedPlayer.LOGGER.info("Storing GameProfile info for {}, {}, {}", this.getUuid().toString(), gameProfile.getId().toString(), gameProfile.getName());
            }
        }
    }

    @Override
    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            Object object = source.getAttacker();
            if (object instanceof LivingEntity livingEntity) {
                object = this.getWorld();
                if (object instanceof ServerWorld serverWorld) {
                    EnchantmentHelper.getEquipmentDropChance(serverWorld, livingEntity, source, 1.0f);
                }
            }
            this.dropStack(world, itemStack);
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
        }
        dropInventory(world);
    }

    public void dropInventory(ServerWorld world) {
        super.dropInventory(world);
        for (int i = 0; i < this.main.size(); i++) {
            if (!this.main.get(i).isEmpty()) {
                this.dropStack(world, this.main.get(i));
                this.main.set(i, ItemStack.EMPTY);
            }
        }
    }

    public static ZombifiedPlayerEntity spawnZombifiedPlayer(PlayerEntity player) {
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER, serverWorld);
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            zombifiedPlayer.storeGameProfile(player.getGameProfile());
            Text name = Text.of("Zombified " + player.getName().getLiteralString());
            zombifiedPlayer.setCustomName(name);
            zombifiedPlayer.setPosition(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.setPersistent();
            zombifiedPlayer.transferInventory(serverWorld, player);
            serverWorld.spawnEntity(zombifiedPlayer);
        }
        return zombifiedPlayer;
    }

    public void transferInventory(ServerWorld world, PlayerEntity playerEntity) {
        if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getMainHandStack(), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainandOffHandToZombifiedPlayer()) {
                this.setStackInHand(Hand.MAIN_HAND, playerEntity.getMainHandStack().copyAndEmpty());
            }
        }

        if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getOffHandStack(), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (MainConfig.getTransferMainandOffHandToZombifiedPlayer()) {
                this.setStackInHand(Hand.OFF_HAND, playerEntity.getOffHandStack().copyAndEmpty());
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getEquippedStack(EquipmentSlot.FROM_INDEX.apply(i+1)), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.equipStack(EquipmentSlot.FROM_INDEX.apply(i+1), ItemStack.EMPTY);//.getInventory().getMainStacks().set(i, ItemStack.EMPTY);
            } else {
                if (MainConfig.getTransferArmorToZombifiedPlayer()) {
                    this.tryEquip(world, playerEntity.getEquippedStack(EquipmentSlot.FROM_INDEX.apply(i+1)).copyAndEmpty());//getInventory().getMainStacks().get(i).copyAndEmpty());
                }
            }
        }

        for (int i = 0; i < playerEntity.getInventory().getMainStacks().size(); i++) {
            if (!playerEntity.getInventory().getMainStacks().get(i).isEmpty()) {
                if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getInventory().getMainStacks().get(i), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                    playerEntity.getInventory().getMainStacks().set(i, ItemStack.EMPTY);
                    this.main.set(i, ItemStack.EMPTY);
                }
                if (MainConfig.getTransferInventoryToZombifiedPlayer()) {
                    this.main.set(i, playerEntity.getInventory().getMainStacks().get(i).copyAndEmpty());
                }
            }
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.writeData(view.getListAppender("Inventory", StackWithSlot.CODEC));
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.readData(view.getTypedListView("Inventory", StackWithSlot.CODEC));
    }

    public void writeData(WriteView.ListAppender<StackWithSlot> list) {
        for(int i = 0; i < this.main.size(); ++i) {
            ItemStack itemStack = (ItemStack)this.main.get(i);
            if (!itemStack.isEmpty()) {
                list.add(new StackWithSlot(i, itemStack));
            }
        }

    }

    public void readData(ReadView.TypedListReadView<StackWithSlot> list) {
        this.main.clear();

        for(StackWithSlot stackWithSlot : list) {
            if (stackWithSlot.isValidSlot(this.main.size())) {
                this.setStack(stackWithSlot.slot(), stackWithSlot.stack());
            }
        }

    }

    public void setStack(int slot, ItemStack stack) {
        if (slot < this.main.size()) {
            this.main.set(slot, stack);
        }

        EquipmentSlot equipmentSlot = (EquipmentSlot)EQUIPMENT_SLOTS.get(slot);
        if (equipmentSlot != null) {
            this.equipment.put(equipmentSlot, stack);
        }

    }

    static {
        EQUIPMENT_SLOTS = new Int2ObjectArrayMap(Map.of(EquipmentSlot.FEET.getOffsetEntitySlotId(36), EquipmentSlot.FEET, EquipmentSlot.LEGS.getOffsetEntitySlotId(36), EquipmentSlot.LEGS, EquipmentSlot.CHEST.getOffsetEntitySlotId(36), EquipmentSlot.CHEST, EquipmentSlot.HEAD.getOffsetEntitySlotId(36), EquipmentSlot.HEAD, 40, EquipmentSlot.OFFHAND, 41, EquipmentSlot.BODY, 42, EquipmentSlot.SADDLE));
    }
}
