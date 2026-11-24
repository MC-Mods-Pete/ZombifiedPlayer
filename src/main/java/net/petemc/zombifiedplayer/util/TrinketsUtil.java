package net.petemc.zombifiedplayer.util;

import dev.emi.trinkets.api.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrinketsUtil {

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static List<ItemStack> getTrinketItemsAndClear(Player player) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Tuple<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                        trinketsItems.add(itemStackPair.getB().copy());
                        itemStackPair.getB().setCount(0);
                }
            });
        } catch (Exception e) {
        }
        
        return trinketsItems;
    }

    public static boolean checkForItemInTrinkets(Player player, String itemId) {
        ResourceLocation identifier = ResourceLocation.tryParse(itemId);
        if (identifier == null) {
            return false;
        }

        return BuiltInRegistries.ITEM.getOptional(identifier)
                .map(item -> checkForItemInTrinkets(player, item.getDefaultInstance()))
                .orElse(false);
    }

    public static boolean checkForItemInTrinkets(Player player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isTrinketsLoaded()) {
            return foundItem.get();
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Tuple<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                    if (itemStackPair.getB().is(itemToCheck.getItem())) {
                        ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in trinkets!");
                        foundItem.set(true);
                    }
                }
            });
        } catch (Exception e) {
        }

        return foundItem.get();
    }

    
    public static ListTag writeTrinketItemsToNbt(List<ItemStack> trinketsItems) {
        ListTag nbtList = new ListTag();
        
        for (int i = 0; i < trinketsItems.size(); i++) {
            ItemStack stack = trinketsItems.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemNbt = new CompoundTag();
                itemNbt.putByte("Slot", (byte) i);
                stack.save(itemNbt);
                nbtList.add(itemNbt);
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readTrinketItemsFromNbt(ListTag nbtList) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag itemNbt = nbtList.getCompound(i);
            ItemStack stack = ItemStack.of(itemNbt);
            if (!stack.isEmpty()) {
                trinketsItems.add(stack);
            }
        }
        
        return trinketsItems;
    }
}
