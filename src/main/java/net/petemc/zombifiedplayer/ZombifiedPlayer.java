package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.event.ServerStartedEvent;
import net.petemc.zombifiedplayer.event.PlayerDeathEvents;
import net.petemc.zombifiedplayer.event.ServerZombifiedPlayerLoadEvent;
import net.petemc.zombifiedplayer.network.NetworkHandlerServer;
import net.petemc.zombifiedplayer.network.NetworkPayloads;
import net.petemc.zombifiedplayer.util.ModCompatibility;
//import net.petemc.zombifiedplayer.util.StateSaverAndLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class ZombifiedPlayer implements ModInitializer {
	public static final String MOD_ID = "zombifiedplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//public static StateSaverAndLoader serverState = null;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Zombified Player Mod");
		MainConfig.init();
		PlayerDeathEvents.registerEvent();
		ServerZombifiedPlayerLoadEvent.registerEvent();
		ServerStartedEvent.registerEvents();
		ModCompatibility.init();

		FabricDefaultAttributeRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerEntity.createAttributes());

		PayloadTypeRegistry.clientboundPlay().register(NetworkPayloads.GameProfilePayload.ID, NetworkPayloads.GameProfilePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(NetworkPayloads.RequestGameProfilePayload.ID, NetworkPayloads.RequestGameProfilePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(NetworkPayloads.RequestGameProfilePayload.ID, (payload, context) -> {
			Objects.requireNonNull(context.player().level().getServer()).execute(() -> {
				NetworkHandlerServer.processGameProfileRequest(context.player(), payload.entityUUID(), payload.entityID());
			});
		});
	}
}