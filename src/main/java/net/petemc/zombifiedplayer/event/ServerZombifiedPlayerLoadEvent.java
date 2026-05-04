package net.petemc.zombifiedplayer.event;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.network.NetworkPayloads;

import java.util.UUID;

public class ServerZombifiedPlayerLoadEvent {

    private static Entity pEntity = null;
    private static Level pLevel = null;

    public ServerZombifiedPlayerLoadEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            pEntity = entity;
            pLevel = level;
            execute();
        });
    }

    public static void execute() {
        if (pEntity != null) {
            if (!pLevel.isClientSide()) {
                if (pEntity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
                    if ((zombifiedPlayerEntity.getGameProfile().id() != null) && (zombifiedPlayerEntity.getGameProfile().name() != null)) {
                        for (ServerPlayer player : PlayerLookup.level((ServerLevel) pLevel)) {
                            ServerPlayNetworking.send(player, new NetworkPayloads.GameProfilePayload(zombifiedPlayerEntity.getUUID(), zombifiedPlayerEntity.getId(), zombifiedPlayerEntity.gameProfile.id(), zombifiedPlayerEntity.gameProfile.name()));
                        }
                    }
                }
            }
        }
    }

    public static void registerEvent() { new ServerZombifiedPlayerLoadEvent(); }
}
