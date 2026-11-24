package net.petemc.zombifiedplayer.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerServer {
    public static void processGameProfileRequest(ServerPlayer serverPlayer, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId) {
        Entity zombifiedPlayer = serverPlayer.level().getEntity(zombifiedPlayerId);

        if (zombifiedPlayer instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            FriendlyByteBuf buf = PacketByteBufs.create();

            buf.writeUUID(zombifiedPlayerEntity.getUUID());
            buf.writeInt(zombifiedPlayerEntity.getId());
            buf.writeUUID(zombifiedPlayerEntity.getGameProfile().getId());
            buf.writeUtf(zombifiedPlayerEntity.getGameProfile().getName());
            ServerPlayNetworking.send((ServerPlayer) serverPlayer, NetworkPayloads.GAMEPROFILE_PACKET_ID, buf);
        }
    }
}
