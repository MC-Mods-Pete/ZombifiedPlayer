package net.petemc.zombifiedplayer.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.fml.ModList;
//import top.theillusivec4.curios.api.CuriosApi;
//import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

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
        /*
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

         */
        
        return curiosItems;
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
            if (itemStackWithSlot.isValidInContainer(curiosItems.size())) {
                setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack(), curiosItems);
                if (itemStackWithSlot.slot() < curiosItems.size()) {
                    curiosItems.set(itemStackWithSlot.slot(), itemStackWithSlot.stack());
                }
            }
        }
    }

    public static void setItem(int index, ItemStack itemStack, List<ItemStack> curiosItems) {
        if (index < curiosItems.size()) {
            curiosItems.set(index, itemStack);
        }
/*
        EquipmentSlot equipmentslot = (EquipmentSlot)EQUIPMENT_SLOT_MAPPING.get(index);
        if (equipmentslot != null) {
            this.equipment.set(equipmentslot, itemStack);
        }

 */
    }
/*
    public static ListTag curiosItemsToNbt(Entity entity, List<ItemStack> curiosItems, ListTag nbtList) {
        int i;
        CompoundTag nbtCompound;
        
        for (i = 0; i < curiosItems.size(); ++i) {
            if (!curiosItems.get(i).isEmpty()) {
                nbtCompound = new CompoundTag();
                nbtCompound.putByte("Slot", (byte) i);
                //nbtList.add(curiosItems.get(i).save(entity.registryAccess(), nbtCompound));
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> curiosItemsFromNbt(Entity entity, ListTag nbtList) {
        List<ItemStack> curiosItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); ++i) {
            /*CompoundTag nbtCompound = nbtList.getCompound(i);
            ItemStack stack = ItemStack.parse(entity.registryAccess(), nbtCompound).orElse(ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                curiosItems.add(stack);
            }


        }
        return curiosItems;
    }

 */
}
