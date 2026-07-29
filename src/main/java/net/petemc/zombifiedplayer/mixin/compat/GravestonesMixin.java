package net.petemc.zombifiedplayer.mixin.compat;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.GravestonesUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.guavy.gravestones.Gravestones.class, remap = false)
public class GravestonesMixin {

    @Inject(method = "placeGrave", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onPlaceGrave(World world, Vec3d pos, PlayerEntity player, CallbackInfo ci) {
        if (MainConfig.getGravestoneCompatibility() && GravestonesUtil.shouldSkipGravestone(player.getUuid())) {
            ci.cancel();
        }
    }
}

