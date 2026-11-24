package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {

    /** A skin texture and whether it is made for the slim model (3 pixel arms). */
    private record Skin(ResourceLocation texture, boolean slim) {
    }

    // Fallback skins with the matching arm width (Minecraft 1.19.2 has only these two default skins)
    private static final Skin STEVE = new Skin(
            new ResourceLocation("minecraft", "textures/entity/steve.png"), false);
    private static final Skin ALEX = new Skin(
            new ResourceLocation("minecraft", "textures/entity/alex.png"), true);

    /** Marker for "no own skin": skinOf picks Steve or Alex, fixed per UUID (like vanilla does for players without a skin). */
    private static final Skin FALLBACK = new Skin(STEVE.texture(), false);

    /** Resolved skin per stored profile UUID (FALLBACK once we know the account has no custom skin). */
    private static final Map<UUID, Skin> SKINS = new ConcurrentHashMap<>();

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

    private final ZombieModel<ZombifiedPlayerEntity> wideModel;
    private final ZombieModel<ZombifiedPlayerEntity> slimModel;

    /**
     * Own layers with the player mesh, wide or slim arms depending on the skin (zombie models from resource packs
     * do not apply, the built-in shambling animation pack animates these layers).
     */
    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx,
              new ZombifiedPlayerModel(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER), false),
              new ZombieModel<>(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER_INNER_ARMOR)),
              new ZombieModel<>(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER_OUTER_ARMOR)));
        this.wideModel = this.model;
        this.slimModel = new ZombifiedPlayerModel(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER_SLIM), true);
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    @Override
    public void render(@NotNull ZombifiedPlayerEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer, int packedLight) {
        // the layers (zombification, held items) use getModel(), so they follow the switch
        this.model = skinOf(entity).slim() ? this.slimModel : this.wideModel;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ZombifiedPlayerEntity entity) {
        return skinOf(entity).texture();
    }

    /** The loaded skin of the profile, otherwise the fallback skin (and a fetch is started, never blocking). */
    private static Skin skinOf(ZombifiedPlayerEntity entity) {
        GameProfile profile = entity.getGameProfile();
        UUID uuid = profile == null || profile.getId() == null ? entity.getUUID() : profile.getId();
        Skin skin = profile == null || profile.getId() == null ? null : SKINS.get(uuid);
        if (skin == null) {
            if (profile != null && profile.getId() != null) triggerSkinFetch(profile);
            skin = FALLBACK;
        }
        if (skin == FALLBACK) {
            return Math.floorMod(uuid.hashCode(), 2) == 1 ? ALEX : STEVE;
        }
        return skin;
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
            SKINS.put(uuid, FALLBACK);
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

        // Fresh profile: the lookup must not modify the profile of the entity.
        // The callback runs on the client main thread.
        SkullBlockEntity.updateGameprofile(new GameProfile(uuid, name), resolved -> {
            if (!FETCHING.remove(uuid, now)) return; // timed out meanwhile, a newer attempt owns this UUID
            onProfileResolved(uuid, name, resolved);
        });
    }

    private static void onProfileResolved(UUID uuid, String name, GameProfile resolved) {
        if (resolved == null || !resolved.getProperties().containsKey(SkinManager.PROPERTY_TEXTURES)) {
            // No account found or Mojang not reachable, try again later
            onFetchFailed(uuid, name, "profile lookup returned no textures");
            return;
        }

        try {
            SkinManager skinManager = Minecraft.getInstance().getSkinManager();
            MinecraftProfileTexture skin = skinManager.getInsecureSkinInformation(resolved).get(MinecraftProfileTexture.Type.SKIN);
            // Account without custom skin: keep the fallback skin and stop fetching
            Skin resolvedSkin = skin != null
                    ? new Skin(skinManager.registerTexture(skin, MinecraftProfileTexture.Type.SKIN), "slim".equals(skin.getMetadata("model")))
                    : FALLBACK;

            SKINS.put(uuid, resolvedSkin);
            RETRY_AFTER.remove(uuid);
            FAILED_TRIES.remove(uuid);
            ZombifiedPlayer.LOGGER.debug("Skin cached for {} -> {} (slim: {})", name, resolvedSkin.texture(), resolvedSkin.slim());
        } catch (RuntimeException e) {
            onFetchFailed(uuid, name, e.toString());
        }
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
