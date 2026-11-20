package net.petemc.zombifiedplayer.util;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.events.OnDropCallback;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.accessories.api.slot.SlotReference;
import io.wispforest.accessories.impl.ExpandedSimpleContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.fml.ModList;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AccessoriesUtil {

    public static boolean isAccessoriesLoaded() {
        return ModList.get().isLoaded("accessories");
    }

    /**
     * Takes the accessories (also cosmetic ones) that go to the zombified player, following the drop rules of Accessories:
     * item rule, changed by other mods ({@code OnDropCallback}), then the rule of the slot type. KEEP and DESTROY stay in
     * the slot, Accessories applies them itself. DROP goes to the zombified player, also with keepInventory.
     * DEFAULT: the player keeps it with keepInventory (unless ignoreKeepInventory), otherwise it goes to the zombified
     * player, with Curse of Vanishing it is destroyed.
     */
    public static List<ItemStack> takeDroppedAccessories(Player player, DamageSource source, boolean playerKeepsInventory) {
        List<ItemStack> accessoryItems = new ArrayList<>();

        if (!isAccessoriesLoaded()) {
            return accessoryItems;
        }

        try {
            AccessoriesCapability.getOptionally(player).ifPresent(capability -> {
                for (Map.Entry<String, AccessoriesContainer> entry : capability.getContainers().entrySet()) {
                    AccessoriesContainer container = entry.getValue();
                    takeDropped(player, entry.getKey(), container.getAccessories(), source, playerKeepsInventory, accessoryItems);
                    takeDropped(player, entry.getKey(), container.getCosmeticAccessories(), source, playerKeepsInventory, accessoryItems);
                    container.markChanged();
                }
            });
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error getting accessory items from player: " + e.getMessage());
        }

        return accessoryItems;
    }

    private static void takeDropped(Player player, String slotName, ExpandedSimpleContainer stacks, DamageSource source,
                                    boolean playerKeepsInventory, List<ItemStack> accessoryItems) {
        for (int i = 0; i < stacks.getContainerSize(); i++) {
            ItemStack stack = stacks.getItem(i);
            if (stack.isEmpty()) continue;
            SlotReference reference = SlotReference.of(player, slotName, i);
            DropRule rule = AccessoriesAPI.getOrDefaultAccessory(stack).getDropRule(stack, reference, source);
            rule = OnDropCallback.getAlternativeRule(rule, stack, reference, source);
            if (rule == DropRule.DEFAULT && reference.type() != null) {
                rule = reference.type().dropRule();
            }
            if (rule == DropRule.DEFAULT) {
                if (playerKeepsInventory) continue;
                if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    stacks.setItem(i, ItemStack.EMPTY);
                    continue;
                }
                rule = DropRule.DROP;
            }
            if (rule == DropRule.DROP) {
                accessoryItems.add(stack.copy());
                stacks.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static boolean checkForItemInAccessories(Player player, String itemId) {
        return BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse(itemId))
                .map(item -> checkForItemInAccessories(player, item.value().getDefaultInstance()))
                .orElse(false);
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
