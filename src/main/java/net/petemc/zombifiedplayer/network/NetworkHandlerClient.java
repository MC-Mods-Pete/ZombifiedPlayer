package net.petemc.zombifiedplayer.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.UUID;

public class NetworkHandlerClient {
    public static void processGameProfile(ClientPlayerEntity clientPlayerEntity, UUID zombifiedPlayerUuid, Integer zombifiedPlayerId, UUID gameProfileUuid, String gameProfileName) {
        Entity entity = clientPlayerEntity.getWorld().getEntityById(zombifiedPlayerId);

        if (entity instanceof ZombifiedPlayerEntity zombifiedPlayerEntity) {
            zombifiedPlayerEntity.setGameProfile(new GameProfile(gameProfileUuid, gameProfileName));
        }

    }
}
