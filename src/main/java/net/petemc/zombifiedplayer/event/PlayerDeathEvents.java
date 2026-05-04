package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import net.petemc.zombifiedplayer.util.PneumonoGravestonesUtil;
import net.petemc.zombifiedplayer.util.TrinketsUtil;
import net.petemc.zombifiedplayer.util.UniversalGravesUtil;

public class PlayerDeathEvents {

    private static LivingEntity pPlayer = null;
    private static LivingEntity pAttacker = null;
    private static DamageSource pSource = null;
    private static float pAmount = 0.0f;

    public PlayerDeathEvents() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            pPlayer = entity;
            pAttacker = damageSource.getEntity() instanceof LivingEntity ? ((LivingEntity) damageSource.getEntity()) : null;
            pSource = damageSource;
            pAmount = amount;
            executeAllowDeath();
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            pPlayer = entity;
            pAttacker = damageSource.getEntity() instanceof LivingEntity ? ((LivingEntity) damageSource.getEntity()) : null;
            pSource = damageSource;
            executeAfterDeath();
        });
    }

    public static void executeAllowDeath() {
        if (pPlayer != null) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                boolean flag = TrinketsUtil.checkForItemInTrinkets(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance());
                boolean flag2 = TrinketsUtil.checkForItemInTrinkets(serverPlayer, "chargedcharms:charged_totem_charm");
                if (!(pPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || pPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING) || flag || flag2)) {
                    if ((MainConfig.getSpawnOnAnyDeath() ||
                            (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection()) ||
                            (attackerIsUndead() && MainConfig.getSpawnZombifiedPlayerAfterDeath()))) {
                        UniversalGravesUtil.skipGrave(serverPlayer);
                        PneumonoGravestonesUtil.skipGrave(serverPlayer);
                        ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
                    }
                }
            }
        }
    }

    public static void executeAfterDeath() {
        if (pPlayer != null) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                if ((MainConfig.getSpawnOnAnyDeath() ||
                        (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection()) ||
                        (attackerIsUndead() && MainConfig.getSpawnZombifiedPlayerAfterDeath()))) {
                    if (MainConfig.getPrintSpawnMessageInChat()) {
                        serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.spawn.message"), false);
                        if (MainConfig.getPrintSpawnLocationInChat()) {
                            BlockPos blockpos = serverPlayer.getOnPos();
                            Component textCoordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", blockpos.getX(), (blockpos.getY() + 1), blockpos.getZ()))
                                    .withStyle((style) -> {
                                        return style.withColor(ChatFormatting.GREEN)
                                                .withClickEvent(new ClickEvent.SuggestCommand("/tp @s " + blockpos.getX() + " " + (blockpos.getY() + 1) + " " + blockpos.getZ()))
                                                .withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.coordinates.tooltip")));
                                    });
                            serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.location.message", textCoordinates), false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Prüft, ob ein ItemStack einem Mod-Item anhand seiner Resource-Location-ID entspricht.
     * Gibt false zurück, wenn das Item im Registry nicht gefunden wird (Mod nicht geladen).
     */
    private static boolean isModItem(net.minecraft.world.item.ItemStack stack, String itemId) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(itemId))
                .map(stack::is)
                .orElse(false);
    }

    private static boolean attackerIsUndead() {
        boolean attackerIsUndead = false;
        if (pAttacker != null) {
            attackerIsUndead =
                   ((pAttacker.getType() == EntityType.ZOMBIE) ||
                    (pAttacker.getType() == EntityType.HUSK) ||
                    (pAttacker.getType() == EntityType.ZOMBIFIED_PIGLIN) ||
                    (pAttacker.getType() == EntityType.DROWNED) ||
                    (pAttacker.getType() == EntityType.ZOMBIE_VILLAGER) ||
                    (pAttacker.getType() == EntityType.ZOGLIN) ||
                    (pAttacker.getType() == ModEntities.ZOMBIFIED_PLAYER));
        }
        attackerIsUndead = attackerIsUndead || ModCompatibility.wasKilledByHordeZombie(pAttacker);
        return attackerIsUndead;
    }

    public static void registerEvent() {
        new PlayerDeathEvents();
    }
}
