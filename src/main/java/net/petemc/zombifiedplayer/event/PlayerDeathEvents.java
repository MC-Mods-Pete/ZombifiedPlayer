package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.zombifiedplayer.Config;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.CuriosUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;

public class PlayerDeathEvents {
    @Mod.EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if(!event.getEntity().level().isClientSide()) {
                if(event.getEntity() instanceof ServerPlayer serverPlayer) {
                    boolean flag = CuriosUtil.checkForItemInCurios(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance());
                    if (!(serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || flag)) {
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
        boolean attackerIsUndead = true;
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
