package net.petemc.zombifiedplayer.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.HardcoreRevivalUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

public class PlayerDeathEvents {

    @Mod.EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onPlayerDeath(LivingDeathEvent event) {
            if (!event.getEntity().level().isClientSide()
                    && event.getEntity() instanceof ServerPlayer serverPlayer) {
                // Hardcore Revival: a timeout death counts with the cause of the knockout
                DamageSource effectiveSource = HardcoreRevivalUtil.resolveEffectiveDeathSource(serverPlayer, event.getSource());
                if (ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, effectiveSource)) {
                    ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
                }
            }
        }
    }
}
