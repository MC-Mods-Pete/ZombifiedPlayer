package net.petemc.zombifiedplayer.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ModEntities;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import net.petemc.zombifiedplayer.util.TrinketsUtil;

public class PlayerDeathEvents {

    private static LivingEntity pPlayer = null;
    private static LivingEntity pAttacker = null;
    private static DamageSource pSource = null;
    private static float pAmount = 0.0f;

    public PlayerDeathEvents() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, amount) -> {
            pPlayer = entity;
            pAttacker = damageSource.getAttacker() instanceof LivingEntity ? ((LivingEntity) damageSource.getAttacker()) : null;
            pSource = damageSource;
            pAmount = amount;
            executeAllowDeath();
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            pPlayer = entity;
            pAttacker = damageSource.getAttacker() instanceof LivingEntity ? ((LivingEntity) damageSource.getAttacker()) : null;
            pSource = damageSource;
            executeAfterDeath();
        });
    }

    public static void executeAllowDeath() {
        if (pPlayer != null) {
            if (pPlayer instanceof ServerPlayerEntity serverPlayer) {
                boolean flag = TrinketsUtil.checkForItemInTrinkets(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultStack());
                if (!(pPlayer.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) || pPlayer.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) || flag)) {
                    if ((MainConfig.getSpawnOnAnyDeath() ||
                            (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection()) ||
                            (attackerIsUndead() && MainConfig.getSpawnZombifiedPlayerAfterDeath()))) {
                        ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer);
                    }
                }
            }
        }
    }

    public static void executeAfterDeath() {
        if (pPlayer != null) {
            if (pPlayer instanceof ServerPlayerEntity serverPlayer) {
                if ((MainConfig.getSpawnOnAnyDeath() ||
                        (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection()) ||
                        (attackerIsUndead() && MainConfig.getSpawnZombifiedPlayerAfterDeath()))) {
                    if (MainConfig.getPrintSpawnMessageInChat()) {
                        serverPlayer.sendMessageToClient(Text.translatable("zombifiedplayer.spawn.message"), false);
                        if (MainConfig.getPrintSpawnLocationInChat()) {
                            Text textCoordinates = Texts.bracketed(Text.translatable("chat.coordinates", pPlayer.getBlockPos().getX(), pPlayer.getBlockPos().getY(), pPlayer.getBlockPos().getZ()))
                                    .styled(
                                            style -> style.withColor(Formatting.GREEN)
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pPlayer.getBlockPos().getX() + " " + pPlayer.getBlockPos().getY() + " " + pPlayer.getBlockPos().getZ()))
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("chat.coordinates.tooltip")))
                                    );
                            serverPlayer.sendMessageToClient(Text.translatable("zombifiedplayer.location.message", textCoordinates), false);
                        }
                    }
                }
            }
        }
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
