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

    /** Set when the installed Curios has an incompatible API (e.g. a Curios for another Minecraft version). */
    private static volatile boolean incompatible = false;

    public static boolean isCuriosLoaded() {
        return !incompatible && ModList.get().isLoaded("curios");
    }

    /** Incompatible Curios API: logged once, after that Curios handles its items alone (no crash on death). */
    public static void disable(LinkageError error) {
        if (!incompatible) {
            incompatible = true;
            ZombifiedPlayer.LOGGER.error("The installed Curios API is not compatible ({}), Curios compatibility is turned off", error.toString());
        }
    }

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
        } catch (LinkageError e) {
            disable(e);
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
