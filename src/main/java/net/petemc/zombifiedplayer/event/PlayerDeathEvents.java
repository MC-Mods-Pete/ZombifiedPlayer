package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

public class PlayerDeathEvents {

    @EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!event.getEntity().level().isClientSide()
                    && event.getEntity() instanceof ServerPlayer serverPlayer
                    && ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, event.getSource())) {
                ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
            }
        }
    }
}
