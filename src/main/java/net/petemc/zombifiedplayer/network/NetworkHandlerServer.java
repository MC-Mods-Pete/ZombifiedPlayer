package net.petemc.zombifiedplayer.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerServer {
    public static void processGameProfileRequest(ServerPlayerEntity serverPlayer, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId) {
        Entity zombifiedPlayer = serverPlayer.getWorld().getEntityById(zombifiedPlayerId);

        if (zombifiedPlayer instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            PacketByteBuf buf = PacketByteBufs.create();

            buf.writeUuid(zombifiedPlayerEntity.getUuid());
            buf.writeInt(zombifiedPlayerEntity.getId());
            buf.writeUuid(zombifiedPlayerEntity.getGameProfile().getId());
            buf.writeString(zombifiedPlayerEntity.getGameProfile().getName());
            ServerPlayNetworking.send((ServerPlayerEntity) serverPlayer, NetworkPayloads.GAMEPROFILE_PACKET_ID, buf);
        }
    }
}
