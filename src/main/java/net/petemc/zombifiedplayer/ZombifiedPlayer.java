package net.petemc.zombifiedplayer;

import net.fabricmc.api.ModInitializer;

import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombifiedPlayer implements ModInitializer {
	public static final String MOD_ID = "zombifiedplayer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Zombified Player Mod");
		ZombifiedPlayerConfig.init();
	}
}