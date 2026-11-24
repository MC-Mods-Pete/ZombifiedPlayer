package net.petemc.zombifiedplayer.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ModEntities;

public class ModItems {
    // Colors: Steve's shirt and rotten green. The client takes the egg colors from SpawnEggItem itself.
    public static final Item ZOMBIFIED_PLAYER_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM,
            new ResourceLocation(ZombifiedPlayer.MOD_ID, "zombified_player_spawn_egg"),
            new SpawnEggItem(ModEntities.ZOMBIFIED_PLAYER, 0x00A8A8, 0x2A4220, new Item.Properties()));

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> entries.accept(ZOMBIFIED_PLAYER_SPAWN_EGG));
        // Vanilla gives the dispenser behaviour only to the spawn eggs that exist at bootstrap, before mods are initialized.
        // Same behaviour as vanilla's spawn eggs (the vanilla one is not accessible in 1.20.1).
        DispenserBlock.registerBehavior(ZOMBIFIED_PLAYER_SPAWN_EGG, new DefaultDispenseItemBehavior() {
            @Override
            public ItemStack execute(BlockSource source, ItemStack stack) {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                EntityType<?> type = ((SpawnEggItem) stack.getItem()).getType(stack.getTag());
                try {
                    type.spawn(source.getLevel(), stack, null, source.getPos().relative(direction), MobSpawnType.DISPENSER,
                            direction != Direction.UP, false);
                } catch (Exception e) {
                    ZombifiedPlayer.LOGGER.error("Error while dispensing spawn egg from dispenser at {}", source.getPos(), e);
                    return ItemStack.EMPTY;
                }
                stack.shrink(1);
                source.getLevel().gameEvent(null, GameEvent.ENTITY_PLACE, source.getPos());
                return stack;
            }
        });
    }
}
