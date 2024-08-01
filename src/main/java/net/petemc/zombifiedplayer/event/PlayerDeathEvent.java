package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

public class PlayerDeathEvent {

    private static LivingEntity pPlayer = null;
    private static LivingEntity pAttacker = null;
    private static DamageSource pSource = null;
    private static float pAmount = 0.0f;

    public PlayerDeathEvent() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            pPlayer = entity;
            pAttacker = damageSource.getAttacker() instanceof LivingEntity ? ((LivingEntity) damageSource.getAttacker()) : null;
            pSource = damageSource;
            pAmount = amount;
            execute();
            return true;
        });
    }

    public static void execute() {
        if (pPlayer == null) {
            ZombifiedPlayer.LOGGER.warn("Failed to load Player entity!");
        } else if (pPlayer.getWorld() == null) {
            ZombifiedPlayer.LOGGER.warn("Failed to load World!");
        } else {
            if (pPlayer instanceof ServerPlayerEntity serverPlayer && ZombifiedPlayerConfig.INSTANCE.spawnZombifiedPlayerAfterDeath) {
                if (!isAttackerIsUndead() && ZombifiedPlayerConfig.INSTANCE.onlyOnKilledByUndead) {
                    return;
                }
                ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
            }
        }
    }

    private static boolean isAttackerIsUndead() {
        boolean attackerIsUndead = false;
        if (pAttacker != null) {
            attackerIsUndead =
                   ((pAttacker.getType() == EntityType.ZOMBIE) ||
                    (pAttacker.getType() == EntityType.HUSK) ||
                    (pAttacker.getType() == EntityType.ZOMBIFIED_PIGLIN) ||
                    (pAttacker.getType() == EntityType.DROWNED) ||
                    (pAttacker.getType() == EntityType.ZOMBIE_VILLAGER) ||
                    (pAttacker.getType() == EntityType.ZOGLIN) ||
                    (pAttacker.getType() == ModEntities.ZOMBIFIED_PLAYER));
        }
        return attackerIsUndead;
    }

    public static void registerEvent() {
        new PlayerDeathEvent();
    }
}
