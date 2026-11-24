package net.petemc.zombifiedplayer.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ZombifiedPlayer.MOD_ID);

    // Colors: Steve's shirt and rotten green. Forge registers the egg colors and the dispenser behaviour itself.
    // Creative tab like the vanilla spawn eggs in 1.19.2 (Miscellaneous)
    public static final RegistryObject<Item> ZOMBIFIED_PLAYER_SPAWN_EGG = ITEMS.register("zombified_player_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.ZOMBIFIED_PLAYER, 0x00A8A8, 0x2A4220, new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
