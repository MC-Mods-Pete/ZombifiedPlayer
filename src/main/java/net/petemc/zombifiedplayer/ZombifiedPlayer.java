package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.zombifiedplayer.config.Config;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.event.PlayerDeathEvents;
import net.petemc.zombifiedplayer.event.ServerZombifiedPlayerLoadEvent;
import net.petemc.zombifiedplayer.network.NetworkHandlerServer;
import net.petemc.zombifiedplayer.network.NetworkPayloads;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class ZombifiedPlayer implements ModInitializer {
	public static final String MOD_ID = "zombifiedplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Zombified Player Mod");
		Config.init();
		PlayerDeathEvents.registerEvent();
		ServerZombifiedPlayerLoadEvent.registerEvent();
		ModCompatibility.init();

		FabricDefaultAttributeRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerEntity.createZombifiedPlayerAttributes());

        assert NetworkPayloads.REQEST_GAMEPROFILE_PACKET_ID != null;
        ServerPlayNetworking.registerGlobalReceiver(NetworkPayloads.REQEST_GAMEPROFILE_PACKET_ID, (server, serverPlayer, handler, buf, responseSender) -> {
			UUID zombifiedPlayerUuid = buf.readUuid();
			Integer zombifiedPlayerId = buf.readInt();

			server.execute(() -> {
				NetworkHandlerServer.processGameProfileRequest(serverPlayer, zombifiedPlayerUuid, zombifiedPlayerId);
			});
		});

	}
}