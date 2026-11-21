package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.item.ModItems;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import net.petemc.zombifiedplayer.world.NaturalSpawns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombifiedPlayer implements ModInitializer {
	public static final String MOD_ID = "zombifiedplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Zombified Player Mod");
		MainConfig.init();
		ModCompatibility.init();
		ModItems.register();
		NaturalSpawns.register();

		FabricDefaultAttributeRegistry.register(ModEntities.ZOMBIFIED_PLAYER, ZombifiedPlayerEntity.createZombifiedPlayerAttributes());
	}
}