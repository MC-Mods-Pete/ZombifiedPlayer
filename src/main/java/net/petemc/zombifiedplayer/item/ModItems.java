package net.petemc.zombifiedplayer.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    // Colors: Steve's shirt and rotten green. The client takes the egg colors from SpawnEggItem itself.
    public static final Item ZOMBIFIED_PLAYER_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM,
            ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player_spawn_egg"),
            new SpawnEggItem(ModEntities.ZOMBIFIED_PLAYER, 0x00A8A8, 0x4A6B35, new Item.Properties()));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(ZOMBIFIED_PLAYER_SPAWN_EGG));
        // Vanilla gives the dispenser behaviour only to the spawn eggs that exist at bootstrap, before mods are initialized
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(ZOMBIFIED_PLAYER_SPAWN_EGG)) {
            DispenseItemBehavior spawnEggBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.ZOMBIE_SPAWN_EGG);
            if (spawnEggBehavior != null) {
                DispenserBlock.registerBehavior(ZOMBIFIED_PLAYER_SPAWN_EGG, spawnEggBehavior);
            }
        }
    }
}
