package net.petemc.zombifiedplayer.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
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
        }
        
        return curiosItems;
    }

    public static boolean checkForItemInCurios(Player player, String itemId) {
        return ForgeRegistries.ITEMS.getDelegate(ResourceLocation.parse(itemId))
                .map(item -> checkForItemInCurios(player, item.get().getDefaultInstance()))
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

    public static ListTag writeCuriosItemsToNbt(List<ItemStack> curiosItems) {
        ListTag nbtList = new ListTag();
        
        for (int i = 0; i < curiosItems.size(); i++) {
            ItemStack stack = curiosItems.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemNbt = new CompoundTag();
                itemNbt.putByte("Slot", (byte) i);
                stack.save(itemNbt);
                nbtList.add(itemNbt);
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readCuriosItemsFromNbt(ListTag nbtList) {
        List<ItemStack> curiosItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag itemNbt = nbtList.getCompound(i);
            ItemStack stack = ItemStack.of(itemNbt);
            if (!stack.isEmpty()) {
                curiosItems.add(stack);
            }
        }
        
        return curiosItems;
    }
}
