package net.petemc.zombifiedplayer.mixin.client;

import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.PackRepository;
import net.petemc.zombifiedplayer.client.BuiltinPacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Turns the built-in shambling animation pack on at the first start (see {@link BuiltinPacks#turnOnOnce}). */
@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "loadSelectedResourcePacks", at = @At("HEAD"))
    private void zombifiedplayer$turnOnBuiltinPacks(PackRepository repository, CallbackInfo ci) {
        BuiltinPacks.turnOnOnce((Options) (Object) this, repository);
    }
}
