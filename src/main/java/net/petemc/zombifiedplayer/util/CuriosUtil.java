package net.petemc.zombifiedplayer.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.ModList;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CuriosUtil {

    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    public static List<ItemStack> getCuriosItemsAndClear(Player player) {
        List<ItemStack> curiosItems = new ArrayList<>();
        
        if (!isCuriosLoaded()) {
            return curiosItems;
        }
        try {
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                for (String identifier : curiosInventory.getCurios().keySet()) {
                    ICurioStacksHandler stacksHandler = curiosInventory.getCurios().get(identifier);
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty()) {
                            curiosItems.add(stack.copy());
                            stacksHandler.getStacks().setStackInSlot(i, ItemStack.EMPTY);
                        }
                    }
                }
            });
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error accessing Curios API during retrieval", e);
        }

        if (!curiosItems.isEmpty()) {
            ZombifiedPlayer.LOGGER.info("Curious items list is not empty, returning list with " + curiosItems.size() + " items.");
        }
        
        return curiosItems;
    }

    /**
     * Checks if a specific item (identified by its resource location string) is currently equipped in any trinket slot.
     *
     * @param player The player to check.
     * @param itemId The resource location string of the item (e.g. "chargedcharms:charged_totem_charm").
     * @return true if the item is found in a trinket slot, false otherwise.
     */
    public static boolean checkForItemInCurios(Player player, String itemId) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId))
                .map(item -> checkForItemInCurios(player, item.getDefaultInstance()))
                .orElse(false);
    }

    public static boolean checkForItemInCurios(Player player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isCuriosLoaded()) {
            return foundItem.get();
        }

        try {
            CuriosApi.getCuriosInventory(player).ifPresent(curiosInventory -> {
                for (String identifier : curiosInventory.getCurios().keySet()) {
                    ICurioStacksHandler stacksHandler = curiosInventory.getCurios().get(identifier);
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stack = stacksHandler.getStacks().getStackInSlot(i);
                        if (!stack.isEmpty() && stack.getItem() == itemToCheck.getItem()) {
                            ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in curios!");
                            foundItem.set(true);
                            return;
                        }
                    }
                }
            });
        } catch (Exception e) {
        }

        return foundItem.get();
    }

    public static void saveCuriosItems(ValueOutput.TypedOutputList<ItemStackWithSlot> list, List<ItemStack> curiosItems) {
        for (int i = 0; i < curiosItems.size(); ++i) {
            ItemStack itemstack = curiosItems.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new ItemStackWithSlot(i, itemstack));
            }
        }
    }

    public static void loadCuriosItems(ValueInput.TypedInputList<ItemStackWithSlot> list, List<ItemStack> curiosItems) {
        curiosItems.clear();

        for (ItemStackWithSlot itemStackWithSlot : list) {
            curiosItems.add(itemStackWithSlot.stack());
        }
    }
}
