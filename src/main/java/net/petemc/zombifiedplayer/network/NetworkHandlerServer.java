package net.petemc.zombifiedplayer.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerServer {
    public static void processGameProfileRequest(ServerPlayerEntity serverPlayer, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId) {
        Entity zombifiedPlayer = serverPlayer.getEntityWorld().getEntity(zombifiedPlayerUuid);

        if (zombifiedPlayer instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            if (zombifiedPlayerEntity.getGameProfile() != null) {
                ServerPlayNetworking.send(serverPlayer, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUuid(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.getGameProfile().id(), zombifiedPlayerEntity.getGameProfile().name()));
            } else {
                ZombifiedPlayer.LOGGER.warn("Zombified Player with UUID {} does not have a valid gameProfile!", zombifiedPlayerUuid);
            }
        }
    }
}
