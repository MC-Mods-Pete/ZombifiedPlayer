package net.petemc.zombifiedplayer.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerServer {
    public static void processGameProfileRequest(ServerPlayer serverPlayer, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId) {
        Entity zombifiedPlayer = serverPlayer.level().getEntity(zombifiedPlayerId);

        if (zombifiedPlayer instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            ServerPlayNetworking.send(serverPlayer, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUUID(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.getGameProfile().getId(), zombifiedPlayerEntity.getGameProfile().getName()));
        }
    }
}
