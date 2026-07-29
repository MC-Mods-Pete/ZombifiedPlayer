package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.pneumono.gravestones.api.CancelGravestonePlacementCallback;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PneumonoGravestonesUtil {

    private static final Set<UUID> skipGraveSet = Collections.synchronizedSet(new HashSet<>());

    public static boolean isPneumonoGravestonesLoaded() {
        return FabricLoader.getInstance().isModLoaded("gravestones");
    }

    /**
     * Registers the CancelGravestonePlacementCallback to block gravestone creation for marked players.
     * Should be called during mod initialization if Pneumono Gravestones is loaded.
     */
    public static void registerEvent() {
        CancelGravestonePlacementCallback.EVENT.register((world, player, damageSource) ->
                MainConfig.getGravestoneCompatibility() && skipGraveSet.remove(player.getUuid())
        );
    }

    /**
     * Marks this player to skip Pneumono Gravestones gravestone creation on next death.
     * Called before a ZombifiedPlayer is spawned.
     */
    public static void skipGrave(PlayerEntity player) {
        if (isPneumonoGravestonesLoaded()) {
            skipGraveSet.add(player.getUuid());
        }
    }
}


