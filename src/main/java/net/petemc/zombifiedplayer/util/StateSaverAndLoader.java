package net.petemc.zombifiedplayer.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

// Todo remove (currently unused)

public class StateSaverAndLoader extends SavedData {

    public HashMap<UUID, GameProfileData> gameProfiles = new HashMap<>();

    public static StateSaverAndLoader load(CompoundTag tag, HolderLookup.Provider registries) {
        StateSaverAndLoader state = new StateSaverAndLoader();
        CompoundTag gameProfilesNbt = tag.getCompound("gameProfiles");
        gameProfilesNbt.getAllKeys().forEach(key -> {
            GameProfileData gameProfileData = new GameProfileData();

            gameProfileData.gameProfileUUID = gameProfilesNbt.getCompound(key).getUUID("gameProfileUUID");
            gameProfileData.gameProfileName = gameProfilesNbt.getCompound(key).getString("gameProfileName");

            UUID uuid = UUID.fromString(key);
            state.gameProfiles.put(uuid, gameProfileData);
        });

        state.setDirty();
        return state;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        CompoundTag gameProfilesNbt = new CompoundTag();
        gameProfiles.forEach((uuid, gameProfileData) -> {
            CompoundTag gameProfileNbt = new CompoundTag();

            gameProfileNbt.putUUID("gameProfileUUID", gameProfileData.gameProfileUUID);
            gameProfileNbt.putString("gameProfileName", gameProfileData.gameProfileName);

            gameProfilesNbt.put(uuid.toString(), gameProfileNbt);
        });
        tag.put("gameProfiles", gameProfilesNbt);
        return tag;
    }

    public static SavedData.Factory<StateSaverAndLoader> factory() {
        return new SavedData.Factory<>(StateSaverAndLoader::new, StateSaverAndLoader::load, null);
    }

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), ZombifiedPlayer.MOD_ID);
    }

    public static GameProfileData getGameProfileState(UUID zombUuid, Level level) {
        StateSaverAndLoader serverState = getServerState(Objects.requireNonNull(level.getServer()));

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        return serverState.gameProfiles.computeIfAbsent(zombUuid, uuid -> new GameProfileData());
    }
}
