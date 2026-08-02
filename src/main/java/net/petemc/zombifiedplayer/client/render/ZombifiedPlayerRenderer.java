package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {

    private static final ResourceLocation TEXTURE_FALLBACK =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    /** UUIDs for which a fetch is currently running — prevents duplicate fetches. */
    private static final Set<UUID> FETCHING = ConcurrentHashMap.newKeySet();

    /** Timestamp (ms) after which a retry is allowed, per UUID. */
    private static final Map<UUID, Long> RETRY_AFTER = new ConcurrentHashMap<>();

    /** Number of completed fetch attempts per UUID. */
    private static final Map<UUID, Integer> RETRY_COUNT = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_MS = 5_000L;
    private static final int  MAX_RETRIES    = 5;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
              new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)),
              new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
              new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ZombifiedPlayerEntity entity) {
        GameProfile profile = entity.getGameProfile();
        if (profile == null) return TEXTURE_FALLBACK;

        UUID uuid = profile.getId();

        // 1. Direct UUID cache hit
        ResourceLocation cached = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(uuid);
        if (cached != null) return cached;

        // 2. UUID remapped (offline-mode mismatch)
        UUID remapped = ZombifiedPlayer.uuidMissmatches.get(uuid);
        if (remapped != null) {
            ResourceLocation remappedSkin = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(remapped);
            if (remappedSkin != null) return remappedSkin;
        }

        // 3. Trigger async fetch — never blocks the render thread
        triggerSkinFetch(profile);

        return TEXTURE_FALLBACK;
    }

    /**
     * Starts an async skin fetch for the given profile.
     * Safe to call every frame — no-ops if a fetch is already running for this UUID,
     * the retry delay has not elapsed, or the attempt limit has been reached.
     * Never blocks the calling (render) thread.
     */
    private static void triggerSkinFetch(GameProfile profile) {
        UUID uuid = profile.getId();

        if (FETCHING.contains(uuid)) return;

        Long notBefore = RETRY_AFTER.get(uuid);
        if (notBefore != null && System.currentTimeMillis() < notBefore) return;

        int tries = RETRY_COUNT.getOrDefault(uuid, 0);
        if (MainConfig.getLimitSkinFetchTries() && tries >= MAX_RETRIES) return;

        if (!FETCHING.add(uuid)) return;

        ZombifiedPlayer.LOGGER.info("Fetching skin for {} (UUID: {}, attempt {})",
                profile.getName(), uuid, tries + 1);

        // Step 1: resolve full GameProfile with texture properties
        SkullBlockEntity.fetchGameProfile(profile.getName())
                .thenCompose(optFull -> {
                    GameProfile resolved = optFull.orElse(profile);

                    if (!resolved.getId().equals(uuid)) {
                        ZombifiedPlayer.LOGGER.info("UUID mismatch for {}: stored={} resolved={}",
                                profile.getName(), uuid, resolved.getId());
                        ZombifiedPlayer.uuidMissmatches.put(uuid, resolved.getId());
                    }

                    // Step 2: load the actual skin texture
                    return Minecraft.getInstance().getSkinManager().getOrLoad(resolved);
                })
                .thenAccept(skin -> {
                    ResourceLocation texture = skin.texture();
                    UUID resolvedUUID = ZombifiedPlayer.uuidMissmatches.getOrDefault(uuid, uuid);
                    ZombifiedPlayer.cachedPlayerSkinsByUUID.put(resolvedUUID, texture);
                    ZombifiedPlayer.cachedPlayerSkinsByName.put(profile.getName(), texture);

                    ZombifiedPlayer.LOGGER.info("Skin cached for {} -> {}", profile.getName(), texture);
                    RETRY_AFTER.remove(uuid);
                    RETRY_COUNT.remove(uuid);
                    FETCHING.remove(uuid);
                })
                .exceptionally(ex -> {
                    ZombifiedPlayer.LOGGER.warn("Skin fetch failed for {}: {}", profile.getName(), ex.getMessage());
                    RETRY_AFTER.put(uuid, System.currentTimeMillis() + RETRY_DELAY_MS);
                    RETRY_COUNT.merge(uuid, 1, Integer::sum);
                    FETCHING.remove(uuid);
                    return null;
                });
    }
}
