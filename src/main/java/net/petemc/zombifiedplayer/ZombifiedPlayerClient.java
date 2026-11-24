package net.petemc.zombifiedplayer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.petemc.zombifiedplayer.client.BuiltinPacks;
import net.petemc.zombifiedplayer.client.render.ModModelLayers;
import net.petemc.zombifiedplayer.config.ClientConfig;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerRenderer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ZombifiedPlayerClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientConfig.init();
		BuiltinPacks.register();
		ModModelLayers.register();
		EntityRendererRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerRenderer::new);
	}
}