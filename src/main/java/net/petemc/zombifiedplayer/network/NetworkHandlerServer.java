package net.petemc.zombifiedplayer.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerServer {
    public static void processGameProfileRequest(ServerPlayerEntity serverPlayer, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId) {
        Entity zombifiedPlayer = serverPlayer.getWorld().getEntityById(zombifiedPlayerId);

        if (zombifiedPlayer instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            ServerPlayNetworking.send(serverPlayer, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUuid(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.getGameProfile().getId(), zombifiedPlayerEntity.getGameProfile().getName()));
        }
    }
}
