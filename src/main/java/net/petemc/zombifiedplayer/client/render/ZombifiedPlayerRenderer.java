package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders the zombified player like vanilla's zombie renderer (AbstractZombieRenderer: same layers, arm poses,
 * shaking while converting), but with the player mesh and the skin of the dead player.
 * A MobRenderer and not an AbstractZombieRenderer: that one picks the adult or baby model on every frame,
 * here the model follows the skin (wide or slim arms).
 */
public class ZombifiedPlayerRenderer
        extends MobRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombifiedPlayerModel> {

    /** A skin texture and whether it is made for the slim model (3 pixel arms). */
    private record Skin(Identifier texture, boolean slim) {
    }

    // Fallback skins with the matching arm width
    private static final Skin STEVE = new Skin(
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png"), false);
    private static final Skin ALEX = new Skin(
            Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/slim/alex.png"), true);

    /** Marker for "no own skin": skinOf picks Steve or Alex, fixed per UUID (like vanilla does for players without a skin). */
    private static final Skin FALLBACK = new Skin(STEVE.texture(), false);

    /** Resolved skin per stored profile UUID (also the fallback once we know the account has no custom skin). */
    private static final Map<UUID, Skin> SKINS = new ConcurrentHashMap<>();

    /** Start time (ms) of the running fetch per UUID, prevents duplicate fetches. */
    private static final Map<UUID, Long> FETCHING = new ConcurrentHashMap<>();

    /** Timestamp (ms) after which a retry is allowed, per UUID. */
    private static final Map<UUID, Long> RETRY_AFTER = new ConcurrentHashMap<>();

    /** Number of failed fetch attempts per UUID. */
    private static final Map<UUID, Integer> FAILED_TRIES = new ConcurrentHashMap<>();

    private static final long RETRY_DELAY_MS = 5_000L;
    private static final long MAX_RETRY_DELAY_MS = 300_000L;
    /** A fetch whose result has not arrived after this time counts as failed. */
    private static final long FETCH_TIMEOUT_MS = 30_000L;
    private static final int MAX_TRIES = 5;

    private final ZombifiedPlayerModel wideModel;
    private final ZombifiedPlayerModel slimModel;

    /** Own layers with the player mesh, wide or slim arms depending on the skin. */
    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ZombifiedPlayerModel(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER), false), 0.5F);
        this.wideModel = this.model;
        this.slimModel = new ZombifiedPlayerModel(ctx.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER_SLIM), true);
        // the layers of HumanoidMobRenderer and AbstractZombieRenderer, in the same order
        this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getPlayerSkinRenderCache()));
        this.addLayer(new WingsLayer<>(this, ctx.getModelSet(), ctx.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.<ZombieModel<ZombifiedPlayerEntityRenderState>>bake(ModModelLayers.ZOMBIFIED_PLAYER_ARMOR, ctx.getModelSet(), ZombieModel::new),
                ctx.getEquipmentRenderer()));
        this.addLayer(new ZombificationFeatureRenderer(this, ctx.getModelSet()));
    }

    @Override
    public @NotNull ZombifiedPlayerEntityRenderState createRenderState() {
        return new ZombifiedPlayerEntityRenderState();
    }

    @Override
    public void extractRenderState(@NotNull ZombifiedPlayerEntity entity,
                                   @NotNull ZombifiedPlayerEntityRenderState state,
                                   float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        // like HumanoidMobRenderer and AbstractZombieRenderer
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTick, this.itemModelResolver);
        state.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);
        state.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
        state.isAggressive = entity.isAggressive();
        state.isConverting = entity.isUnderWaterConverting();

        Skin skin = skinOf(entity);
        state.skinTexture = skin.texture();
        state.slim = skin.slim();
    }

    @Override
    public void submit(@NotNull ZombifiedPlayerEntityRenderState state, @NotNull PoseStack poseStack,
                       @NotNull SubmitNodeCollector submitNodeCollector, @NotNull CameraRenderState camera) {
        // the layers (zombification, held items) use getModel(), so they follow the switch
        this.model = state.slim ? this.slimModel : this.wideModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull ZombifiedPlayerEntityRenderState state) {
        return state.skinTexture;
    }

    @Override
    protected boolean isShaking(@NotNull ZombifiedPlayerEntityRenderState state) {
        return super.isShaking(state) || state.isConverting;
    }

    /** Arm pose like HumanoidMobRenderer (spears; since 26.3 the zombie renderer has no own arm pose). */
    private static HumanoidModel.ArmPose getArmPose(ZombifiedPlayerEntity entity, HumanoidArm arm) {
        return HumanoidMobRenderer.usesSpearPose(entity.getItemHeldByArm(arm), arm, entity)
                ? HumanoidModel.ArmPose.SPEAR : HumanoidModel.ArmPose.EMPTY;
    }

    /** The loaded skin of the profile, otherwise the fallback skin (and a fetch is started, never blocking). */
    private static Skin skinOf(ZombifiedPlayerEntity entity) {
        GameProfile profile = entity.getGameProfile();
        UUID uuid = profile == null || profile.id() == null ? entity.getUUID() : profile.id();
        Skin skin = profile == null || profile.id() == null ? null : SKINS.get(uuid);
        if (skin == null) {
            if (profile != null && profile.id() != null) triggerSkinFetch(profile);
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
        UUID uuid = profile.id();
        String name = profile.name();
        if (name == null || name.isEmpty()) {
            SKINS.put(uuid, FALLBACK);
            return;
        }

        long now = System.currentTimeMillis();

        Long startedAt = FETCHING.get(uuid);
        if (startedAt != null) {
            if (now - startedAt < FETCH_TIMEOUT_MS) return;
            // The result never arrived (lookup hangs in the background)
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
        ProfileResolver resolver = minecraft.services().profileResolver();
        CompletableFuture.supplyAsync(() -> resolver.fetchByName(name), Util.nonCriticalIoPool())
                // Account without custom skin: the skin manager returns the default skin of that account
                .thenCompose(resolved -> resolved.isPresent()
                        ? minecraft.getSkinManager().get(resolved.get())
                        : CompletableFuture.completedFuture(Optional.<PlayerSkin>empty()))
                .whenComplete((skin, error) -> minecraft.execute(() -> {
                    if (!FETCHING.remove(uuid, now)) return;
                    if (error != null || skin == null || skin.isEmpty()) {
                        // No account found or Mojang not reachable, try again later
                        onFetchFailed(uuid, name, error != null ? error.toString() : "no account or skin found");
                        return;
                    }
                    SKINS.put(uuid, new Skin(skin.get().body().texturePath(), skin.get().model() == PlayerModelType.SLIM));
                    RETRY_AFTER.remove(uuid);
                    FAILED_TRIES.remove(uuid);
                    ZombifiedPlayer.LOGGER.debug("Skin cached for {} -> {}", name, skin.get().body().texturePath());
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
