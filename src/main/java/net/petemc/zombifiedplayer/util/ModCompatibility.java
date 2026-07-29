package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.petemc.contagion.casts.InfectedPlayer;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModCompatibility {
    public static void init() {
        if (isContagionLoaded()) {
            ZombifiedPlayer.LOGGER.info("Contagion mod detected. Death by Infection can spawn Zombified Players.");
        }
        if (isUndeadNightsLoaded()) {
            ZombifiedPlayer.LOGGER.info("Undead Nights mod detected. Death by a Horde Zombie will spawn a Zombified Player.");
        }
        if (TrinketsUtil.isTrinketsLoaded()) {
            ZombifiedPlayer.LOGGER.info("Trinkets API detected. If enabled trinket items will be transferred to the Zombified Player.");
        }
        if (UniversalGravesUtil.isUniversalGravesLoaded()) {
            UniversalGravesUtil.registerEvent();
            ZombifiedPlayer.LOGGER.info("Universal Graves mod detected. Graves will be suppressed when a Zombified Player spawns.");
        }
        if (PneumonoGravestonesUtil.isPneumonoGravestonesLoaded()) {
            PneumonoGravestonesUtil.registerEvent();
            ZombifiedPlayer.LOGGER.info("Pneumono Gravestones mod detected. Gravestones will be suppressed when a Zombified Player spawns.");
        }
    }

    public static boolean isContagionLoaded() {
        return FabricLoader.getInstance().isModLoaded("contagion");
    }

    public static boolean diedFromInfection(ServerPlayerEntity serverPlayer) {
        if (isContagionLoaded()) {
            if (serverPlayer instanceof InfectedPlayer infectedPlayer) {
                return infectedPlayer.contagion_playerDiedFromInfection();
            }
        }
        return false;
    }

    public static boolean isUndeadNightsLoaded() {
        return FabricLoader.getInstance().isModLoaded("undeadnights");
    }

    public static boolean wasKilledByHordeZombie(LivingEntity pAttacker) {
        if (isUndeadNightsLoaded()) {
            return (pAttacker instanceof HordeZombieEntity) || (pAttacker instanceof EliteZombieEntity);
        }
        return false;
    }
}
