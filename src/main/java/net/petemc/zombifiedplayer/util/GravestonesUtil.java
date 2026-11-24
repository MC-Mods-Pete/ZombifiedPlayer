package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Gravestones (by Guavy): no grave when a zombified player spawns (config option gravestoneCompatibility).
 * Gravestones replaces Inventory.dropAll() on death: it places the grave, then clears all trinket slots.
 * Skipping the grave therefore has to skip both and drop the inventory like vanilla (see GravestonesMixin).
 */
public class GravestonesUtil {

    private static final Set<UUID> skipGravestoneSet = Collections.synchronizedSet(new HashSet<>());

    public static boolean isGravestonesLoaded() {
        return FabricLoader.getInstance().isModLoaded("gravestones");
    }

    /**
     * Marks this player to skip the gravestone of the current death.
     * Called before a ZombifiedPlayer is spawned.
     */
    public static void skipGravestone(Player player) {
        if (isGravestonesLoaded()) {
            skipGravestoneSet.add(player.getUUID());
        }
    }

    /** True while the current death of this player gets no gravestone. Called from GravestonesMixin. */
    public static boolean shouldSkipGravestone(UUID playerUuid) {
        return skipGravestoneSet.contains(playerUuid);
    }

    /**
     * End of the death (ServerPlayer.die()): the mark only applies to this death, also when Gravestones placed
     * no grave at all (graves turned off, keepInventory).
     */
    public static void deathFinished(Player player) {
        skipGravestoneSet.remove(player.getUUID());
    }
}
