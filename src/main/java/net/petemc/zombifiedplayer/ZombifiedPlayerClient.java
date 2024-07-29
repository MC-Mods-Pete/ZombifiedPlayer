package net.petemc.zombifiedplayer;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZombifiedPlayerClient implements ClientModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("template-mod");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Hello Fabric Client world!");
	}
}