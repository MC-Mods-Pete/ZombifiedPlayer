package net.petemc.zombifiedplayer.mixin;

import de.maxhenkel.gravestone.corelib.death.PlayerDeathEvent;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents the gravestone mod from spawning a gravestone when a ZombifiedPlayerEntity
 * has already been spawned for the dying player.
 */
@Mixin(value = de.maxhenkel.gravestone.events.DeathEvents.class, remap = false)
public class GravestoneDeathEventsMixin {

    @Inject(method = "playerDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private void onPlayerDeath(PlayerDeathEvent event, CallbackInfo ci) {
        if (ZombifiedPlayerEntity.shouldSkipGravestone(event.getPlayer().getUUID())) {
            ZombifiedPlayerEntity.removeFromSkipGravestone(event.getPlayer().getUUID());
            ci.cancel();
        }
    }
}

