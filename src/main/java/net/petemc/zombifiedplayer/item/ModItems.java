package net.petemc.zombifiedplayer.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZombifiedPlayer.MOD_ID);

    // Colors: Steve's shirt and rotten green. NeoForge registers the egg colors and the dispenser behaviour itself.
    public static final DeferredItem<Item> ZOMBIFIED_PLAYER_SPAWN_EGG = ITEMS.register("zombified_player_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.ZOMBIFIED_PLAYER, 0x00A8A8, 0x2A4220, new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
