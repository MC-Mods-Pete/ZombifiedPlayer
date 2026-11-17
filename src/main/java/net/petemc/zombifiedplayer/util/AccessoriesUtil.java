package net.petemc.zombifiedplayer.util;

import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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

    public static void saveAccessoriesItems(WriteView.ListAppender<StackWithSlot> list, List<ItemStack> accessoriesItems) {
        for (int i = 0; i < accessoriesItems.size(); ++i) {
            ItemStack itemstack = accessoriesItems.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new StackWithSlot(i, itemstack));
            }
        }
    }

    public static void loadAccessoriesItems(ReadView.TypedListReadView<StackWithSlot> list, List<ItemStack> accessoriesItems) {
        accessoriesItems.clear();

        for (StackWithSlot itemStackWithSlot : list) {
            if (itemStackWithSlot.isValidSlot(accessoriesItems.size())) {
                setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack(), accessoriesItems);
                if (itemStackWithSlot.slot() < accessoriesItems.size()) {
                    accessoriesItems.set(itemStackWithSlot.slot(), itemStackWithSlot.stack());
                }
            }
        }
    }

    public static void setItem(int index, ItemStack itemStack, List<ItemStack> accessoriesItems) {
        if (index < accessoriesItems.size()) {
            accessoriesItems.set(index, itemStack);
        }
    }
}

