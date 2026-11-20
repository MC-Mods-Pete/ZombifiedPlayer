package net.petemc.zombifiedplayer.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerClient {
    public static void processGameProfile(LocalPlayer clientPlayerEntity, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId, UUID gameProfileUuid, String gameProfileName) {
        Entity entity = clientPlayerEntity.level().getEntity(zombifiedPlayerId);

        if (entity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            zombifiedPlayerEntity.setGameProfile(new GameProfile(gameProfileUuid, gameProfileName));
        }

    }
}
