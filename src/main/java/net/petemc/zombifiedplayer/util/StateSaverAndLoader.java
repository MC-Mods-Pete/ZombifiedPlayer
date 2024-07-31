package net.petemc.zombifiedplayer.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
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
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
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

    public static StateSaverAndLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
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

    private static Type<StateSaverAndLoader> type = new Type<>(
            StateSaverAndLoader::new, // If there's no 'StateSaverAndLoader' yet create one
            StateSaverAndLoader::createFromNbt, // If there is a 'StateSaverAndLoader' NBT, parse it with 'createFromNbt'
            null // Supposed to be an 'DataFixTypes' enum, but we can just pass null
    );

    public static StateSaverAndLoader getServerState(MinecraftServer server) {
        // (Note: arbitrary choice to use 'World.OVERWORLD' instead of 'World.END' or 'World.NETHER'.  Any work)
        PersistentStateManager persistentStateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();

        // The first time the following 'getOrCreate' function is called, it creates a brand new 'StateSaverAndLoader' and
        // stores it inside the 'PersistentStateManager'. The subsequent calls to 'getOrCreate' pass in the saved
        // 'StateSaverAndLoader' NBT on disk to our function 'StateSaverAndLoader::createFromNbt'.
        StateSaverAndLoader state = persistentStateManager.getOrCreate(type, ZombifiedPlayer.MOD_ID);

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
