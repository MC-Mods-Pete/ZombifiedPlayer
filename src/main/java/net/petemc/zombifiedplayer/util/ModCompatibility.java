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
        if (isContagionLoaded()) {
            ZombifiedPlayer.LOGGER.info("Contagion mod detected. Death by Infection can spawn Zombified Players.");
        }
        if (isUndeadNightsLoaded()) {
            ZombifiedPlayer.LOGGER.info("Undead Nights mod detected. Death by a Horde Zombie will spawn a Zombified Player.");
        }
        if (CuriosUtil.isCuriosLoaded()) {
            ZombifiedPlayer.LOGGER.info("Curios API detected. If enabled curios items will be transferred to the Zombified Player.");
        }
    }

    public static boolean isContagionLoaded() {
        return ModList.get().isLoaded("contagion");
    }

    public static boolean diedFromInfection(ServerPlayer serverPlayer) {
        if (isContagionLoaded()) {
            if (serverPlayer instanceof InfectedPlayer infectedPlayer) {
                return infectedPlayer.contagion_playerDiedFromInfection();
            }
        }
        return false;
    }

    public static boolean isUndeadNightsLoaded() {
        return ModList.get().isLoaded("undeadnights");
    }

    public static boolean wasKilledByHordeZombie(Entity pAttacker) {
        if (isUndeadNightsLoaded()) {
            return (pAttacker instanceof HordeZombieEntity) || (pAttacker instanceof EliteZombieEntity);
        }
        return false;
    }
}
