package net.petemc.zombifiedplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Resource pack inside the mod jar (resourcepacks/), listed under Options > Resource Packs:
 * our own zombie animation for Entity Model Features. Turned on once at the first start, after that the player decides.
 */
@OnlyIn(Dist.CLIENT)
public final class BuiltinPacks {
    private static final String SHAMBLING_ANIMATION = "shambling_animation";
    public static final String SHAMBLING_ANIMATION_ID = ZombifiedPlayer.MOD_ID + "/" + SHAMBLING_ANIMATION;

    /** Turned on in this start, options.txt still has to be saved (not possible while mods are loading). */
    private static boolean saveOptions = false;

    private BuiltinPacks() {
    }

    public static void register(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        Path path = ModList.get().getModFileById(ZombifiedPlayer.MOD_ID).getFile().findResource("resourcepacks", SHAMBLING_ANIMATION);
        PackLocationInfo info = new PackLocationInfo(SHAMBLING_ANIMATION_ID,
                Component.translatable("pack." + ZombifiedPlayer.MOD_ID + "." + SHAMBLING_ANIMATION), PackSource.BUILT_IN, Optional.empty());
        Pack pack = Pack.readMetaAndCreate(info, new PathPackResources.PathResourcesSupplier(path), PackType.CLIENT_RESOURCES,
                new PackSelectionConfig(false, Pack.Position.TOP, false));
        if (pack == null) {
            ZombifiedPlayer.LOGGER.warn("Built-in resource pack {} not found", SHAMBLING_ANIMATION);
            return;
        }
        event.addRepositorySource(consumer -> consumer.accept(pack));
    }

    /**
     * Called right before the game selects the resource packs from options.txt (Minecraft constructor):
     * adds the pack once, so it is on by default but stays off once the player turned it off.
     */
    public static void turnOnOnce(Options options, PackRepository repository) {
        if (MainConfig.getShamblingAnimationPackTurnedOn() || repository.getPack(SHAMBLING_ANIMATION_ID) == null) {
            return;
        }
        if (!options.resourcePacks.contains(SHAMBLING_ANIMATION_ID)) {
            options.resourcePacks.add(SHAMBLING_ANIMATION_ID);
            ZombifiedPlayer.LOGGER.info("Turned on the built-in resource pack {}", SHAMBLING_ANIMATION_ID);
        }
        saveOptions = true;
    }

    /**
     * Saves options.txt with the pack and remembers that it was turned on, once mod loading is done
     * (saving earlier would drop the key bindings of mods that are not registered yet).
     */
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!saveOptions || ClientModLoader.isLoading()) {
            return;
        }
        saveOptions = false;
        Minecraft.getInstance().options.save();
        MainConfig.setShamblingAnimationPackTurnedOn();
    }
}
