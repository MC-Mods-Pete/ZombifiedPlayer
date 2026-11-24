package net.petemc.zombifiedplayer.mixin.compat;

import net.minecraft.world.entity.player.Player;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.GravestonesUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.guavy.gravestones.compat.TrinketsCompat.class, remap = false)
public class GravestonesTrinketsCompatMixin {

    /**
     * After placing the grave, Gravestones clears all trinket slots (their items are in the grave then).
     * Without a grave this would delete the trinkets that Trinkets keeps for the player (drop rule KEEP);
     * Trinkets already applied its drop rules before.
     */
    @Inject(method = "dropAll", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onDropAll(Player player, CallbackInfo ci) {
        if (MainConfig.getGravestoneCompatibility() && GravestonesUtil.shouldSkipGravestone(player.getUUID())) {
            ci.cancel();
        }
    }
}
