package net.petemc.zombifiedplayer.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.util.HardcoreRevivalUtil;
import net.petemc.zombifiedplayer.util.ZombifiedPlayerSpawnLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sends the "You have been zombified" chat notification AFTER the vanilla
 * death message has been broadcast. The actual spawn already happened earlier
 * in ServerLivingEntityEvents.ALLOW_DEATH (before dropAllDeathLoot).
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {

    @Inject(
        method = "die",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            shift = At.Shift.AFTER
        )
    )
    public void die(DamageSource pCause, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        DamageSource effectiveDamageSource = HardcoreRevivalUtil.resolveEffectiveDeathSource(serverPlayer, pCause);

        if (ZombifiedPlayerSpawnLogic.shouldSpawn(serverPlayer, effectiveDamageSource)) {
            if (MainConfig.getPrintSpawnMessageInChat()) {
                serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.spawn.message"), false);

                if (MainConfig.getPrintSpawnLocationInChat()) {
                    BlockPos blockpos = serverPlayer.getOnPos();
                    Component textCoordinates = ComponentUtils.wrapInSquareBrackets(
                            Component.translatable("chat.coordinates", blockpos.getX(), (blockpos.getY() + 1), blockpos.getZ()))
                            .withStyle((style) -> style
                                    .withColor(ChatFormatting.GREEN)
                                    .withClickEvent(new ClickEvent.SuggestCommand(
                                            "/tp @s " + blockpos.getX() + " " + (blockpos.getY() + 1) + " " + blockpos.getZ()))
                                    .withHoverEvent(new HoverEvent.ShowText(
                                            Component.translatable("chat.coordinates.tooltip"))));
                    serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.location.message", textCoordinates), false);
                }
            }
        }
    }
}
