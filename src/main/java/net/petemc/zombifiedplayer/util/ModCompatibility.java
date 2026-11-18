package net.petemc.zombifiedplayer.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.petemc.contagion.casts.InfectedEntity;
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
            CuriosDeathDrops.register();
        }
        if (isCorpseLoaded()) {
            ZombifiedPlayer.LOGGER.info("Corpse mod detected. No corpse will spawn when a Zombified Player is created.");
        }
        if (isGravestoneLoaded()) {
            ZombifiedPlayer.LOGGER.info("Gravestone mod detected. No gravestone will spawn when a Zombified Player is created.");
        }
        if (HardcoreRevivalUtil.isHardcoreRevivalLoaded()) {
            // event hooks are registered in common setup (ZombifiedPlayer#commonSetup)
            ZombifiedPlayer.LOGGER.info("Hardcore Revival detected. Timeout deaths inherit the original knockout death cause for Zombified Player checks.");
        }
    }

    public static boolean isGravestoneLoaded() {
        return ModList.get().isLoaded("gravestone");
    }

    public static boolean isCorpseLoaded() {
        return ModList.get().isLoaded("corpse");
    }

    public static boolean isContagionLoaded() {
        return ModList.get().isLoaded("contagion");
    }

    public static boolean diedFromInfection(ServerPlayer serverPlayer) {
        if (isContagionLoaded()) {
            if (serverPlayer instanceof InfectedEntity infectedEntity) {
                return infectedEntity.contagion_playerDiedFromInfection();
            }
        }
        return false;
    }

    private static final ResourceLocation CONTAGION_INFECTION = ResourceLocation.fromNamespaceAndPath("contagion", "infection");

    /** True if the player has Contagion's infection effect, e.g. at the moment of a death by fall damage. */
    public static boolean isInfected(ServerPlayer serverPlayer) {
        if (!isContagionLoaded()) {
            return false;
        }
        MobEffect infection = ForgeRegistries.MOB_EFFECTS.getValue(CONTAGION_INFECTION);
        return infection != null && serverPlayer.hasEffect(infection);
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
