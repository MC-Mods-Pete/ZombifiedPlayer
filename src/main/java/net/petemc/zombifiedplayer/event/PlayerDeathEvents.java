package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.HardcoreRevivalUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

public class PlayerDeathEvents {

    @Mod.EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
    public static class ForgeEvents {

        /**
         * Priority LOW: mods that cancel the death (totems, revive mods) with normal or higher priority run first,
         * so the inventory is not transferred for a player who survives. Cancelled events are not received.
         * Not LOWEST: the corelib of Corpse and Gravestone takes its snapshot of the player at LOWEST,
         * the transfer must happen before that.
         */
        @SubscribeEvent(priority = EventPriority.LOW)
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!event.getEntity().level().isClientSide()
                    && event.getEntity() instanceof ServerPlayer serverPlayer) {
                // Hardcore Revival: a timeout death counts with the cause of the knockout
                DamageSource effectiveSource = HardcoreRevivalUtil.resolveEffectiveDeathSource(serverPlayer, event.getSource());
                // Final death reached: the tracked knockout cause is not needed anymore
                HardcoreRevivalUtil.clearTrackedCause(serverPlayer);

                boolean spawned = ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, effectiveSource)
                        && ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer, event.getSource()) != null;
                // read by ServerPlayerEntityMixin for the chat message later in ServerPlayer.die()
                ZombifiedPlayerSpawnLogic.setSpawnedThisDeath(serverPlayer, spawned);
            }
        }
    }
}
