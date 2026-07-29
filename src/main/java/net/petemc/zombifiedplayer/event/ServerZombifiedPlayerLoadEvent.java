package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
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
                    if ((zombifiedPlayerEntity.getGameProfile().getId() != null) && (zombifiedPlayerEntity.getGameProfile().getName() != null)) {
                        for (ServerPlayerEntity player : PlayerLookup.world((ServerWorld) pWorld)) {
                            ServerPlayNetworking.send(player, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUuid(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.gameProfile.getId(), zombifiedPlayerEntity.gameProfile.getName()));
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
