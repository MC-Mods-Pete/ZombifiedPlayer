package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
//import net.petemc.zombifiedplayer.util.StateSaverAndLoader;

public class ServerStartedEvent {

    private static MinecraftServer pServer;

    public ServerStartedEvent() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            pServer = server;
            executeServerStarted();
        });
    }

    public static void executeServerStarted() {
        //if (ZombifiedPlayer.serverState == null) {
        //    ZombifiedPlayer.serverState = pServer.getOverworld().getPersistentStateManager().getOrCreate(StateSaverAndLoader.createStateType());
        //    ZombifiedPlayer.LOGGER.info("{}: persistent state loaded.", ZombifiedPlayer.MOD_ID);
        //}
    }

    public static void registerEvents() { new ServerStartedEvent(); }
}
