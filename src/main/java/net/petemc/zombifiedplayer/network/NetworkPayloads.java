package net.petemc.zombifiedplayer.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.UUID;

public class NetworkPayloads {
    public static final Identifier GAMEPROFILE_PACKET_ID = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "transmit_gameprofile");
    public static final Identifier REQEST_GAMEPROFILE_PACKET_ID = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "request_gameprofile");

    public record GameProfilePayload(UUID entityUUID, Integer entityID, UUID playerUUID, String name) implements CustomPacketPayload {
        public static final Type<GameProfilePayload> ID = new Type<>(GAMEPROFILE_PACKET_ID);
        public static final StreamCodec<ByteBuf, GameProfilePayload> CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, GameProfilePayload::entityUUID,
                        ByteBufCodecs.VAR_INT, GameProfilePayload::entityID,
                        UUIDUtil.STREAM_CODEC, GameProfilePayload::playerUUID,
                        ByteBufCodecs.STRING_UTF8, GameProfilePayload::name,
                        GameProfilePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

    public record RequestGameProfilePayload(UUID entityUUID, Integer entityID) implements CustomPacketPayload {
        public static final Type<RequestGameProfilePayload> ID = new Type<RequestGameProfilePayload>(REQEST_GAMEPROFILE_PACKET_ID);
        public static final StreamCodec<ByteBuf, RequestGameProfilePayload> CODEC =
                StreamCodec.composite(
                        UUIDUtil.STREAM_CODEC, RequestGameProfilePayload::entityUUID,
                        ByteBufCodecs.VAR_INT, RequestGameProfilePayload::entityID,
                        RequestGameProfilePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }

}
