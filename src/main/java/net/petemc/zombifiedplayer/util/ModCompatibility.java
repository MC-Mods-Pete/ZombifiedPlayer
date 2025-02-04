package net.petemc.zombifiedplayer.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.petemc.contagion.casts.InfectedPlayer;
import net.petemc.undeadnights.entity.EliteZombieEntity;
import net.petemc.undeadnights.entity.HordeZombieEntity;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModCompatibility {
    public static void init() {
        if (contagionDetected()) {
            ZombifiedPlayer.LOGGER.info("Contagion detected. Death by Infection can spawn Zombified Players.");
        }
        if (undeadNightsDetected()) {
            ZombifiedPlayer.LOGGER.info("Undead Nights mod detected. Death by a Horde Zombie will spawn a Zombified Players.");
        }
    }

    public static boolean contagionDetected() {
        return ModList.get().isLoaded("contagion");
    }

    public static boolean diedFromInfection(ServerPlayer serverPlayer) {
        if (contagionDetected()) {
            if (serverPlayer instanceof InfectedPlayer infectedPlayer) {
                return infectedPlayer.contagion_playerDiedFromInfection();
            }
        }
        return false;
    }

    public static boolean undeadNightsDetected() {
        return ModList.get().isLoaded("undeadnights");
    }

    public static boolean wasKilledByHordeZombie(Entity pAttacker) {
        if (undeadNightsDetected()) {
            return (pAttacker instanceof HordeZombieEntity) || (pAttacker instanceof EliteZombieEntity);
        }
        return false;
    }
}
