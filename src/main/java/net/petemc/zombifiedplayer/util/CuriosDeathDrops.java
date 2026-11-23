package net.petemc.zombifiedplayer.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.event.CurioDropsEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Curios items on death. Curios applies its own rules (drop rules of items and slots, overrides of other mods,
 * keepInventory, its keepCurios option, Curse of Vanishing), we only take what Curios drops and give it to the
 * zombified player instead of the ground. Only loaded when Curios is installed.
 */
public final class CuriosDeathDrops {

    private record Pending(ZombifiedPlayerEntity zombifiedPlayer, long gameTime) {
    }

    /** Zombified player spawned for the current death of a player (drops follow later in the same tick). */
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private CuriosDeathDrops() {
    }

    public static void register() {
        // LOWEST: overrides and changes of other mods come first
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CuriosDeathDrops::onDropRules);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, CuriosDeathDrops::onCurioDrops);
    }

    /** A zombified player was spawned for this death: the Curios drops of this death go to it. */
    public static void expectDrops(Player player, ZombifiedPlayerEntity zombifiedPlayer) {
        PENDING.put(player.getUUID(), new Pending(zombifiedPlayer, player.level().getGameTime()));
    }

    private static @Nullable ZombifiedPlayerEntity pendingFor(LivingEntity entity) {
        Pending pending = PENDING.get(entity.getUUID());
        if (pending == null) return null;
        if (pending.gameTime() != entity.level().getGameTime()) {
            // the drops never came (e.g. cancelled by another mod)
            PENDING.remove(entity.getUUID(), pending);
            return null;
        }
        return pending.zombifiedPlayer();
    }

    /**
     * keepInventory on, but ignoreKeepInventory: Curios would let the player keep the items with the default rule.
     * Treat them like without keepInventory: dropped (to the zombified player), Curse of Vanishing destroyed.
     * Items with an own rule (item, slot or an override of another mod) keep that rule.
     */
    private static void onDropRules(DropRulesEvent event) {
        try {
            addKeepInventoryOverrides(event);
        } catch (LinkageError e) {
            CuriosUtil.disable(e);
        }
    }

    private static void addKeepInventoryOverrides(DropRulesEvent event) {
        if (!CuriosUtil.isCuriosLoaded()) return;
        if (!(event.getEntity() instanceof Player player) || pendingFor(player) == null) return;
        if (!(player.level() instanceof ServerLevel level) || !level.getGameRules().get(GameRules.KEEP_INVENTORY) || !MainConfig.getIgnoreKeepInventory()) return;

        Set<ItemStack> drop = Collections.newSetFromMap(new IdentityHashMap<>());
        Set<ItemStack> destroy = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Map.Entry<String, ICurioStacksHandler> entry : event.getCurioHandler().getCurios().entrySet()) {
            ICurioStacksHandler handler = entry.getValue();
            if (handler.getDropRule() != DropRule.DEFAULT) continue;
            collectDefault(event, player, entry.getKey(), handler, handler.getStacks(), false, drop, destroy);
            collectDefault(event, player, entry.getKey(), handler, handler.getCosmeticStacks(), true, drop, destroy);
        }
        if (!drop.isEmpty()) event.addOverride(drop::contains, DropRule.ALWAYS_DROP);
        if (!destroy.isEmpty()) event.addOverride(destroy::contains, DropRule.DESTROY);
    }

    private static void collectDefault(DropRulesEvent event, Player player, String identifier, ICurioStacksHandler handler,
                                       IDynamicStackHandler stacks, boolean cosmetic, Set<ItemStack> drop, Set<ItemStack> destroy) {
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack stack = stacks.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (event.getOverrides().stream().anyMatch(o -> o.getA().test(stack))) continue;
            boolean visible = i < handler.getRenders().size() && handler.getRenders().get(i);
            SlotContext context = new SlotContext(identifier, player, i, cosmetic, visible);
            DropRule rule = CuriosApi.getCurio(stack)
                    .map(curio -> curio.getDropRule(context, event.getSource(), event.isRecentlyHit()))
                    .orElse(DropRule.DEFAULT);
            if (rule != DropRule.DEFAULT) continue;
            (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) ? destroy : drop).add(stack);
        }
    }

    /** What Curios drops goes to the zombified player instead of the ground (or a grave). */
    private static void onCurioDrops(CurioDropsEvent event) {
        try {
            takeDrops(event);
        } catch (LinkageError e) {
            CuriosUtil.disable(e);
        }
    }

    private static void takeDrops(CurioDropsEvent event) {
        if (!CuriosUtil.isCuriosLoaded()) return;
        ZombifiedPlayerEntity zombifiedPlayer = pendingFor(event.getEntity());
        if (zombifiedPlayer == null) return;
        PENDING.remove(event.getEntity().getUUID());
        for (ItemEntity drop : event.getDrops()) {
            if (!drop.getItem().isEmpty()) zombifiedPlayer.curiosItems.add(drop.getItem().copy());
        }
        event.getDrops().clear();
    }
}
