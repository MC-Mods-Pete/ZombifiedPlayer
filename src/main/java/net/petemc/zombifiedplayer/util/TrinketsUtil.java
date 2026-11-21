package net.petemc.zombifiedplayer.util;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;

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
     * Takes the trinkets that go to the zombified player, following the drop rules of Trinkets: the rule comes from
     * Trinkets itself ({@code TrinketsApi.getDropRule}, the same call Trinkets makes on death: item rule, changed by other
     * mods, rule of the slot type, then keepInventory and Curse of Vanishing).
     * KEEP stays in the slot. DESTROY is removed. DROP goes to the zombified player, also with keepInventory.
     * playerKeepsInventory is false with ignoreKeepInventory, then the default rule drops (or destroys) like without keepInventory.
     */
    public static List<ItemStack> takeDroppedTrinkets(Player player, boolean playerKeepsInventory) {
        List<ItemStack> trinketsItems = new ArrayList<>();

        if (!isTrinketsLoaded()) {
            return trinketsItems;
        }

        try {
            TrinketAttachment attachment = TrinketsApi.getAttachment(player);
            if (attachment != null) {
                attachment.forEach((access, stack) -> {
                    if (stack.isEmpty()) return;
                    TrinketDropRule rule = TrinketsApi.getDropRule(stack, access, player, playerKeepsInventory);
                    if (rule == TrinketDropRule.DROP) {
                        trinketsItems.add(stack.copy());
                        access.inventory().setItem(access.index(), ItemStack.EMPTY);
                    } else if (rule == TrinketDropRule.DESTROY) {
                        access.inventory().setItem(access.index(), ItemStack.EMPTY);
                    }
                });
            }
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.error("Error getting trinket items from player", e);
        } catch (LinkageError e) {
            disableIncompatibleApi(e);
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
        } catch (LinkageError e) {
            disableIncompatibleApi(e);
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
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null) {
            return false;
        }
        return BuiltInRegistries.ITEM.getOptional(identifier)
                .map(item -> checkForItemInTrinkets(player, item.getDefaultInstance()))
                .orElse(false);
    }
}
