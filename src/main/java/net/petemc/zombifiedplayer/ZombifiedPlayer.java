package net.petemc.zombifiedplayer;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.petemc.zombifiedplayer.client.BuiltinPacks;
import net.petemc.zombifiedplayer.client.render.ModModelLayers;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.item.ModItems;
import net.petemc.zombifiedplayer.util.HardcoreRevivalUtil;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import net.petemc.zombifiedplayer.world.NaturalSpawnsBiomeModifier;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ZombifiedPlayer.MOD_ID)
public class ZombifiedPlayer
{
    public static final String MOD_ID = "zombifiedplayer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZombifiedPlayer(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        NaturalSpawnsBiomeModifier.register(modEventBus);
        modEventBus.addListener(this::registerSpawnPlacements);

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);

        context.registerConfig(ModConfig.Type.SERVER, MainConfig.SPEC_SERVER);
        context.registerConfig(ModConfig.Type.CLIENT, MainConfig.SPEC_CLIENT);
        ModCompatibility.init();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // main thread, all mods are constructed
        event.enqueueWork(HardcoreRevivalUtil::registerEvents);
    }

    /** Same spawn rules as zombies: on the ground, in the dark (natural spawning, spawners). */
    private void registerSpawnPlacements(SpawnPlacementRegisterEvent event)
    {
        event.register(ModEntities.ZOMBIFIED_PLAYER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.ZOMBIFIED_PLAYER_SPAWN_EGG);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Initializing Zombified Player Mod");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            MinecraftForge.EVENT_BUS.addListener(BuiltinPacks::onClientTick);
            EntityRenderers.register(ModEntities.ZOMBIFIED_PLAYER.get(), ZombifiedPlayerRenderer::new);
        }

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            ModModelLayers.register(event);
        }

        @SubscribeEvent
        public static void onAddPackFinders(AddPackFindersEvent event)
        {
            BuiltinPacks.register(event);
        }
    }
}
