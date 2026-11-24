package net.petemc.zombifiedplayer.util;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.BalmEvents;
import net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent;
import net.blay09.mods.hardcorerevival.api.PlayerRescuedEvent;
import net.blay09.mods.hardcorerevival.api.PlayerRevivedEvent;

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
        events.onEvent(PlayerKnockedOutEvent.class,
                event -> HardcoreRevivalUtil.onKnockedOut(event.getPlayer(), event.getSource()));
        events.onEvent(PlayerRevivedEvent.class,
                event -> HardcoreRevivalUtil.onNoLongerKnockedOut(event.getPlayer()));
        events.onEvent(PlayerRescuedEvent.class,
                event -> HardcoreRevivalUtil.onNoLongerKnockedOut(event.getPlayer()));
    }
}
