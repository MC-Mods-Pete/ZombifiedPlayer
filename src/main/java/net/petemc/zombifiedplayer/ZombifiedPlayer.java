package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

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

import java.util.UUID;

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

		ServerPlayNetworking.registerGlobalReceiver(NetworkPayloads.REQEST_GAMEPROFILE_PACKET_ID, (server, player, handler, buf, responseSender) -> {
			UUID zombPlayerUuid = buf.readUuid();
			Integer zombPlayerId = buf.readInt();

			server.execute(() -> {
				NetworkHandlerServer.processGameProfileRequest(player, zombPlayerUuid, zombPlayerId);
			});
		});

	}
}