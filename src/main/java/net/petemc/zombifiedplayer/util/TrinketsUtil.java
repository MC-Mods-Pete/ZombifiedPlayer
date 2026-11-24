package net.petemc.zombifiedplayer.util;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TrinketsUtil {

    /** Set when the installed Trinkets has a different API than the one this mod was built against. */
    private static volatile boolean apiIncompatible = false;

    public static boolean isTrinketsLoaded() {
        return !apiIncompatible && FabricLoader.getInstance().isModLoaded("trinkets");
    }

    /**
     * The installed Trinkets version has a different API (missing class or method): log once and stop using it,
     * instead of crashing in the middle of the player's death. Trinkets then handles its slots on death itself.
     */
    private static void disableIncompatibleApi(LinkageError e) {
        if (!apiIncompatible) {
            apiIncompatible = true;
            ZombifiedPlayer.LOGGER.error("The installed Trinkets version is not compatible with this version of Zombified Player, "
                    + "trinkets are no longer transferred to zombified players and do not count for the totem check", e);
        }
    }

    /**
     * Takes the trinkets that go to the zombified player, following the drop rules of Trinkets in the same order as
     * Trinkets on death: item rule, changed by other mods ({@code TrinketDropCallback}), then the rule of the slot type.
     * KEEP and DESTROY stay in the slot, Trinkets applies them itself. DROP goes to the zombified player, also with
     * keepInventory. DEFAULT: the player keeps it with keepInventory (unless ignoreKeepInventory), otherwise it goes to
     * the zombified player, with Curse of Vanishing it is destroyed.
     */
    public static List<ItemStack> takeDroppedTrinkets(Player player, boolean playerKeepsInventory) {
        List<ItemStack> trinketsItems = new ArrayList<>();

        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> trinketComponent.forEach((reference, stack) -> {
                if (stack.isEmpty()) return;
                DropRule rule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, reference, player);
                rule = TrinketDropCallback.EVENT.invoker().drop(rule, stack, reference, player);
                TrinketInventory inventory = reference.inventory();
                if (rule == DropRule.DEFAULT) {
                    rule = inventory.getSlotType().getDropRule();
                }
                if (rule == DropRule.DEFAULT) {
                    if (playerKeepsInventory) return;
                    if (EnchantmentHelper.hasVanishingCurse(stack)) {
                        inventory.setItem(reference.index(), ItemStack.EMPTY);
                        return;
                    }
                    rule = DropRule.DROP;
                }
                if (rule == DropRule.DROP) {
                    trinketsItems.add(stack.copy());
                    inventory.setItem(reference.index(), ItemStack.EMPTY);
                }
            }));
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error getting trinket items from player: " + e.getMessage());
        } catch (LinkageError e) {
            disableIncompatibleApi(e);
        }

        return trinketsItems;
    }

    public static boolean checkForItemInTrinkets(Player player, String itemId) {
        ResourceLocation identifier = ResourceLocation.tryParse(itemId);
        if (identifier == null) {
            return false;
        }

        return BuiltInRegistries.ITEM.getOptional(identifier)
                .map(item -> checkForItemInTrinkets(player, item.getDefaultInstance()))
                .orElse(false);
    }

    public static boolean checkForItemInTrinkets(Player player, ItemStack itemToCheck) {
        AtomicBoolean foundItem = new AtomicBoolean(false);

        if (!isTrinketsLoaded()) {
            return foundItem.get();
        }

        try {
            TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
                for (Tuple<SlotReference, ItemStack> itemStackPair : trinketComponent.getAllEquipped()) {
                    if (itemStackPair.getB().is(itemToCheck.getItem())) {
                        ZombifiedPlayer.LOGGER.info("Found " + itemToCheck + " in trinkets!");
                        foundItem.set(true);
                    }
                }
            });
        } catch (Exception e) {
        } catch (LinkageError e) {
            disableIncompatibleApi(e);
        }

        return foundItem.get();
    }

    
    public static ListTag writeTrinketItemsToNbt(List<ItemStack> trinketsItems) {
        ListTag nbtList = new ListTag();
        
        for (int i = 0; i < trinketsItems.size(); i++) {
            ItemStack stack = trinketsItems.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemNbt = new CompoundTag();
                itemNbt.putByte("Slot", (byte) i);
                stack.save(itemNbt);
                nbtList.add(itemNbt);
            }
        }
        
        return nbtList;
    }

    public static List<ItemStack> readTrinketItemsFromNbt(ListTag nbtList) {
        List<ItemStack> trinketsItems = new ArrayList<>();
        
        for (int i = 0; i < nbtList.size(); i++) {
            CompoundTag itemNbt = nbtList.getCompound(i);
            ItemStack stack = ItemStack.of(itemNbt);
            if (!stack.isEmpty()) {
                trinketsItems.add(stack);
            }
        }
        
        return trinketsItems;
    }
}
