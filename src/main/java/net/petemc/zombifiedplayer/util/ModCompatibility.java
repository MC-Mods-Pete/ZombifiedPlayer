package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
        if (TrinketsUtil.isTrinketsLoaded()) {
            ZombifiedPlayer.LOGGER.info("Trinkets API detected. If enabled trinket items will be transferred to the Zombified Player.");
        }
        if (HardcoreRevivalUtil.isHardcoreRevivalLoaded()) {
            HardcoreRevivalUtil.registerEvents();
            ZombifiedPlayer.LOGGER.info("Hardcore Revival detected. Timeout deaths inherit the original knockout death cause for Zombified Player checks.");
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
        return BuiltInRegistries.MOB_EFFECT.getHolder(CONTAGION_INFECTION)
                .map(serverPlayer::hasEffect)
                .orElse(false);
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
