package net.petemc.zombifiedplayer.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;
import net.petemc.zombifiedplayer.util.GameProfileData;
import net.petemc.zombifiedplayer.util.StateSaverAndLoader;

public class ServerZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static World pWorld = null;

    public ServerZombifiedPlayerLoadEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
        if (pEntity == null) {
            ZombifiedPlayer.LOGGER.warn("Failed to load entity!");
        } else if (pWorld.isClient()) {
            ZombifiedPlayer.LOGGER.warn("World is Client world!");
        } else if (pEntity.getWorld() == null) {
            ZombifiedPlayer.LOGGER.warn("Failed to load World!");
        } else {
            if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {

                GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(zombifiedPlayerEntity.getUuid(), pWorld);
                if ((gameProfileState.gameProfileUUID != null) && (gameProfileState.gameProfileName != null)) {
                    zombifiedPlayerEntity.gameProfile = new GameProfile(gameProfileState.gameProfileUUID, gameProfileState.gameProfileName);
                    for (ServerPlayerEntity player : PlayerLookup.world((ServerWorld) pWorld)) {
                        ServerPlayNetworking.send(player, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUuid(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.gameProfile.getId(), zombifiedPlayerEntity.gameProfile.getName()));
                    }
                }
            }

        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
