package net.petemc.zombifiedplayer.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import net.petemc.zombifiedplayer.util.PneumonoGravestonesUtil;
import net.petemc.zombifiedplayer.util.UniversalGravesUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;

/**
 * Called by ServerPlayerEntityMixin from ServerPlayer.die(): first the spawn (inventory still full),
 * later the chat message.
 */
public class PlayerDeathEvents {

    public static void onPlayerDeath(ServerPlayer serverPlayer, DamageSource source) {
        if (serverPlayer.level().isClientSide()) {
            return;
        }
        boolean spawn = ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, source);
        if (spawn) {
            UniversalGravesUtil.skipGrave(serverPlayer);
            PneumonoGravestonesUtil.skipGrave(serverPlayer);
        }
        boolean spawned = spawn && ZombifiedPlayerEntity.spawnZombifiedPlayer(serverPlayer) != null;
        // read for the chat message later in ServerPlayer.die()
        ZombifiedPlayerSpawnLogic.setSpawnedThisDeath(serverPlayer, spawned);
    }

    public static void sendSpawnMessage(ServerPlayer serverPlayer) {
        if (!ZombifiedPlayerSpawnLogic.takeSpawnedThisDeath(serverPlayer) || !MainConfig.getPrintSpawnMessageInChat()) {
            return;
        }
        serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.spawn.message"), false);
        if (MainConfig.getPrintSpawnLocationInChat()) {
            BlockPos pos = serverPlayer.blockPosition();
            Component textCoordinates = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
                    .withStyle(style -> style.withColor(ChatFormatting.GREEN)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
            serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.location.message", textCoordinates), false);
        }
    }
}
