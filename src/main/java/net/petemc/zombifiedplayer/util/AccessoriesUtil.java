package net.petemc.zombifiedplayer.util;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.loader.api.FabricLoader;
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

    public static List<ItemStack> getAccessoryItemsAndClear(PlayerEntity player) {
        List<ItemStack> accessoryItems = new ArrayList<>();

        if (!isAccessoriesLoaded()) {
            return accessoryItems;
        }

        try {
            AccessoriesCapability.getOptionally(player).ifPresent(capability -> {
                for (SlotEntryReference entry : capability.getAllEquipped()) {
                    accessoryItems.add(entry.stack().copy());
                    entry.reference().setStack(ItemStack.EMPTY);
                }
            });
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error getting accessory items from player: " + e.getMessage());
        }

        return accessoryItems;
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

    public static NbtList writeAccessoryItemsToNbt(List<ItemStack> accessoryItems) {
        NbtList nbtList = new NbtList();

        for (int i = 0; i < accessoryItems.size(); i++) {
            ItemStack stack = accessoryItems.get(i);
            if (!stack.isEmpty()) {
                NbtCompound itemNbt = new NbtCompound();
                itemNbt.putByte("Slot", (byte) i);
                stack.writeNbt(itemNbt);
                nbtList.add(itemNbt);
            }
        }

        return nbtList;
    }

    public static List<ItemStack> readAccessoryItemsFromNbt(NbtList nbtList) {
        List<ItemStack> accessoryItems = new ArrayList<>();

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound itemNbt = nbtList.getCompound(i);
            ItemStack stack = ItemStack.fromNbt(itemNbt);
            if (!stack.isEmpty()) {
                accessoryItems.add(stack);
            }
        }

        return accessoryItems;
    }
}

