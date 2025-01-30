package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
//import net.petemc.contagion.casts.InfectedPlayer;
//import net.petemc.undeadnights.entity.EliteZombieEntity;
//import net.petemc.undeadnights.entity.HordeZombieEntity;
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
        return FabricLoader.getInstance().isModLoaded("contagion");
    }

    public static boolean diedFromInfection(ServerPlayerEntity serverPlayer) {
        /*if (contagionDetected()) {
            if (serverPlayer instanceof InfectedPlayer infectedPlayer) {
                return infectedPlayer.contagion_playerDiedFromInfection();
            }
        }*/
        return false;
    }

    public static boolean undeadNightsDetected() {
        return FabricLoader.getInstance().isModLoaded("undeadnights");
    }

    public static boolean wasKilledByHordeZombie(LivingEntity pAttacker) {
        /*if (undeadNightsDetected()) {
            return (pAttacker instanceof HordeZombieEntity) || (pAttacker instanceof EliteZombieEntity);
        }*/
        return false;
    }
}
