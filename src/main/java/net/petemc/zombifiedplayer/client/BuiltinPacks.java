package net.petemc.zombifiedplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

/**
 * Resource pack inside the mod jar (resourcepacks/), listed under Options > Resource Packs:
 * our own zombie animation for Entity Model Features. Turned on once at the first start, after that the player decides.
 */
public final class BuiltinPacks {
    /** Turned on in this start, options.txt still has to be saved (not possible while mods are loading). */
    private static boolean saveOptions = false;
    /** All mods are loaded (key bindings registered). Not ClientModLoader.isLoading(): removed in NeoForge 26.1.2. */
    private static volatile boolean loadComplete = false;

    private BuiltinPacks() {
    }

    public static void register(AddPackFindersEvent event) {
        event.addPackFinders(BuiltinPackSelection.SHAMBLING_ANIMATION_LOCATION, PackType.CLIENT_RESOURCES,
                Component.translatable("pack." + ZombifiedPlayer.MOD_ID + "." + BuiltinPackSelection.SHAMBLING_ANIMATION),
                PackSource.BUILT_IN, false, Pack.Position.TOP);
    }

    /**
     * Called right before the game selects the resource packs from options.txt (Minecraft constructor):
     * adds the pack once, so it is on by default but stays off once the player turned it off.
     */
    public static void turnOnOnce(Options options, PackRepository repository) {
        if (BuiltinPackSelection.turnOnOnce(options.resourcePacks, repository.getPack(BuiltinPackSelection.SHAMBLING_ANIMATION_ID) != null,
                MainConfig.getShamblingAnimationPackTurnedOn())) {
            saveOptions = true;
        }
    }

    /**
     * Saves options.txt with the pack and remembers that it was turned on, once mod loading is done
     * (saving earlier would drop the key bindings of mods that are not registered yet).
     */
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        loadComplete = true;
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (!saveOptions || !loadComplete) {
            return;
        }
        saveOptions = false;
        Minecraft.getInstance().options.save();
        MainConfig.setShamblingAnimationPackTurnedOn();
    }
}
