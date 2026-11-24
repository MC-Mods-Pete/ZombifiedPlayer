package net.petemc.zombifiedplayer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.loading.ClientModLoader;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.resource.PathPackResources;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.nio.file.Path;

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
        PathPackResources resources = new PathPackResources(SHAMBLING_ANIMATION_ID, path);
        PackMetadataSection metadata;
        try {
            metadata = resources.getMetadataSection(PackMetadataSection.SERIALIZER);
        } catch (Exception e) {
            metadata = null;
        }
        if (metadata == null) {
            ZombifiedPlayer.LOGGER.warn("Built-in resource pack {} not found", SHAMBLING_ANIMATION);
            return;
        }
        PackMetadataSection packMetadata = metadata;
        // 1.19.2: repository sources get the pack constructor of the repository
        event.addRepositorySource((consumer, constructor) -> consumer.accept(constructor.create(SHAMBLING_ANIMATION_ID,
                Component.translatable("pack." + ZombifiedPlayer.MOD_ID + "." + SHAMBLING_ANIMATION), false,
                () -> resources, packMetadata, Pack.Position.TOP, PackSource.BUILT_IN, false)));
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!saveOptions || event.phase != TickEvent.Phase.END || ClientModLoader.isLoading()) {
            return;
        }
        saveOptions = false;
        Minecraft.getInstance().options.save();
        MainConfig.setShamblingAnimationPackTurnedOn();
    }
}
