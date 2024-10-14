package net.petemc.zombifiedplayer.network;

import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class NetworkPayloads {
    public static final Identifier GAMEPROFILE_PACKET_ID = Identifier.of(ZombifiedPlayer.MOD_ID, "transmit_gameprofile");
    public static final Identifier REQEST_GAMEPROFILE_PACKET_ID = Identifier.of(ZombifiedPlayer.MOD_ID, "request_gameprofile");
}
