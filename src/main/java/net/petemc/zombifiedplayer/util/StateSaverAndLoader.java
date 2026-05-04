package net.petemc.zombifiedplayer.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
/*
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StateSaverAndLoader extends SavedData {
    public Map<UUID, String> gameProfiles;// = new HashMap<>();

    public static final Codec<StateSaverAndLoader> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.CODEC, Codec.STRING)
                            .fieldOf("gameProfiles").forGetter(state -> state.gameProfiles)
            ).apply(instance, StateSaverAndLoader::new)
    );

    public static SavedDataType<StateSaverAndLoader> createStateType() {
        return new SavedDataType<>(Identifier.parse(ZombifiedPlayer.MOD_ID + "_data"), StateSaverAndLoader::new, CODEC, null);
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
        this.setDirty();
    }
}

 */
