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

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerRenderer::new);

		ClientZombifiedPlayerLoadEvent.registerEvent();

		ClientPlayNetworking.registerGlobalReceiver(NetworkPayloads.GameProfilePayload.ID, (payload, context) -> {
			context.client().execute(() -> {
				NetworkHandlerClient.processGameProfile(context.player(), payload.entityUUID(), payload.entityID(), payload.playerUUID(), payload.name());
			});
		});
	}
}