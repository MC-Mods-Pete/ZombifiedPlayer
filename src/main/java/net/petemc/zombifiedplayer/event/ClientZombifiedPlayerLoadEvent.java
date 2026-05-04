package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

public class ClientZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static Level pLevel = null;

    public ClientZombifiedPlayerLoadEvent() {
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            pEntity = entity;
            pLevel = level;
            execute();
        });
    }

    public static void execute() {
        if (pEntity != null) {
            if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                ClientPlayNetworking.send(new NetworkPayloads.RequestGameProfilePayload(zombifiedPlayerEntity.getUUID(), zombifiedPlayerEntity.getId()));
            }
        }
    }

    public static void registerEvent() {
        new ClientZombifiedPlayerLoadEvent();
    }
}
