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

import java.util.UUID;

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
        if (pEntity != null) {
            if (!pWorld.isClient()) {
                if (ZombifiedPlayer.serverState == null) {
                    ZombifiedPlayer.LOGGER.warn("Persistant state still null!");
                }
                if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                    if (ZombifiedPlayer.serverState.gameProfiles.containsKey(zombifiedPlayerEntity.getUuid())) {
                        String gameProfileCombo = ZombifiedPlayer.serverState.gameProfiles.get(zombifiedPlayerEntity.getUuid());
                        String[] strArray = gameProfileCombo.split(":");
                        UUID uuid = UUID.fromString(strArray[0]);
                        if ((strArray[0] != null) && (strArray[1] != null)) {
                            zombifiedPlayerEntity.gameProfile = new GameProfile(uuid, strArray[1]);
                            for (ServerPlayerEntity player : PlayerLookup.world((ServerWorld) pWorld)) {
                                ServerPlayNetworking.send(player, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUuid(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.gameProfile.id(), zombifiedPlayerEntity.gameProfile.name()));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
