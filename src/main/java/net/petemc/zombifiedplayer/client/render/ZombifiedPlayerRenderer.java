package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> {

    private static final Identifier TEXTURE_FALLBACK =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    /** UUIDs for which a fetch is currently running — prevents duplicate fetches. */
    private static final java.util.Set<UUID> fetchingInProgress = ConcurrentHashMap.newKeySet();

    /** Timestamp (ms) after which a retry is allowed, per UUID. */
    private static final Map<UUID, Long> retryAfter = new ConcurrentHashMap<>();

    /** Number of completed fetch attempts per UUID. */
    private static final Map<UUID, Integer> retryCount = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_MS = 5_000L; // 5 s between retries
    private static final int  MAX_RETRIES    = 5;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        this(ctx, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx,
                                   ModelLayerLocation layer,
                                   ModelLayerLocation legsArmorLayer,
                                   ArmorModelSet<ModelLayerLocation> equipmentModelData,
                                   ArmorModelSet<ModelLayerLocation> equipmentModelData2) {
        super(ctx,
              new ZombieModel<>(ctx.bakeLayer(layer)),
              new ZombieModel<>(ctx.bakeLayer(legsArmorLayer)),
              ArmorModelSet.bake(equipmentModelData,  ctx.getModelSet(), ZombieModel::new),
              ArmorModelSet.bake(equipmentModelData2, ctx.getModelSet(), ZombieModel::new));
    }

    @Override
    public @NotNull ZombifiedPlayerEntityRenderState createRenderState() {
        return new ZombifiedPlayerEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull ZombifiedPlayerEntity entity,
                                   @NotNull ZombifiedPlayerEntityRenderState reusedState,
                                   float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.gameProfile = entity.getGameProfile();
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull ZombifiedPlayerEntityRenderState state) {
        if (state.gameProfile == null) return TEXTURE_FALLBACK;

        UUID uuid = state.gameProfile.id();

        // 1. Skin cached directly by UUID
        Identifier skin = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(uuid);
        if (skin != null) {
            state.skinTexture = skin;
            return skin;
        }

        // 2. UUID remapped (offline player whose UUID differs from Mojang UUID)
        UUID remappedUUID = ZombifiedPlayer.uuidMissmatches.get(uuid);
        if (remappedUUID != null) {
            skin = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(remappedUUID);
            if (skin != null) {
                state.skinTexture = skin;
                return skin;
            }
        }

        // 3. Trigger async fetch — never blocks the render thread
        triggerSkinFetch(state.gameProfile);

        return TEXTURE_FALLBACK;
    }

    /**
     * Starts an async skin fetch for the given profile.
     * Safe to call every frame — no-ops if a fetch is already running for this UUID
     * or if the retry delay / attempt limit has been reached.
     * Never blocks the calling (render) thread.
     */
    private static void triggerSkinFetch(GameProfile profile) {
        UUID uuid = profile.id();

        // Already fetching
        if (fetchingInProgress.contains(uuid)) return;

        // Retry delay not elapsed yet
        Long notBefore = retryAfter.get(uuid);
        if (notBefore != null && System.currentTimeMillis() < notBefore) return;

        // Max retries reached (only when config option is enabled)
        int tries = retryCount.getOrDefault(uuid, 0);
        if (MainConfig.getLimitSkinFetchTries() && tries >= MAX_RETRIES) return;

        fetchingInProgress.add(uuid);
        ZombifiedPlayer.LOGGER.info("Fetching skin for {} (UUID: {}, attempt {})",
                profile.name(), uuid, tries + 1);

        ResolvableProfile resolvableProfile = ResolvableProfile.createUnresolved(profile.name());

        Minecraft.getInstance()
                .playerSkinRenderCache()
                .lookup(resolvableProfile)
                .thenCompose(optionalEntry -> {
                    // Resolve the full GameProfile (may differ for offline-mode servers)
                    GameProfile resolved = optionalEntry
                            .map(PlayerSkinRenderCache.RenderInfo::gameProfile)
                            .orElse(profile);

                    if (!resolved.id().equals(uuid)) {
                        ZombifiedPlayer.LOGGER.info(
                                "UUID mismatch for {}: stored={} resolved={}",
                                profile.name(), uuid, resolved.id());
                        ZombifiedPlayer.uuidMissmatches.put(uuid, resolved.id());
                    }

                    return Minecraft.getInstance().getSkinManager().get(resolved);
                })
                .thenAccept(optionalSkin -> {
                    if (optionalSkin.isPresent()) {
                        PlayerSkin skin = optionalSkin.get();
                        Identifier texture = skin.body().texturePath();

                        UUID resolvedUUID = ZombifiedPlayer.uuidMissmatches.getOrDefault(uuid, uuid);
                        ZombifiedPlayer.cachedPlayerSkinsByUUID.put(resolvedUUID, texture);
                        ZombifiedPlayer.cachedPlayerSkinsByName.put(profile.name(), texture);

                        ZombifiedPlayer.LOGGER.info("Skin loaded for {} -> {}", profile.name(), texture);
                        // Success — reset retry state
                        retryAfter.remove(uuid);
                        retryCount.remove(uuid);
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No skin available for {}", profile.name());
                        scheduleRetry(uuid);
                    }
                    fetchingInProgress.remove(uuid);
                })
                .exceptionally(ex -> {
                    ZombifiedPlayer.LOGGER.warn("Skin fetch failed for {}: {}", profile.name(), ex.getMessage());
                    fetchingInProgress.remove(uuid);
                    scheduleRetry(uuid);
                    return null;
                });
    }

    private static void scheduleRetry(UUID uuid) {
        retryAfter.put(uuid, System.currentTimeMillis() + RETRY_DELAY_MS);
        retryCount.merge(uuid, 1, Integer::sum);
    }
}
