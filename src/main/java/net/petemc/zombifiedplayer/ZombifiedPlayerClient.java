package net.petemc.zombifiedplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.event.ClientZombifiedPlayerLoadEvent;
import net.petemc.zombifiedplayer.network.NetworkHandlerClient;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZombifiedPlayerClient implements ClientModInitializer {

	public static ConcurrentHashMap<UUID, Identifier> cachedPlayerSkinsByUUID = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Identifier> cachedPlayerSkinsByName = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, UUID> uuidMissmatches = new ConcurrentHashMap<>();

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerRenderer::new);
		ClientZombifiedPlayerLoadEvent.registerEvent();

        assert NetworkPayloads.GAMEPROFILE_PACKET_ID != null;
        ClientPlayNetworking.registerGlobalReceiver(NetworkPayloads.GAMEPROFILE_PACKET_ID, (client, handler, buf, responseSender) -> {
			UUID zombifiedPlayerUuid = buf.readUuid();
			Integer zombifiedPlayerId = buf.readInt();
			UUID gameProfileUuid = buf.readUuid();
			String gameProfileName = buf.readString();

			client.execute(() -> {
                assert client.player != null;
                NetworkHandlerClient.processGameProfile(client.player, zombifiedPlayerUuid, zombifiedPlayerId, gameProfileUuid, gameProfileName);
			});
		});
	}
}