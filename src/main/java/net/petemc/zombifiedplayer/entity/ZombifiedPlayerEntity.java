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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.petemc.zombifiedplayer.Config;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class ZombifiedPlayerEntity extends Zombie implements IEntityAdditionalSpawnData {
    public GameProfile gameProfile;
    public final NonNullList<ItemStack> main = NonNullList.withSize(36, ItemStack.EMPTY);

    public ZombifiedPlayerEntity(EntityType<? extends Zombie> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Config.getMakeTheZombifiedPlayersStronger() ? 40.0 : 20.0)
                .add(Attributes.FOLLOW_RANGE, Config.getMakeTheZombifiedPlayersStronger() ? 50.0 : 40.0)
                .add(Attributes.MOVEMENT_SPEED, Config.getMakeTheZombifiedPlayersStronger() ? 0.29f : 0.23f)
                .add(Attributes.ATTACK_DAMAGE, Config.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
                .add(Attributes.ARMOR, Config.getMakeTheZombifiedPlayersStronger() ? 4.0 : 2.0)
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
        return Config.getMakeTheZombifiedPlayersImmuneToFire();
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean canBreakDoors()
    {
        return Config.getZombifiedPlayersCanBreakDoors();
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

    /*public void storeGameProfile(GameProfile gameProfile) {
        if (!this.level().isClientSide()) {
            GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(this.getUUID(), this.level());
            gameProfileState.gameProfileUUID = gameProfile.getId();
            gameProfileState.gameProfileName = gameProfile.getName();
            ZombifiedPlayer.LOGGER.info("Storing GameProfile info for {}, {}, {}",this.getUUID().toString(),gameProfileState.gameProfileUUID.toString(),gameProfileState.gameProfileName);
        }
    }*/

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pDamageSource, int pLooting, boolean pHitByPlayer) {
        super.dropCustomDeathLoot(pDamageSource, pLooting, pHitByPlayer);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            this.spawnAtLocation(itemStack);
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
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
    }

    public static ZombifiedPlayerEntity spawnZombifiedPlayer(Player player) {
        ZombifiedPlayerEntity zombifiedPlayer = null;
        if (player.level() instanceof ServerLevel serverLevel) {
            zombifiedPlayer = new ZombifiedPlayerEntity(ModEntities.ZOMBIFIED_PLAYER.get(), serverLevel);
            zombifiedPlayer.setGameProfile(player.getGameProfile());
            //zombifiedPlayer.storeGameProfile(player.getGameProfile());
            Component name = Component.literal("Zombified " + player.getName().getString());
            zombifiedPlayer.setCustomName(name);
            zombifiedPlayer.setPos(player.getX(), player.getY(), player.getZ());
            zombifiedPlayer.setPersistenceRequired();
            zombifiedPlayer.transferInventory(player);
            serverLevel.addFreshEntity(zombifiedPlayer);
        }
        return zombifiedPlayer;
    }

    public void transferInventory(Player playerEntity) {
        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getMainHandItem())) {
            playerEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        } else {
            if (Config.getTransferMainandOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, playerEntity.getMainHandItem().copyAndClear());
            }
        }

        if (EnchantmentHelper.hasVanishingCurse(playerEntity.getOffhandItem())) {
            playerEntity.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            if (Config.getTransferMainandOffHandToZombifiedPlayer()) {
                this.setItemInHand(InteractionHand.OFF_HAND, playerEntity.getOffhandItem().copyAndClear());
            }
        }

        for (int i = 0; i < 4; i++) {
            if (EnchantmentHelper.hasVanishingCurse(playerEntity.getInventory().armor.get(i))) {
                playerEntity.getInventory().armor.set(i, ItemStack.EMPTY);
            } else {
                if (Config.getTransferArmorToZombifiedPlayer()) {
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
                if (Config.getTransferInventoryToZombifiedPlayer()) {
                    this.main.set(i, playerEntity.getInventory().items.get(i).copyAndClear());
                }
            }
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
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        UUID gpUUID = nbt.getUUID("gameProfileUUID");
        String gpName = nbt.getString("gameProfileName");
        gameProfile = new GameProfile(gpUUID, gpName);
        ListTag nbtList = nbt.getList("Inventory", Tag.TAG_COMPOUND);
        this.readInventoryFromNbt(nbtList);
    }

    public ListTag writeInventoryToNbt(ListTag nbtList) {
        int i;
        CompoundTag nbtCompound;
        for(i = 0; i < this.main.size(); ++i) {
            if (!((ItemStack)this.main.get(i)).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte)i);
                ((ItemStack)this.main.get(i)).deserializeNBT(nbtCompound);
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
