package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
                    if ((zombifiedPlayerEntity.getGameProfile().getId() != null) && (zombifiedPlayerEntity.getGameProfile().getName() != null)) {
                        for (ServerPlayer player : PlayerLookup.world((ServerLevel) pWorld)) {
                            ServerPlayNetworking.send(player, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUUID(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.gameProfile.getId(), zombifiedPlayerEntity.gameProfile.getName()));
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
