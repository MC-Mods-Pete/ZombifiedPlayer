package net.petemc.zombifiedplayer.client;

import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.List;

/**
 * Id and the turn-on-once decision of the built-in shambling animation pack, without client classes
 * (so it can be checked by a GameTest on the server). Used by {@link BuiltinPacks}.
 */
public final class BuiltinPackSelection {
    public static final String SHAMBLING_ANIMATION = "shambling_animation";
    /** Folder of the pack in the mod jar (namespace = mod id, path below the jar root), for AddPackFindersEvent#addPackFinders. */
    public static final Identifier SHAMBLING_ANIMATION_LOCATION = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "resourcepacks/" + SHAMBLING_ANIMATION);
    /** Pack id as NeoForge 26.1 registers it ("mod/" + location). */
    public static final String SHAMBLING_ANIMATION_ID = "mod/" + SHAMBLING_ANIMATION_LOCATION;

    private BuiltinPackSelection() {
    }

    /**
     * Adds the pack to the selected packs unless it was turned on before or is missing.
     * Returns whether options.txt and the marker have to be saved.
     */
    public static boolean turnOnOnce(List<String> selectedPacks, boolean packAvailable, boolean alreadyTurnedOn) {
        if (alreadyTurnedOn || !packAvailable) {
            return false;
        }
        if (!selectedPacks.contains(SHAMBLING_ANIMATION_ID)) {
            selectedPacks.add(SHAMBLING_ANIMATION_ID);
            ZombifiedPlayer.LOGGER.info("Turned on the built-in resource pack {}", SHAMBLING_ANIMATION_ID);
        }
        return true;
    }
}
