package net.petemc.zombifiedplayer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

// Deprecated --> will be removed

public class StateSaverAndLoader extends SavedData {

    public HashMap<UUID, GameProfileData> gameProfiles = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag gameProfilesNbt = new CompoundTag();
        gameProfiles.forEach((uuid, gameProfileData) -> {
            CompoundTag gameProfileNbt = new CompoundTag();

            gameProfileNbt.putUUID("gameProfileUUID", gameProfileData.gameProfileUUID);
            gameProfileNbt.putString("gameProfileName", gameProfileData.gameProfileName);

            gameProfilesNbt.put(uuid.toString(), gameProfileNbt);
        });
        nbt.put("gameProfiles", gameProfilesNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(CompoundTag tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        CompoundTag gameProfilesNbt = tag.getCompound("gameProfiles");
        gameProfilesNbt.getAllKeys().forEach(key -> {
            GameProfileData gameProfileData = new GameProfileData();

            gameProfileData.gameProfileUUID = gameProfilesNbt.getCompound(key).getUUID("gameProfileUUID");
            gameProfileData.gameProfileName = gameProfilesNbt.getCompound(key).getString("gameProfileName");

            UUID uuid = UUID.fromString(key);
            state.gameProfiles.put(uuid, gameProfileData);
        });

        return state;
    }

    /**
     * This function gets the 'PersistentStateManager' and creates or returns the filled in 'StateSaveAndLoader'.
     * It does this by calling 'StateSaveAndLoader::createFromNbt' passing it the previously saved 'NbtCompound' we wrote in 'writeNbt'.
     */
    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        DimensionDataStorage persistentStateManager = server.getLevel(Level.OVERWORLD).getDataStorage();

        StateSaverAndLoader state = persistentStateManager.computeIfAbsent(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::new,
                ZombifiedPlayer.MOD_ID
        );

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.setDirty();

        return state;
    }

    public static GameProfileData getGameProfileState(UUID zombUuid, Level world) {
        StateSaverAndLoader serverState = getServerState(Objects.requireNonNull(world.getServer()));

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        return serverState.gameProfiles.computeIfAbsent(zombUuid, uuid -> new GameProfileData());
    }
}
