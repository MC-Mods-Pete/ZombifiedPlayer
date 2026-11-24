package net.petemc.zombifiedplayer.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

public class ServerZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static Level pWorld = null;

    public ServerZombifiedPlayerLoadEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
        if (pEntity != null) {
            if (!pWorld.isClientSide()) {
                if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                    //GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(zombifiedPlayerEntity.getUuid(), pWorld);
                    if ((zombifiedPlayerEntity.getGameProfile().getId() != null) && (zombifiedPlayerEntity.getGameProfile().getName() != null)) {
                        for (ServerPlayer serverPlayer : PlayerLookup.world((ServerLevel) pWorld)) {
                            FriendlyByteBuf buf = PacketByteBufs.create();

                            buf.writeUUID(zombifiedPlayerEntity.getUUID());
                            buf.writeInt(zombifiedPlayerEntity.getId());
                            buf.writeUUID(zombifiedPlayerEntity.getGameProfile().getId());
                            buf.writeUtf(zombifiedPlayerEntity.getGameProfile().getName());
                            assert NetworkPayloads.GAMEPROFILE_PACKET_ID != null;
                            ServerPlayNetworking.send(serverPlayer, NetworkPayloads.GAMEPROFILE_PACKET_ID, buf);
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
