package net.petemc.zombifiedplayer.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.UUID;

public class NetworkPayloads {
    public static final Identifier GAMEPROFILE_PACKET_ID = Identifier.of(ZombifiedPlayer.MOD_ID, "transmit_gameprofile");
    public static final Identifier REQEST_GAMEPROFILE_PACKET_ID = Identifier.of(ZombifiedPlayer.MOD_ID, "request_gameprofile");

    public record GameProfilePayload(UUID entityUUID, Integer entityID, UUID playerUUID, String name) implements CustomPayload {
        public static final Id<GameProfilePayload> ID = new Id<>(GAMEPROFILE_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, GameProfilePayload> CODEC =
                PacketCodec.tuple(
                        Uuids.PACKET_CODEC, GameProfilePayload::entityUUID,
                        PacketCodecs.INTEGER, GameProfilePayload::entityID,
                        Uuids.PACKET_CODEC, GameProfilePayload::playerUUID,
                        PacketCodecs.STRING, GameProfilePayload::name, GameProfilePayload::new);


        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    public record RequestGameProfilePayload(UUID entityUUID, Integer entityID) implements CustomPayload {
        public static final Id<RequestGameProfilePayload> ID = new Id<>(REQEST_GAMEPROFILE_PACKET_ID);
        public static final PacketCodec<RegistryByteBuf, RequestGameProfilePayload> CODEC =
                PacketCodec.tuple(
                        Uuids.PACKET_CODEC, RequestGameProfilePayload::entityUUID,
                        PacketCodecs.INTEGER, RequestGameProfilePayload::entityID, RequestGameProfilePayload::new);


        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

}
