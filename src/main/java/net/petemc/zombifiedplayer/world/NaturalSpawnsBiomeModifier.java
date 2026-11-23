package net.petemc.zombifiedplayer.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;

/**
 * Adds natural zombified player spawns to every biome in which zombies spawn (also modded biomes,
 * and none where a datapack or mod removed the zombies). Controlled by the server config options
 * naturalSpawning and naturalSpawnWeight: NeoForge loads the server config before it applies biome modifiers,
 * so changes take effect the next time the world is loaded.
 * Used by data/zombifiedplayer/neoforge/biome_modifier/natural_spawns.json.
 */
public final class NaturalSpawnsBiomeModifier implements BiomeModifier {
    public static final NaturalSpawnsBiomeModifier INSTANCE = new NaturalSpawnsBiomeModifier();

    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ZombifiedPlayer.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<NaturalSpawnsBiomeModifier>> CODEC =
            SERIALIZERS.register("natural_spawns", () -> MapCodec.unit(INSTANCE));

    private NaturalSpawnsBiomeModifier() {
    }

    public static void register(IEventBus modEventBus) {
        SERIALIZERS.register(modEventBus);
    }

    @Override
    public void modify(RegistryAccess registries, Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        // MODIFY runs after the other mods added and removed spawns, so the zombie entries are final
        if (phase != Phase.MODIFY || !MainConfig.getNaturalSpawning()) {
            return;
        }
        MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
        // since 26.3 a biome without monster spawns has no list for the category
        WeightedList.Builder<MobSpawnSettings.SpawnerData> monsters = spawns.getSpawner(MobCategory.MONSTER);
        boolean zombiesSpawnHere = monsters != null && monsters.getList().stream()
                .anyMatch(spawner -> spawner.value().type() == EntityTypes.ZOMBIE);
        if (zombiesSpawnHere) {
            spawns.addSpawn(ModEntities.ZOMBIFIED_PLAYER.get(), MainConfig.getNaturalSpawnWeight(), ConstantInt.of(1));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC.get();
    }
}
