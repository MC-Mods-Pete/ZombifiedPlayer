package net.petemc.zombifiedplayer.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Optional Hardcore Revival integration.
 *
 * Hardcore Revival cancels the death and knocks the player out instead. If nobody rescues the player,
 * Hardcore Revival kills them with hardcorerevival:not_rescued_in_time, which has no attacker.
 * We remember the original knockout damage source and use it for the spawn check of that final death,
 * so a player knocked out by a zombie still turns into a Zombified Player.
 */
public final class HardcoreRevivalUtil {

    private static final Map<UUID, DamageSource> knockoutSources = new ConcurrentHashMap<>();
    private static boolean eventsRegistered = false;

    private HardcoreRevivalUtil() {
    }

    public static boolean isHardcoreRevivalLoaded() {
        return ModList.get().isLoaded("hardcorerevival");
    }

    /** Call once on the main thread (common setup), after all mods are constructed. */
    public static void registerEvents() {
        if (!isHardcoreRevivalLoaded() || eventsRegistered) {
            return;
        }

        try {
            HardcoreRevivalEvents.register();
            eventsRegistered = true;
        } catch (Throwable t) {
            // LinkageError etc. if the Hardcore Revival or Balm API changed
            ZombifiedPlayer.LOGGER.warn("Hardcore Revival detected, but event hook registration failed.", t);
        }
    }

    static void onKnockedOut(Player player, DamageSource source) {
        if (player instanceof ServerPlayer && source != null) {
            knockoutSources.put(player.getUUID(), source);
        }
    }

    static void onNoLongerKnockedOut(Player player) {
        if (player != null) {
            knockoutSources.remove(player.getUUID());
        }
    }

    /**
     * Returns the knockout damage source if the player was killed by Hardcore Revival's timeout,
     * otherwise the given death source.
     */
    public static DamageSource resolveEffectiveDeathSource(ServerPlayer player, DamageSource deathSource) {
        if (!isHardcoreRevivalLoaded() || player == null || deathSource == null) {
            return deathSource;
        }

        DamageSource knockoutSource = knockoutSources.get(player.getUUID());
        if (knockoutSource == null) {
            return deathSource;
        }

        return isNotRescuedInTimeDamage(deathSource) ? knockoutSource : deathSource;
    }

    public static void clearTrackedCause(ServerPlayer player) {
        if (player != null) {
            knockoutSources.remove(player.getUUID());
        }
    }

    private static boolean isNotRescuedInTimeDamage(DamageSource source) {
        return source.typeHolder().unwrapKey()
                .map(key -> "hardcorerevival:not_rescued_in_time".equals(key.location().toString()))
                .orElse(false);
    }
}
