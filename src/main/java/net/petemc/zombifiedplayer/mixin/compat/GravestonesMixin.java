package net.petemc.zombifiedplayer.mixin.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.GravestonesUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = net.guavy.gravestones.Gravestones.class, remap = false)
public class GravestonesMixin {

    /**
     * Gravestones calls this instead of Inventory.dropAll(). Without a grave, what is left in the inventory
     * (items not transferred to the zombified player) drops like in vanilla.
     */
    @Inject(method = "placeGrave", at = @At("HEAD"), cancellable = true, require = 0)
    private static void onPlaceGrave(Level world, Vec3 pos, Player player, CallbackInfo ci) {
        if (MainConfig.getGravestoneCompatibility() && GravestonesUtil.shouldSkipGravestone(player.getUUID())) {
            player.getInventory().dropAll();
            ci.cancel();
        }
    }
}
