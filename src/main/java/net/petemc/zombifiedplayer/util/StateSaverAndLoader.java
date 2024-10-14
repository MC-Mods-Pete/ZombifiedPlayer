package net.petemc.zombifiedplayer.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverAndLoader extends PersistentState {

    public HashMap<UUID, GameProfileData> gameProfiles = new HashMap<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound gameProfilesNbt = new NbtCompound();
        gameProfiles.forEach((uuid, gameProfileData) -> {
            NbtCompound gameProfileNbt = new NbtCompound();

            gameProfileNbt.putUuid("gameProfileUUID", gameProfileData.gameProfileUUID);
            gameProfileNbt.putString("gameProfileName", gameProfileData.gameProfileName);

            gameProfilesNbt.put(uuid.toString(), gameProfileNbt);
        });
        nbt.put("gameProfiles", gameProfilesNbt);

        return nbt;
    }

    public static StateSaverAndLoader createFromNbt(NbtCompound tag) {
        StateSaverAndLoader state = new StateSaverAndLoader();

        NbtCompound gameProfilesNbt = tag.getCompound("gameProfiles");
        gameProfilesNbt.getKeys().forEach(key -> {
            GameProfileData gameProfileData = new GameProfileData();

            gameProfileData.gameProfileUUID = gameProfilesNbt.getCompound(key).getUuid("gameProfileUUID");
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
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        StateSaverAndLoader state = persistentStateManager.getOrCreate(
                StateSaverAndLoader::createFromNbt,
                StateSaverAndLoader::new,
                ZombifiedPlayer.MOD_ID
        );

        // If state is not marked dirty, when Minecraft closes, 'writeNbt' won't be called and therefore nothing will be saved.
        // Technically it's 'cleaner' if you only mark state as dirty when there was actually a change, but the vast majority
        // of mod writers are just going to be confused when their data isn't being saved, and so it's best just to 'markDirty' for them.
        // Besides, it's literally just setting a bool to true, and the only time there's a 'cost' is when the file is written to disk when
        // there were no actual change to any of the mods state (INCREDIBLY RARE).
        state.markDirty();

        return state;
    }

    public static GameProfileData getGameProfileState(UUID zombUuid, World world) {
        StateSaverAndLoader serverState = getServerState(world.getServer());

        // Either get the player by the uuid, or we don't have data for him yet, make a new player state
        return serverState.gameProfiles.computeIfAbsent(zombUuid, uuid -> new GameProfileData());
    }
}
