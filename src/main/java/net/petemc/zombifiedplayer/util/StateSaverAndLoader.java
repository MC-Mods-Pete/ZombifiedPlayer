package net.petemc.zombifiedplayer.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {
    public Map<UUID, String> gameProfiles;// = new HashMap<>();

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(Uuids.CODEC, Codec.STRING)
                            .fieldOf("gameProfiles").forGetter(state -> state.gameProfiles)
            ).apply(instance, StateSaverAndLoader::new)
    );

    public static PersistentStateType<StateSaverAndLoader> createStateType() {
        return new PersistentStateType<>(ZombifiedPlayer.MOD_ID + "_data", StateSaverAndLoader::new, CODEC, null);
    }

    public StateSaverAndLoader() {
        this(
                new HashMap<UUID, String>()
        );
    }

    public StateSaverAndLoader(
            Map<UUID, String> spawnedHordeMobs
    )
    {
        this.gameProfiles = new HashMap<>(spawnedHordeMobs);
        this.markDirty();
    }
}
