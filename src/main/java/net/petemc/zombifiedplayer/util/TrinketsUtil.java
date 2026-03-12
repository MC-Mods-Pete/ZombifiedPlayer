package net.petemc.zombifiedplayer.util;

import dev.emi.trinkets.api.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Pair;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrinketsUtil {

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static List<ItemStack> getTrinketItemsAndClear(PlayerEntity player) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Pair<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                        trinketsItems.add(itemStackPair.getRight().copy());
                        itemStackPair.getRight().setCount(0);
                }
            });
        } catch (Exception e) {
        }
        
        return trinketsItems;
    }

    public static boolean checkForItemInTrinkets(PlayerEntity player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isTrinketsLoaded()) {
            return foundItem.get();
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Pair<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                    if (itemStackPair.getRight().isOf(itemToCheck.getItem())) {
                        ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in trinkets!");
                        foundItem.set(true);
                    }
                }
            });
        } catch (Exception e) {
        }

        return foundItem.get();
    }

    
    public static NbtList writeTrinketItemsToNbt(List<ItemStack> trinketsItems) {
        NbtList nbtList = new NbtList();
        
        for (int i = 0; i < trinketsItems.size(); i++) {
            ItemStack stack = trinketsItems.get(i);
            if (!stack.isEmpty()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putByte("Slot", (byte) i);
                stack.writeNbt(itemNbt);
                nbtList.add(itemNbt);
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readTrinketItemsFromNbt(NbtList nbtList) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound itemNbt = nbtList.getCompound(i);
            ItemStack stack = ItemStack.fromNbt(itemNbt);
            if (!stack.isEmpty()) {
                trinketsItems.add(stack);
            }
        }
        
        return trinketsItems;
    }
}
