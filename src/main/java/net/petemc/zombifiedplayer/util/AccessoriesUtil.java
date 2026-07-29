package net.petemc.zombifiedplayer.util;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccessoriesUtil {

    public static boolean isAccessoriesLoaded() {
        return FabricLoader.getInstance().isModLoaded("accessories");
    }

    public static List<ItemStack> getAccessoriesItemsAndClear(PlayerEntity player) {
        List<ItemStack> accessoriesItems = new ArrayList<>();

        if (!isAccessoriesLoaded()) {
            return accessoriesItems;
        }

        try {
            AccessoriesCapability.getOptionally(player).ifPresent(capability -> {
                for (SlotEntryReference entry : capability.getAllEquipped()) {
                    accessoriesItems.add(entry.stack().copy());
                    entry.reference().setStack(ItemStack.EMPTY);
                }
            });
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error getting accessory items from player: " + e.getMessage());
        }

        return accessoriesItems;
    }

    public static boolean checkForItemInAccessories(PlayerEntity player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isAccessoriesLoaded()) {
            return foundItem.get();
        }

        try {
            AccessoriesCapability.getOptionally(player).ifPresent(capability -> {
                for (SlotEntryReference entry : capability.getAllEquipped()) {
                    if (entry.stack().isOf(itemToCheck.getItem())) {
                        ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in accessories!");
                        foundItem.set(true);
                    }
                }
            });
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error checking accessories for item: " + e.getMessage());
        }

        return foundItem.get();
    }

    public static NbtList writeAccessoriesItemsToNbt(Entity entity, List<ItemStack> accessoryItems, NbtList nbtList) {
        for (int i = 0; i < accessoryItems.size(); i++) {
            ItemStack itemStack = accessoryItems.get(i);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                nbtList.add(itemStack.encode(entity.getRegistryManager(), nbtCompound));
            }
        }

        return nbtList;
    }

    public static List<ItemStack> readAccessoriesItemsFromNbt(Entity entity, NbtList nbtList)  {
        List<ItemStack> accessoryItems = new ArrayList<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            ItemStack itemStack = ItemStack.fromNbt(entity.getRegistryManager(), nbtCompound).orElse(ItemStack.EMPTY);
            if (!itemStack.isEmpty()) {
                accessoryItems.add(itemStack);
            }
        }

        return accessoryItems;
    }
}

