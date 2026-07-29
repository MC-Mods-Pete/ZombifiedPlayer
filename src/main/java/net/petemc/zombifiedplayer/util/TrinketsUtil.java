package net.petemc.zombifiedplayer.util;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
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

    
    public static NbtList writeTrinketItemsToNbt(Entity entity, List<ItemStack> trinketItems, NbtList nbtList) {
        for (int i = 0; i < trinketItems.size(); i++) {
            ItemStack itemStack = trinketItems.get(i);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                nbtList.add(itemStack.encode(entity.getRegistryManager(), nbtCompound));
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readTrinketItemsFromNbt(Entity entity, NbtList nbtList) {
        List<ItemStack> trinketItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            ItemStack itemStack = ItemStack.fromNbt(entity.getRegistryManager(), nbtCompound).orElse(ItemStack.EMPTY);
            if (!itemStack.isEmpty()) {
                trinketItems.add(itemStack);
            }
        }
        
        return trinketItems;
    }
}
