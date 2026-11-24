package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

public class ClientZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static Level pWorld = null;

    public ClientZombifiedPlayerLoadEvent() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            pEntity = entity;
            pWorld = world;
            execute();
        });
    }

    public static void execute() {
        if (pEntity != null) {
            if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                FriendlyByteBuf buf = PacketByteBufs.create();

                buf.writeUUID(zombifiedPlayerEntity.getUUID());
                buf.writeInt(zombifiedPlayerEntity.getId());
                assert NetworkPayloads.REQEST_GAMEPROFILE_PACKET_ID != null;
                ClientPlayNetworking.send(NetworkPayloads.REQEST_GAMEPROFILE_PACKET_ID, buf);
            }
        }
    }

    public static void registerEvent() {
        new ClientZombifiedPlayerLoadEvent();
    }
}
