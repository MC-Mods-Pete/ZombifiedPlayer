package net.petemc.zombifiedplayer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.entity.ModEntities;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ZombifiedPlayer.MOD_ID)
public class ZombifiedPlayer {
    public static final String MOD_ID = "zombifiedplayer";
    public static final String MOD_NAME = "ZombifiedPlayer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ConcurrentHashMap<UUID, ResourceLocation> cachedPlayerSkinsByUUID = new ConcurrentHashMap<>();

    public ZombifiedPlayer(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);
        //modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC_SERVER);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Initializing the {} Mod", MOD_NAME);
        event.enqueueWork(() -> {

        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Initializing Zombified Player Mod");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.ZOMBIFIED_PLAYER.get(), ZombifiedPlayerRenderer::new);
        }
    }
}
