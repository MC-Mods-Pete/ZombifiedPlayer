package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.UniversalGravesUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;
import net.petemc.zombifiedplayer.util.PneumonoGravestonesUtil;

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
        if (pPlayer instanceof ServerPlayer serverPlayer && ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, pSource)) {
            UniversalGravesUtil.skipGrave(serverPlayer);
            PneumonoGravestonesUtil.skipGrave(serverPlayer);
            ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
        }
    }

    public static void executeAfterDeath() {
        if (pPlayer instanceof ServerPlayer serverPlayer && ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, pSource)) {
            if (MainConfig.getPrintSpawnMessageInChat()) {
                serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.spawn.message"), false);
                if (MainConfig.getPrintSpawnLocationInChat()) {
                    Component textCoordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pPlayer.blockPosition().getX(), pPlayer.blockPosition().getY(), pPlayer.blockPosition().getZ()))
                            .withStyle(
                                    style -> style.withColor(ChatFormatting.GREEN)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pPlayer.blockPosition().getX() + " " + pPlayer.blockPosition().getY() + " " + pPlayer.blockPosition().getZ()))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                            );
                    serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.location.message", textCoordinates), false);
                }
            }
        }
    }

    public static void registerEvent() {
        new PlayerDeathEvents();
    }
}
