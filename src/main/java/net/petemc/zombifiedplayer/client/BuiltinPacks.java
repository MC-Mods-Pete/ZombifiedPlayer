package net.petemc.zombifiedplayer.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.ClientConfig;

/**
 * Resource pack inside the mod jar (resourcepacks/), listed under Options > Resource Packs:
 * our own zombie animation for Entity Model Features. Turned on once at the first start, after that the player decides.
 * Not with Fabric's DEFAULT_ENABLED: Fabric remembers the pack as handled at once, but vanilla saves options.txt only
 * when an option is changed, so without a save in the first session the pack would be off at the second start.
 */
@Environment(EnvType.CLIENT)
public final class BuiltinPacks {
    /** Turned on in this start, options.txt still has to be saved (done on the first client tick). */
    private static boolean saveOptions = false;

    private BuiltinPacks() {
    }

    public static void register() {
        boolean registered = FabricLoader.getInstance().getModContainer(ZombifiedPlayer.MOD_ID)
                .map(container -> ResourceLoader.registerBuiltinPack(BuiltinPackSelection.SHAMBLING_ANIMATION_LOCATION, container,
                        Component.translatable("pack." + ZombifiedPlayer.MOD_ID + "." + BuiltinPackSelection.SHAMBLING_ANIMATION),
                        PackActivationType.NORMAL))
                .orElse(false);
        if (!registered) {
            ZombifiedPlayer.LOGGER.warn("Built-in resource pack {} not found", BuiltinPackSelection.SHAMBLING_ANIMATION);
        }
        ClientTickEvents.END_CLIENT_TICK.register(BuiltinPacks::onClientTick);
    }

    /**
     * Called right before the game selects the resource packs from options.txt (Minecraft constructor):
     * adds the pack once, so it is on by default but stays off once the player turned it off.
     */
    public static void turnOnOnce(Options options, PackRepository repository) {
        if (BuiltinPackSelection.turnOnOnce(options.resourcePacks, repository.getPack(BuiltinPackSelection.SHAMBLING_ANIMATION_ID) != null,
                ClientConfig.getShamblingAnimationPackTurnedOn())) {
            saveOptions = true;
        }
    }

    /** Saves options.txt with the pack and remembers that it was turned on (all key bindings are registered by now). */
    private static void onClientTick(Minecraft minecraft) {
        if (!saveOptions) {
            return;
        }
        saveOptions = false;
        minecraft.options.save();
        ClientConfig.setShamblingAnimationPackTurnedOn();
    }
}
