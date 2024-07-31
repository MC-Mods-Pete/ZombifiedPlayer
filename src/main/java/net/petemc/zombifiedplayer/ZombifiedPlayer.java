package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.event.PlayerDeathEvent;
import net.petemc.zombifiedplayer.event.ServerZombifiedPlayerLoadEvent;
import net.petemc.zombifiedplayer.network.NetworkHandlerServer;
import net.petemc.zombifiedplayer.network.NetworkPayloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombifiedPlayer implements ModInitializer {
	public static final String MOD_ID = "zombifiedplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Zombified Player Mod");
		ZombifiedPlayerConfig.init();
		PlayerDeathEvent.registerEvent();
		ServerZombifiedPlayerLoadEvent.registerEvent();

		FabricDefaultAttributeRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerEntity.createZombifiedPlayerAttributes());

		PayloadTypeRegistry.playS2C().register(NetworkPayloads.GameProfilePayload.ID, NetworkPayloads.GameProfilePayload.CODEC);
		PayloadTypeRegistry.playC2S().register(NetworkPayloads.RequestGameProfilePayload.ID, NetworkPayloads.RequestGameProfilePayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(NetworkPayloads.RequestGameProfilePayload.ID, (payload, context) -> {
			context.player().server.execute(() -> {
				NetworkHandlerServer.processGameProfileRequest(context.player(), payload.entityUUID(), payload.entityID());
			});
		});
	}
}