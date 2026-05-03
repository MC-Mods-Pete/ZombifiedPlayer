package net.petemc.zombifiedplayer.util;
/*
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccessoriesUtil {

    public static boolean isAccessoriesLoaded() {
        return ModList.get().isLoaded("accessories");
    }

    public static List<ItemStack> getAccessoriesItemsAndClear(Player player) {
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

    public static boolean checkForItemInAccessories(Player player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isAccessoriesLoaded()) {
            return foundItem.get();
        }

        try {
            AccessoriesCapability.getOptionally(player).ifPresent(capability -> {
                for (SlotEntryReference entry : capability.getAllEquipped()) {
                    if (entry.stack().getItem() == itemToCheck.getItem()) {
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

    public static ListTag writeAccessoriesItemsToNbt(Entity entity, List<ItemStack> accessoryItems, ListTag nbtList) {
        CompoundTag nbtCompound;

        for (int i = 0; i < accessoryItems.size(); ++i) {
            if (!accessoryItems.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) i);
                nbtList.add(accessoryItems.get(i).save(entity.registryAccess(), nbtCompound));
            }
        }

        return nbtList;
    }

    public static List<ItemStack> readAccessoriesItemsFromNbt(Entity entity, ListTag nbtList) {
        List<ItemStack> curiosItems = new ArrayList<>();

        for (int i = 0; i < nbtList.size(); ++i) {
            CompoundTag nbtCompound = nbtList.getCompound(i);
            ItemStack stack = ItemStack.parse(entity.registryAccess(), nbtCompound).orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                curiosItems.add(stack);
            }
        }
        return curiosItems;
    }
}
 */
