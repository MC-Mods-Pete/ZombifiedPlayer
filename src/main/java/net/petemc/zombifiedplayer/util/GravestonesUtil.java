package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GravestonesUtil {

    private static final Set<UUID> skipGravestoneSet = Collections.synchronizedSet(new HashSet<>());

    public static boolean isGravestonesLoaded() {
        return FabricLoader.getInstance().isModLoaded("gravestones");
    }

    /**
     * Marks this player to skip gravestone creation on next death.
     * Called before a ZombifiedPlayer is spawned.
     */
    public static void skipGravestone(Player player) {
        if (isGravestonesLoaded()) {
            skipGravestoneSet.add(player.getUUID());
        }
    }

    /**
     * Checks if this player's gravestone should be skipped, and removes them from the set.
     * Called from the GravestonesMixin.
     */
    public static boolean shouldSkipGravestone(UUID playerUuid) {
        return skipGravestoneSet.remove(playerUuid);
    }
}

