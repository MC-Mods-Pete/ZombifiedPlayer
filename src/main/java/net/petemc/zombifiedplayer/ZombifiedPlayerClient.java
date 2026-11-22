package net.petemc.zombifiedplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.client.render.ModModelLayers;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.entity.ModEntities;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZombifiedPlayerClient implements ClientModInitializer {

	public static ConcurrentHashMap<UUID, Identifier> cachedPlayerSkinsByUUID = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, Identifier> cachedPlayerSkinsByName = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, UUID> uuidMissmatches = new ConcurrentHashMap<>();

	@Override
	public void onInitializeClient() {
		ModModelLayers.register();
		EntityRendererRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerRenderer::new);
	}
}