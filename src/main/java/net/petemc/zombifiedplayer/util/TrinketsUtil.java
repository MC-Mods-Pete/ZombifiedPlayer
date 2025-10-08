package net.petemc.zombifiedplayer.util;

//import dev.emi.trinkets.api.SlotReference;
//import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

// Trinkets are currently not compatible with 1.21.9

public class TrinketsUtil {

    public static boolean isTrinketsLoaded() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    public static List<ItemStack> getTrinketItemsAndClear(PlayerEntity player) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        /*
        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Pair<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                        trinketsItems.add(itemStackPair.getRight().copy());
                        itemStackPair.getRight().setCount(0);
                }
            });
        } catch (Exception e) {
        }
        */
        
        return trinketsItems;
    }

    public static void saveTrinketItems(WriteView.ListAppender<StackWithSlot> list, List<ItemStack> curiosItems) {
        for (int i = 0; i < curiosItems.size(); ++i) {
            ItemStack itemstack = curiosItems.get(i);
            if (!itemstack.isEmpty()) {
                list.add(new StackWithSlot(i, itemstack));
            }
        }
    }

    public static void loadTrinketItems(ReadView.TypedListReadView<StackWithSlot> list, List<ItemStack> curiosItems) {
        curiosItems.clear();

        for (StackWithSlot itemStackWithSlot : list) {
            if (itemStackWithSlot.isValidSlot(curiosItems.size())) {
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
    public static NbtList writeTrinketItemsToNbt(Entity entity, List<ItemStack> trinketItems, NbtList nbtList) {
        for (int i = 0; i < trinketItems.size(); i++) {
            ItemStack itemStack = trinketItems.get(i);
            if (!itemStack.isEmpty()) {
                NbtCompound nbtCompound = new NbtCompound();
                nbtCompound.putByte("Slot", (byte) i);
                //nbtList.add(itemStack.encode(entity.getRegistryManager(), nbtCompound));
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readTrinketItemsFromNbt(Entity entity, NbtList nbtList) {
        List<ItemStack> trinketItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            //NbtCompound nbtCompound = nbtList.getCompound(i);
            //ItemStack itemStack = ItemStack.fromNbt(entity.getRegistryManager(), nbtCompound).orElse(ItemStack.EMPTY);
            //if (!itemStack.isEmpty()) {
            //    trinketItems.add(itemStack);
            //}
        }
        
        return trinketItems;
    }

     */
}
