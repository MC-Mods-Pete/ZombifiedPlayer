package net.petemc.zombifiedplayer;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.petemc.zombifiedplayer.client.BuiltinPacks;
import net.petemc.zombifiedplayer.client.render.ModModelLayers;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ZombifiedPlayer.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID, value = Dist.CLIENT)
public class ZombifiedPlayerClient {

    public ZombifiedPlayerClient(ModContainer modContainer) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        // client config: remembers that the built-in shambling animation pack was turned on once
        modContainer.registerConfig(ModConfig.Type.CLIENT, MainConfig.SPEC_CLIENT);
        NeoForge.EVENT_BUS.addListener(BuiltinPacks::onClientTick);
    }

    @SubscribeEvent
    static void onAddPackFinders(AddPackFindersEvent event) {
        BuiltinPacks.register(event);
    }

    @SubscribeEvent
    static void onLoadComplete(FMLLoadCompleteEvent event) {
        BuiltinPacks.onLoadComplete(event);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(ModEntities.ZOMBIFIED_PLAYER.get(), ZombifiedPlayerRenderer::new);
    }

    @SubscribeEvent
    static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModModelLayers.register(event);
    }
}
