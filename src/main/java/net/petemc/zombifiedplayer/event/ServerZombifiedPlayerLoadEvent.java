package net.petemc.zombifiedplayer.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

public class ServerZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static World pWorld = null;

    public ServerZombifiedPlayerLoadEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
        if (pEntity != null) {
            if (!pWorld.isClient()) {
                if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                    //GameProfileData gameProfileState = StateSaverAndLoader.getGameProfileState(zombifiedPlayerEntity.getUuid(), pWorld);
                    if ((zombifiedPlayerEntity.getGameProfile().getId() != null) && (zombifiedPlayerEntity.getGameProfile().getName() != null)) {
                        for (ServerPlayerEntity serverPlayer : PlayerLookup.world((ServerWorld) pWorld)) {
                            PacketByteBuf buf = PacketByteBufs.create();

                            buf.writeUuid(zombifiedPlayerEntity.getUuid());
                            buf.writeInt(zombifiedPlayerEntity.getId());
                            buf.writeUuid(zombifiedPlayerEntity.getGameProfile().getId());
                            buf.writeString(zombifiedPlayerEntity.getGameProfile().getName());
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
