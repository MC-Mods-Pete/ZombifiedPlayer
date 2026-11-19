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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {

    private static final ResourceLocation TEXTURE_FALLBACK =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");

    /** Resolved skin per stored profile UUID (also the fallback once we know the account has no custom skin). */
    private static final Map<UUID, ResourceLocation> SKINS = new ConcurrentHashMap<>();

    /** Start time (ms) of the running fetch per UUID, prevents duplicate fetches. */
    private static final Map<UUID, Long> FETCHING = new ConcurrentHashMap<>();

    /** Timestamp (ms) after which a retry is allowed, per UUID. */
    private static final Map<UUID, Long> RETRY_AFTER = new ConcurrentHashMap<>();

    /** Number of failed fetch attempts per UUID. */
    private static final Map<UUID, Integer> FAILED_TRIES = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_MS = 5_000L;
    private static final long MAX_RETRY_DELAY_MS = 300_000L;
    /** A fetch whose callback has not arrived after this time counts as failed. */
    private static final long FETCH_TIMEOUT_MS = 30_000L;
    private static final int MAX_TRIES = 5;

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
        if (profile == null || profile.getId() == null) return TEXTURE_FALLBACK;

        ResourceLocation cached = SKINS.get(profile.getId());
        if (cached != null) return cached;

        // Never blocks the render thread
        triggerSkinFetch(profile);

        return TEXTURE_FALLBACK;
    }

    /**
     * Starts an async skin fetch for the given profile.
     * Safe to call every frame: no-op if a fetch is already running for this UUID,
     * the retry delay has not elapsed, or the attempt limit has been reached.
     *
     * The profile is looked up by name (like player heads do), so on offline-mode servers,
     * where the stored UUID does not match the Mojang account, the skin of the account
     * with that name is used.
     */
    private static void triggerSkinFetch(GameProfile profile) {
        UUID uuid = profile.getId();
        String name = profile.getName();
        if (name == null || name.isEmpty()) {
            SKINS.put(uuid, TEXTURE_FALLBACK);
            return;
        }

        long now = System.currentTimeMillis();

        Long startedAt = FETCHING.get(uuid);
        if (startedAt != null) {
            if (now - startedAt < FETCH_TIMEOUT_MS) return;
            // The callback never arrived (lookup failed in the background)
            if (FETCHING.remove(uuid, startedAt)) {
                onFetchFailed(uuid, name, "no response");
            }
            return;
        }

        Long notBefore = RETRY_AFTER.get(uuid);
        if (notBefore != null && now < notBefore) return;

        int failedTries = FAILED_TRIES.getOrDefault(uuid, 0);
        if (MainConfig.getLimitSkinFetchTries() && failedTries >= MAX_TRIES) return;

        if (FETCHING.putIfAbsent(uuid, now) != null) return;

        ZombifiedPlayer.LOGGER.debug("Fetching skin for {} (UUID: {}, attempt {})", name, uuid, failedTries + 1);

        // Results are handled on the client main thread; a fetch that timed out meanwhile is ignored
        // (a newer attempt owns this UUID then).
        Minecraft minecraft = Minecraft.getInstance();
        SkullBlockEntity.fetchGameProfile(name).whenComplete((resolved, error) -> minecraft.execute(() -> {
            if (!Objects.equals(FETCHING.get(uuid), now)) return;
            if (error != null || resolved.isEmpty() || !resolved.get().getProperties().containsKey("textures")) {
                // No account found or Mojang not reachable, try again later
                FETCHING.remove(uuid, now);
                onFetchFailed(uuid, name, error != null ? error.toString() : "profile lookup returned no textures");
                return;
            }
            // Account without custom skin: the skin manager returns the default skin of that account
            minecraft.getSkinManager().getOrLoad(resolved.get()).whenComplete((skin, skinError) -> minecraft.execute(() -> {
                if (!FETCHING.remove(uuid, now)) return;
                if (skinError != null || skin == null) {
                    onFetchFailed(uuid, name, skinError != null ? skinError.toString() : "no skin");
                    return;
                }
                SKINS.put(uuid, skin.texture());
                RETRY_AFTER.remove(uuid);
                FAILED_TRIES.remove(uuid);
                ZombifiedPlayer.LOGGER.debug("Skin cached for {} -> {}", name, skin.texture());
            }));
        }));
    }

    private static void onFetchFailed(UUID uuid, String name, String reason) {
        int failedTries = FAILED_TRIES.merge(uuid, 1, Integer::sum);
        long delay = Math.min(RETRY_DELAY_MS << Math.min(failedTries - 1, 6), MAX_RETRY_DELAY_MS);
        RETRY_AFTER.put(uuid, System.currentTimeMillis() + delay);

        if (MainConfig.getLimitSkinFetchTries() && failedTries >= MAX_TRIES) {
            ZombifiedPlayer.LOGGER.info("Giving up fetching the skin for {} after {} tries ({})", name, failedTries, reason);
        } else {
            ZombifiedPlayer.LOGGER.debug("Skin fetch for {} failed ({}), retrying in {} s", name, reason, delay / 1000);
        }
    }
}
