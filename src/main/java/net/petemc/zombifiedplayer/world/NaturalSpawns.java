package net.petemc.zombifiedplayer.world;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;

/**
 * Adds natural zombified player spawns to every biome in which zombies spawn (also modded biomes,
 * and none where a datapack removed the zombies). Controlled by the config options naturalSpawning and
 * naturalSpawnWeight: Fabric applies biome modifications each time a world is loaded, so changes take effect
 * the next time the world is loaded.
 * Unlike the NeoForge biome modifier the selector sees the biome as defined by the data (vanilla, datapacks, mods
 * with own biomes), not zombie spawns that other mods add or remove through the Fabric biome API.
 */
public final class NaturalSpawns {

    private NaturalSpawns() {
    }

    public static void register() {
        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "natural_spawns"))
                .add(ModificationPhase.ADDITIONS,
                        context -> MainConfig.getNaturalSpawning() && BiomeSelectors.spawnsOneOf(EntityType.ZOMBIE).test(context),
                        (selection, modification) -> modification.getSpawnSettings().addSpawn(MobCategory.MONSTER,
                                new MobSpawnSettings.SpawnerData(ModEntities.ZOMBIFIED_PLAYER, MainConfig.getNaturalSpawnWeight(), 1, 1)));
    }
}
