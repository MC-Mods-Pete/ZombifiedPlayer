package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.petemc.zombifiedplayer.Config;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.ModCompatibility;

public class PlayerDeathEvents {
    @EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if(!event.getEntity().level().isClientSide()) {
                if(event.getEntity() instanceof ServerPlayer serverPlayer) {
                    if (!(serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING))) {
                        if ((Config.getSpawnOnAnyDeath() ||
                                (ModCompatibility.diedFromInfection(serverPlayer) && Config.getSpawnWhenKilledByInfection()) ||
                                (attackerIsUndead(event.getSource().getEntity()) && Config.getSpawnZombifiedPlayerAfterDeath()))) {
                            ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
                        }
                    }
                }
            }
        }
    }

    public static boolean attackerIsUndead(Entity pAttacker) {
        boolean attackerIsUndead = false;
        if (pAttacker != null) {
            attackerIsUndead =
                   ((pAttacker.getType() == EntityType.ZOMBIE) ||
                    (pAttacker.getType() == EntityType.HUSK) ||
                    (pAttacker.getType() == EntityType.ZOMBIFIED_PIGLIN) ||
                    (pAttacker.getType() == EntityType.DROWNED) ||
                    (pAttacker.getType() == EntityType.ZOMBIE_VILLAGER) ||
                    (pAttacker.getType() == EntityType.ZOGLIN) ||
                    (pAttacker.getType() == ModEntities.ZOMBIFIED_PLAYER.get()));
        }
        attackerIsUndead = attackerIsUndead || ModCompatibility.wasKilledByHordeZombie(pAttacker);
        return attackerIsUndead;
    }
}
