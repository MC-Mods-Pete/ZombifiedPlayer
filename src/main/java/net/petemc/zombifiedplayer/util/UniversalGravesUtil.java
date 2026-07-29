package net.petemc.zombifiedplayer.util;

import eu.pb4.graves.event.PlayerGraveCreationEvent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UniversalGravesUtil {

    private static final Set<UUID> skipGraveSet = Collections.synchronizedSet(new HashSet<>());

    public static boolean isUniversalGravesLoaded() {
        return FabricLoader.getInstance().isModLoaded("universal-graves");
    }

    /**
     * Registers the PlayerGraveCreationEvent to block grave creation for marked players.
     * Should be called during mod initialization if Universal Graves is loaded.
     */
    public static void registerEvent() {
        PlayerGraveCreationEvent.EVENT.register(player -> {
            if (MainConfig.getGravestoneCompatibility() && skipGraveSet.remove(player.getUuid())) {
                return PlayerGraveCreationEvent.CreationResult.BLOCK_SILENT;
            }
            return PlayerGraveCreationEvent.CreationResult.ALLOW;
        });
    }

    /**
     * Marks this player to skip Universal Graves grave creation on next death.
     * Called before a ZombifiedPlayer is spawned.
     */
    public static void skipGrave(PlayerEntity player) {
        if (isUniversalGravesLoaded()) {
            skipGraveSet.add(player.getUuid());
        }
    }
}

