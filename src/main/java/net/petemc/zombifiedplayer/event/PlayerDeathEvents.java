package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.HardcoreRevivalUtil;
import net.petemc.zombifiedplayer.util.PneumonoGravestonesUtil;
import net.petemc.zombifiedplayer.util.UniversalGravesUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

public class PlayerDeathEvents {

    public PlayerDeathEvents() {
        // Spawn happens here — before dropAllDeathLoot() — so the inventory is still full.
        // Chat notification is handled by ServerPlayerEntityMixin (after broadcastSystemMessage).
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            if (!entity.level().isClientSide()
                    && entity instanceof ServerPlayer serverPlayer) {
                DamageSource effectiveDamageSource = HardcoreRevivalUtil.resolveEffectiveDeathSource(serverPlayer, damageSource);
                boolean shouldSpawn = ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, effectiveDamageSource);

                // Final death reached: stale knockout sources must be dropped either way.
                HardcoreRevivalUtil.clearTrackedCause(serverPlayer);

                if (shouldSpawn) {
                    UniversalGravesUtil.skipGrave(serverPlayer);
                    PneumonoGravestonesUtil.skipGrave(serverPlayer);
                    ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
                }
            }
            return true;
        });
    }

    public static void registerEvent() {
        new PlayerDeathEvents();
    }
}
