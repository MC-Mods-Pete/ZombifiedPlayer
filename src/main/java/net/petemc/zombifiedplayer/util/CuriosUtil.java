package net.petemc.zombifiedplayer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.List;

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
            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(curiosInventory -> {
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

    public static ListTag curiosItemsToNbt(List<ItemStack> curiosItems) {
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

    public static List<ItemStack> curiosItemsFromNbt(ListTag nbtList) {
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
