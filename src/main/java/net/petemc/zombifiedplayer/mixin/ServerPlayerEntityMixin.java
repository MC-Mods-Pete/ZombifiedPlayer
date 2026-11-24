package net.petemc.zombifiedplayer.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.event.PlayerDeathEvents;
import net.petemc.zombifiedplayer.util.GravestonesUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    /**
     * The death is final here (totems and mods that cancel the death via ALLOW_DEATH ran before),
     * and the inventory is still full (dropAllDeathLoot comes later in die()).
     */
    @Inject(method = "die", at = @At("HEAD"))
    private void zombifiedplayer$onDeath(DamageSource source, CallbackInfo ci) {
        PlayerDeathEvents.onPlayerDeath((ServerPlayer) (Object) this, source);
    }

    /**
     * Injected before removeEntitiesOnShoulder(): runs after the vanilla death message part,
     * also when there is no broadcast (game rule showDeathMessages off, team death message visibility).
     */
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;removeEntitiesOnShoulder()V"))
    private void zombifiedplayer$spawnMessage(DamageSource source, CallbackInfo ci) {
        PlayerDeathEvents.sendSpawnMessage((ServerPlayer) (Object) this);
    }

    /** The death is over: marks that only apply to this death are removed. */
    @Inject(method = "die", at = @At("RETURN"))
    private void zombifiedplayer$deathFinished(DamageSource source, CallbackInfo ci) {
        GravestonesUtil.deathFinished((ServerPlayer) (Object) this);
    }
}
