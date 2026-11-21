package net.petemc.zombifiedplayer.item;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    private static final ResourceKey<Item> SPAWN_EGG_KEY = ResourceKey.create(Registries.ITEM,
            Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player_spawn_egg"));
    // Since 1.21.5 spawn eggs have their own texture (textures/item/zombified_player_spawn_egg.png) instead of two colors
    public static final Item ZOMBIFIED_PLAYER_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(SPAWN_EGG_KEY).spawnEgg(ModEntities.ZOMBIFIED_PLAYER)));

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output -> output.accept(ZOMBIFIED_PLAYER_SPAWN_EGG));
        // Vanilla gives the dispenser behaviour only to the spawn eggs that exist at bootstrap, before mods are initialized
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(ZOMBIFIED_PLAYER_SPAWN_EGG)) {
            DispenseItemBehavior spawnEggBehavior = DispenserBlock.DISPENSER_REGISTRY.get(Items.ZOMBIE_SPAWN_EGG);
            if (spawnEggBehavior != null) {
                DispenserBlock.registerBehavior(ZOMBIFIED_PLAYER_SPAWN_EGG, spawnEggBehavior);
            }
        }
    }
}
