package net.petemc.zombifiedplayer.mixin;

import de.maxhenkel.corpse.corelib.death.PlayerDeathEvent;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents the corpse mod from spawning a corpse when a ZombifiedPlayerEntity
 * has already been spawned for the dying player.
 */
@Mixin(value = de.maxhenkel.corpse.events.DeathEvents.class, remap = false)
public class CorpseDeathEventsMixin {

    @Inject(method = "playerDeath", at = @At("HEAD"), cancellable = true, remap = false)
    private void onPlayerDeath(PlayerDeathEvent event, CallbackInfo ci) {
        if (ZombifiedPlayerEntity.shouldSkipCorpse(event.getPlayer().getUUID())) {
            ZombifiedPlayerEntity.removeFromSkipCorpse(event.getPlayer().getUUID());
            ci.cancel();
        }
    }
}

