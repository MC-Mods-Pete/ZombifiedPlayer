package net.petemc.zombifiedplayer.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.event.PlayerDeathEvents;
import net.petemc.zombifiedplayer.util.ModCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin
{
    @Shadow @Final public MinecraftServer server;

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", shift = At.Shift.AFTER))
    public void die(DamageSource pCause, CallbackInfo ci)
    {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        if ((MainConfig.getSpawnOnAnyDeath() ||
                (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection()) ||
                (PlayerDeathEvents.attackerIsUndead(pCause.getEntity()) && MainConfig.getSpawnZombifiedPlayerAfterDeath()))) {
            if (MainConfig.getPrintSpawnMessageInChat()) {
                serverPlayer.sendSystemMessage(Component.translatable("zombifiedplayer.spawn.message"));
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
