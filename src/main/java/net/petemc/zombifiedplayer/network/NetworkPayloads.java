package net.petemc.zombifiedplayer.network;

import net.minecraft.resources.ResourceLocation;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class NetworkPayloads {
    public static final ResourceLocation GAMEPROFILE_PACKET_ID = ResourceLocation.tryBuild(ZombifiedPlayer.MOD_ID, "transmit_gameprofile");
    public static final ResourceLocation REQEST_GAMEPROFILE_PACKET_ID = ResourceLocation.tryBuild(ZombifiedPlayer.MOD_ID, "request_gameprofile");
}
