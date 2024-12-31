package net.petemc.zombifiedplayer.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.util.GameProfileData;
import net.petemc.zombifiedplayer.util.StateSaverAndLoader;
import net.petemc.zombifiedplayer.ZombifiedPlayer;


public class ZombifiedPlayerEntity extends ZombieEntity {
    public GameProfile gameProfile;
    public final DefaultedList<ItemStack> main = DefaultedList.ofSize(36, ItemStack.EMPTY);

    public ZombifiedPlayerEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createZombifiedPlayerAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, ZombifiedPlayerConfig.INSTANCE.makeTheZombifiedPlayersStronger ? 40.0 : 20.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, ZombifiedPlayerConfig.INSTANCE.makeTheZombifiedPlayersStronger ? 50.0 : 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, ZombifiedPlayerConfig.INSTANCE.makeTheZombifiedPlayersStronger ? 0.29f : 0.23f)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, ZombifiedPlayerConfig.INSTANCE.makeTheZombifiedPlayersStronger ? 4.0 : 2.0)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0)
                .add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    public boolean canBreakDoors()
    {
        return ZombifiedPlayerConfig.INSTANCE.zombifiedPlayersCanBreakDoors;
    }

    @Override
    protected boolean canConvertInWater() {
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
    protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
        super.dropEquipment(source, lootingMultiplier, allowDrops);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            this.dropStack(itemStack);
            this.equipStack(equipmentSlot, ItemStack.EMPTY);
        }
        dropInventory();
    }

    public void dropInventory() {
        super.dropInventory();
        for (int i = 0; i < this.main.size(); i++) {
            if (!this.main.get(i).isEmpty()) {
                this.dropStack(this.main.get(i));
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
            Text name = Text.of("Zombified " + player.getEntityName());
            zombifiedPlayer.setCustomName(name);
            zombifiedPlayer.setPosition(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.setPersistent();
            zombifiedPlayer.transferInventory(player);
            serverWorld.spawnEntity(zombifiedPlayer);
        }
        return zombifiedPlayer;
    }

    public void transferInventory(PlayerEntity playerEntity) {
        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getMainHandStack())) {
            playerEntity.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (ZombifiedPlayerConfig.INSTANCE.transferMainandOffHandToZombifiedPlayer) {
                this.setStackInHand(Hand.MAIN_HAND, playerEntity.getMainHandStack().copy());
                playerEntity.getMainHandStack().setCount(0);
            }
        }

        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getOffHandStack())) {
            playerEntity.setStackInHand(Hand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (ZombifiedPlayerConfig.INSTANCE.transferMainandOffHandToZombifiedPlayer) {
                this.setStackInHand(Hand.OFF_HAND, playerEntity.getOffHandStack().copy());
                playerEntity.getOffHandStack().setCount(0);
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.hasVanishingCurse(playerEntity.getInventory().armor.get(i))) {
                playerEntity.getInventory().armor.set(i, ItemStack.EMPTY);
            } else {
                if (ZombifiedPlayerConfig.INSTANCE.transferArmorToZombifiedPlayer) {
                    this.tryEquip(playerEntity.getInventory().armor.get(i).copy());
                    playerEntity.getInventory().armor.get(i).setCount(0);
                }
            }
        }

        for (int i = 0; i < playerEntity.getInventory().main.size(); i++) {
            if (!playerEntity.getInventory().main.get(i).isEmpty()) {
                if (EnchantmentHelper.hasVanishingCurse(playerEntity.getInventory().main.get(i))) {
                    playerEntity.getInventory().main.set(i, ItemStack.EMPTY);
                    this.main.set(i, ItemStack.EMPTY);
                }
                if (ZombifiedPlayerConfig.INSTANCE.transferInventoryToZombifiedPlayer) {
                    this.main.set(i, playerEntity.getInventory().main.get(i).copy());
                    playerEntity.getInventory().main.get(i).setCount(0);
                }
            }
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("Inventory", this.writeNbt(new NbtList()));
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        NbtList nbtList = nbt.getList("Inventory", NbtElement.COMPOUND_TYPE);
        this.readNbt(nbtList);
    }

    public NbtList writeNbt(NbtList nbtList) {
        int i;
        NbtCompound nbtCompound;
        for(i = 0; i < this.main.size(); ++i) {
            if (!((ItemStack)this.main.get(i)).isEmpty()) {
                nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte)i);
                ((ItemStack)this.main.get(i)).writeNbt(nbtCompound);
                nbtList.add(nbtCompound);
            }
        }
        return nbtList;
    }

    public void readNbt(NbtList nbtList) {
        this.main.clear();

        for(int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            int j = nbtCompound.getByte("Slot") & 255;
            ItemStack itemStack = ItemStack.fromNbt(nbtCompound);
            if (!itemStack.isEmpty()) {
                if (j >= 0 && j < this.main.size()) {
                    this.main.set(j, itemStack);
                }
            }
        }
    }
}
