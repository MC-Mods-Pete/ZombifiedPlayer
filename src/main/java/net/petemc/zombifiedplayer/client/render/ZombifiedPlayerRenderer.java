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
import net.minecraft.world.item.component.ResolvableProfile;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.ZombifiedPlayerClient;
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> {

    private static final Identifier TEXTURE_FALLBACK =
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    // Per-UUID async fetch tracking — never shared across entities incorrectly
    private static final Set<UUID> fetchingInProgress = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> retryAfter = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> retryCount = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_MS = 5_000L;
    private static final int  MAX_RETRIES    = 5;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        this(ctx, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer,
                                   ModelLayerLocation legsArmorLayer,
                                   ArmorModelSet<ModelLayerLocation> equipmentModelData,
                                   ArmorModelSet<ModelLayerLocation> equipmentModelData2) {
        super(ctx,
              new ZombieModel<>(ctx.bakeLayer(layer)),
              new ZombieModel<>(ctx.bakeLayer(legsArmorLayer)),
              ArmorModelSet.bake(equipmentModelData, ctx.getModelSet(), ZombieModel::new),
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

    /** Never blocks the render thread — returns fallback texture until the async fetch completes. */
    @Override
    public @NotNull Identifier getTextureLocation(@NotNull ZombifiedPlayerEntityRenderState state) {
        if (state.gameProfile == null) return TEXTURE_FALLBACK;
        UUID uuid = state.gameProfile.id();

        // 1. Direct cache hit
        Identifier skin = ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(uuid);
        if (skin != null) {
            state.skinTexture = skin;
            return skin;
        }

        // 2. UUID mismatch (offline ↔ online UUID)
        UUID remapped = ZombifiedPlayerClient.uuidMissmatches.get(uuid);
        if (remapped != null) {
            skin = ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(remapped);
            if (skin != null) {
                state.skinTexture = skin;
                return skin;
            }
        }

        // 3. Kick off async fetch — no-op if already running
        triggerSkinFetch(state.gameProfile);
        return TEXTURE_FALLBACK;
    }

    // -------------------------------------------------------------------------
    // Async fetch — NEVER calls .get() on the render thread
    // -------------------------------------------------------------------------

    private static void triggerSkinFetch(GameProfile profile) {
        UUID uuid = profile.id();

        if (fetchingInProgress.contains(uuid)) return;

        Long notBefore = retryAfter.get(uuid);
        if (notBefore != null && System.currentTimeMillis() < notBefore) return;

        if (MainConfig.getLimitSkinFetchTries()
                && retryCount.getOrDefault(uuid, 0) >= MAX_RETRIES) return;

        fetchingInProgress.add(uuid);
        ZombifiedPlayer.LOGGER.info("Fetching skin for {} UUID: {}", profile.name(), uuid);

        Minecraft.getInstance()
                .playerSkinRenderCache()
                .lookup(ResolvableProfile.createUnresolved(profile.name()))
                .thenCompose(opt -> {
                    GameProfile resolved = opt
                            .map(PlayerSkinRenderCache.RenderInfo::gameProfile)
                            .orElse(profile);
                    if (!resolved.id().equals(uuid)) {
                        ZombifiedPlayerClient.uuidMissmatches.put(uuid, resolved.id());
                    }
                    return Minecraft.getInstance().getSkinManager().get(resolved);
                })
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        Identifier texture = opt.get().body().texturePath();
                        UUID resolvedUUID = ZombifiedPlayerClient.uuidMissmatches.getOrDefault(uuid, uuid);
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(resolvedUUID, texture);
                        ZombifiedPlayerClient.cachedPlayerSkinsByName.put(profile.name(), texture);
                        ZombifiedPlayer.LOGGER.info("Skin fetched for {}: {}", profile.name(), texture);
                        retryAfter.remove(uuid);
                        retryCount.remove(uuid);
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No skin received for {}, scheduling retry.", profile.name());
                        scheduleRetry(uuid);
                    }
                    fetchingInProgress.remove(uuid);
                })
                .exceptionally(ex -> {
                    ZombifiedPlayer.LOGGER.error("Skin fetch failed for {}: {}", profile.name(), ex.getMessage());
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
