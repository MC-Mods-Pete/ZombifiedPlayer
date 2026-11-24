package net.petemc.zombifiedplayer.util;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;

/**
 * Hardcore Revival event hooks (Balm events).
 * Only referenced from HardcoreRevivalUtil after checking that Hardcore Revival is loaded,
 * so the Balm and Hardcore Revival classes are never loaded without the mod.
 */
final class HardcoreRevivalEvents {

    private HardcoreRevivalEvents() {
    }

    static void register() {
        BalmEvents events = Balm.getEvents();
        // Hardcore Revival 9 (1.19.2) has no revived/rescued events: the tracked cause is only used for a death by
        // not_rescued_in_time, which always follows a knockout, and every knockout replaces it
        events.onEvent(PlayerKnockedOutEvent.class,
                event -> HardcoreRevivalUtil.onKnockedOut(event.getPlayer(), event.getSource()));
    }
}
