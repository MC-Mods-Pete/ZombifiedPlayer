package net.petemc.zombifiedplayer.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.util.GameProfileData;
import net.petemc.zombifiedplayer.util.StateSaverAndLoader;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.List;

public class ZombifiedPlayerEntity extends ZombieEntity {
    public GameProfile gameProfile;
    public final DefaultedList<ItemStack> main = DefaultedList.ofSize(36, ItemStack.EMPTY);
    public final DefaultedList<ItemStack> armor = DefaultedList.ofSize(4, ItemStack.EMPTY);
    public final DefaultedList<ItemStack> offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
    private final List<DefaultedList<ItemStack>> combinedInventory = ImmutableList.of(this.main, this.armor, this.offHand);

    public ZombifiedPlayerEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createZombifiedPlayerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
    }

    @Override
    public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
        return new EntitySpawnS2CPacket((Entity)this, entityTrackerEntry);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public void storeGameProfile(GameProfile gameProfile) {
        if (!this.getWorld().isClient) {
            GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(this.getUuid(), this.getWorld());
            gameProfileState.gameProfileUUID = gameProfile.getId();
            gameProfileState.gameProfileName = gameProfile.getName();
            ZombifiedPlayer.LOGGER.info("Storing GameProfile info for {}, {}, {}",this.getUuid().toString(),gameProfileState.gameProfileUUID.toString(),gameProfileState.gameProfileName);
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
            this.dropStack(itemStack);
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
        }
        dropInventory();
    }

    public void dropInventory() {
        super.dropInventory();
        for (List list : this.combinedInventory) {
            for (int i = 0; i < list.size(); ++i) {
                ItemStack itemStack = (ItemStack) list.get(i);
                if (itemStack.isEmpty()) continue;
                this.dropStack(itemStack);
                list.set(i, ItemStack.EMPTY);
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
            zombifiedPlayer.transferInventory(player);
            serverWorld.spawnEntity(zombifiedPlayer);
        }
        return zombifiedPlayer;
    }

    public void transferInventory(PlayerEntity playerEntity) {
        if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getMainHandStack(), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            this.setStackInHand(Hand.MAIN_HAND, playerEntity.getMainHandStack().copyAndEmpty());
        }

        if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getOffHandStack(), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            playerEntity.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        } else {
            this.setStackInHand(Hand.OFF_HAND, playerEntity.getOffHandStack().copyAndEmpty());
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getInventory().armor.get(i), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                playerEntity.getInventory().armor.set(i, ItemStack.EMPTY);
            } else {
                this.tryEquip(playerEntity.getInventory().armor.get(i).copyAndEmpty());
            }
        }

        for (int i = 0; i < playerEntity.getInventory().main.size(); i++) {
            if (!playerEntity.getInventory().main.get(i).isEmpty()) {
                if (EnchantmentHelper.hasAnyEnchantmentsWith(playerEntity.getInventory().main.get(i), EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
                    playerEntity.getInventory().main.set(i, ItemStack.EMPTY);
                    this.main.set(i, ItemStack.EMPTY);
                }
                this.main.set(i, playerEntity.getInventory().main.get(i).copyAndEmpty());
            }
        }
    }
}
