package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

public class PlayerDeathEvents {

    @Mod.EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
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
