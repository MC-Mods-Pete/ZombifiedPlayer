package net.petemc.zombifiedplayer.util;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TrinketsUtil {

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    /**
     * Retrieves all equipped trinket items and clears them from the player's slots.
     *
     * @param player The player whose trinkets are being retrieved.
     * @return A list of copies of the items found in trinket slots.
     */
    public static List<ItemStack> getTrinketItemsAndClear(Player player) {
        List<ItemStack> trinketsItems = new ArrayList<>();

        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        try {
            TrinketAttachment attachment = TrinketsApi.getAttachment(player);

            if (attachment != null) {
                attachment.forEach((access, stack) -> {
                    if (!stack.isEmpty()) {
                        trinketsItems.add(stack.copy());
                        ZombifiedPlayer.LOGGER.info("Found " + stack + " in trinkets, adding to list and clearing slot.");
                        stack.setCount(0);
                    }
                });
            }
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error accessing Trinkets API during retrieval", e);
        }

        if (!trinketsItems.isEmpty()) {
            ZombifiedPlayer.LOGGER.info("Trinkets items list is not empty, returning list with " + trinketsItems.size() + " items.");
        }

        return trinketsItems;
    }

    /**
     * Checks if a specific item is currently equipped in any trinket slot.
     *
     * @param player The player to check.
     * @param itemToCheck The item stack to look for.
     * @return true if the item is found in a trinket slot, false otherwise.
     */
    public static boolean checkForItemInTrinkets(Player player, ItemStack itemToCheck) {
        if (!isTrinketsLoaded()) {
            return false;
        }

        try {
            TrinketAttachment attachment = TrinketsApi.getAttachment(player);

            if (attachment != null) {
                boolean flag = attachment.isEquipped(itemToCheck.getItem());
                if (flag) {
                    ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in trinkets!");
                }
                return flag;
            }
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error accessing Trinkets API during item check", e);
        }

        return false;
    }

    /**
     * Checks if a specific item (identified by its resource location string) is currently equipped in any trinket slot.
     *
     * @param player The player to check.
     * @param itemId The resource location string of the item (e.g. "chargedcharms:charged_totem_charm").
     * @return true if the item is found in a trinket slot, false otherwise.
     */
    public static boolean checkForItemInTrinkets(Player player, String itemId) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId))
                .map(item -> checkForItemInTrinkets(player, item.getDefaultInstance()))
                .orElse(false);
    }

    public static void saveTrinketItems(ValueOutput.TypedOutputList<ItemStackWithSlot> list, List<ItemStack> trinketItems) {
        for (int i = 0; i < trinketItems.size(); ++i) {
            ItemStack itemstack = trinketItems.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new ItemStackWithSlot(i, itemstack));
            }
        }
    }

    public static void loadTrinketItems(ValueInput.TypedInputList<ItemStackWithSlot> list, List<ItemStack> trinketItems) {
        trinketItems.clear();

        for (ItemStackWithSlot itemStackWithSlot : list) {
            trinketItems.add(itemStackWithSlot.stack());
        }
    }
}