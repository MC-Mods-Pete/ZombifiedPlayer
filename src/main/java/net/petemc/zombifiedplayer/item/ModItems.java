package net.petemc.zombifiedplayer.item;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZombifiedPlayer.MOD_ID);

    // Since 1.21.5 spawn eggs have their own texture (textures/item/zombified_player_spawn_egg.png) instead of two colors.
    // Entity types are registered before items, so the entity type exists here.
    public static final DeferredItem<SpawnEggItem> ZOMBIFIED_PLAYER_SPAWN_EGG = ITEMS.registerItem("zombified_player_spawn_egg",
            properties -> new SpawnEggItem(properties.spawnEgg(ModEntities.ZOMBIFIED_PLAYER.get())));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    /** Vanilla gives the dispenser behaviour only to the spawn eggs that exist at bootstrap (call on the main thread). */
    public static void registerDispenserBehavior() {
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(ZOMBIFIED_PLAYER_SPAWN_EGG.get())) {
            DispenseItemBehavior spawnEggBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.ZOMBIE_SPAWN_EGG);
            if (spawnEggBehavior != null) {
                DispenserBlock.registerBehavior(ZOMBIFIED_PLAYER_SPAWN_EGG.get(), spawnEggBehavior);
            }
        }
    }
}
