package net.petemc.zombifiedplayer.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Optional Hardcore Revival integration.
 *
 * We track the original knockout damage source and reuse it when Hardcore Revival
 * finally kills a player with hardcorerevival:not_rescued_in_time.
 */
public final class HardcoreRevivalUtil {

    private static final Map<UUID, DamageSource> knockoutSources = new ConcurrentHashMap<>();
    private static boolean eventsRegistered = false;

    private HardcoreRevivalUtil() {
    }

    public static boolean isHardcoreRevivalLoaded() {
        return FabricLoader.getInstance().isModLoaded("hardcorerevival");
    }

    public static void registerEvents() {
        if (!isHardcoreRevivalLoaded() || eventsRegistered) {
            return;
        }

        try {
            registerEvent("net.blay09.mods.hardcorerevival.api.PlayerKnockedOutEvent", event -> {
                ServerPlayer player = asServerPlayer(callGetter(event, "player"));
                DamageSource source = (DamageSource) callGetter(event, "source");
                if (player != null && source != null) {
                    knockoutSources.put(player.getUUID(), source);
                }
            });

            registerEvent("net.blay09.mods.hardcorerevival.api.PlayerRevivedEvent", event -> {
                ServerPlayer player = asServerPlayer(callGetter(event, "player"));
                if (player != null) {
                    knockoutSources.remove(player.getUUID());
                }
            });

            registerEvent("net.blay09.mods.hardcorerevival.api.PlayerRescuedEvent", event -> {
                ServerPlayer player = asServerPlayer(callGetter(event, "player"));
                if (player != null) {
                    knockoutSources.remove(player.getUUID());
                }
            });

            eventsRegistered = true;
        } catch (Exception e) {
            ZombifiedPlayer.LOGGER.warn("Hardcore Revival detected, but event hook registration failed.", e);
        }
    }

    public static DamageSource resolveEffectiveDeathSource(ServerPlayer player, DamageSource deathSource) {
        if (!isHardcoreRevivalLoaded() || player == null || deathSource == null) {
            return deathSource;
        }

        DamageSource knockoutSource = knockoutSources.get(player.getUUID());
        if (knockoutSource == null) {
            return deathSource;
        }

        return isNotRescuedInTimeDamage(deathSource) ? knockoutSource : deathSource;
    }

    public static void clearTrackedCause(ServerPlayer player) {
        if (player != null) {
            knockoutSources.remove(player.getUUID());
        }
    }

    private static boolean isNotRescuedInTimeDamage(DamageSource source) {
        try {
            return source.typeHolder().unwrapKey()
                    .map(key -> "hardcorerevival:not_rescued_in_time".equals(key.identifier().toString()))
                    .orElse(false);
        } catch (Exception ignored) {
            return "not_rescued_in_time".equals(source.getMsgId());
        }
    }

    private static void registerEvent(String eventClassName, Consumer<Object> handler) throws Exception {
        Class<?> eventClass = Class.forName(eventClassName);
        Field eventField = eventClass.getField("EVENT");
        Object eventMapper = eventField.get(null);
        Method registerMethod = eventMapper.getClass().getMethod("register", Consumer.class);
        registerMethod.invoke(eventMapper, (Consumer<Object>) handler::accept);
    }

    private static Object callGetter(Object instance, String methodName) {
        try {
            Method method = instance.getClass().getMethod(methodName);
            return method.invoke(instance);
        } catch (Exception e) {
            return null;
        }
    }

    private static ServerPlayer asServerPlayer(Object player) {
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
    }
}
