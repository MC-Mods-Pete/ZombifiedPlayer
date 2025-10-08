package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> {

    private static ResourceLocation TEXTURE_FALLBACK = ResourceLocation.fromNamespaceAndPath("minecraft","textures/entity/player/wide/steve.png");
    private static GameProfile receivedGameProfile = null;
    private static GameProfile inProgress = null;
    private boolean gameProfileReceived = false;

    private final int counterSteps = 40;
    private final int maxSubTries = 5;
    private final int maxTotalTries = 5;
    private final int counterMax = 2000 + (counterSteps * maxSubTries);

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        this(ctx, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_BABY, ModelLayers.ZOMBIE_ARMOR, ModelLayers.ZOMBIE_BABY_ARMOR);
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ArmorModelSet<ModelLayerLocation> equipmentModelData, ArmorModelSet<ModelLayerLocation> equipmentModelData2) {
        super(ctx, new ZombieModel<>(ctx.bakeLayer(layer)), new ZombieModel<>(ctx.bakeLayer(legsArmorLayer)), ArmorModelSet.bake(equipmentModelData, ctx.getModelSet(), ZombieModel::new), ArmorModelSet.bake(equipmentModelData2, ctx.getModelSet(), ZombieModel::new));
    }


    @Override
    public @NotNull ZombifiedPlayerEntityRenderState createRenderState() {
        return new ZombifiedPlayerEntityRenderState();
    }


    @Override
    public void extractRenderState(@NotNull ZombifiedPlayerEntity entity, @NotNull ZombifiedPlayerEntityRenderState reusedState, float partialTick) {
        super.extractRenderState(entity, reusedState, partialTick);
        reusedState.gameProfile = entity.getGameProfile();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState) {
        if (zombifiedPlayerEntityRenderState.gameProfile != null) {
            if (ZombifiedPlayer.cachedPlayerSkinsByUUID.containsKey(zombifiedPlayerEntityRenderState.gameProfile.id())) {
                zombifiedPlayerEntityRenderState.skinTexture = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(zombifiedPlayerEntityRenderState.gameProfile.id());
                return ZombifiedPlayer.cachedPlayerSkinsByUUID.get(zombifiedPlayerEntityRenderState.gameProfile.id());
            }
            if (ZombifiedPlayer.uuidMissmatches.containsKey(zombifiedPlayerEntityRenderState.gameProfile.id())) {
                if (ZombifiedPlayer.cachedPlayerSkinsByName.containsKey(zombifiedPlayerEntityRenderState.gameProfile.name()) ||
                        ZombifiedPlayer.cachedPlayerSkinsByName.containsKey(zombifiedPlayerEntityRenderState.gameProfile.name().toLowerCase())) {
                    zombifiedPlayerEntityRenderState.skinTexture = ZombifiedPlayer.cachedPlayerSkinsByUUID.get(ZombifiedPlayer.uuidMissmatches.get(zombifiedPlayerEntityRenderState.gameProfile.id()));
                    return ZombifiedPlayer.cachedPlayerSkinsByUUID.get(ZombifiedPlayer.uuidMissmatches.get(zombifiedPlayerEntityRenderState.gameProfile.id()));
                }
            }
            if (zombifiedPlayerEntityRenderState.gameProfile != null) {
                getPlayerSkinFromGameProfile(zombifiedPlayerEntityRenderState.gameProfile);
            }
        }
        return TEXTURE_FALLBACK;
    }

    public void setTexture(ResourceLocation id) {
        TEXTURE_FALLBACK = id;
    }

    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if (inProgress == null) {
                inProgress = profile;
            }

            if (!inProgress.id().equals(profile.id())) {
                return;
            }

            if ((counter > (counterMax - maxSubTries)) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    if (counter == counterMax) {
                        ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.name(), profile.id());
                    }

                    receivedGameProfile = getGameProfile(profile);
                }

                if ((!gameProfileReceived) && (receivedGameProfile != null)) {
                    ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.name(), receivedGameProfile.id());
                    counter = counterMax;
                    totalTries = 0;
                    gameProfileReceived = true;
                }

                if (receivedGameProfile != null) {
                    Minecraft minecraft = Minecraft.getInstance();

                    Optional<PlayerSkin> optionalSkinTextures;
                    PlayerSkin skinTexture = null;
                    optionalSkinTextures = minecraft.getSkinManager().get(receivedGameProfile).get(100, TimeUnit.MILLISECONDS);
                    int tries = 5;
                    while (!minecraft.getSkinManager().get(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            optionalSkinTextures = minecraft.getSkinManager().get(receivedGameProfile).get(50, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeoutException) {
                            tries--;
                        }
                    }

                    if (optionalSkinTextures.isPresent()) {
                        skinTexture = optionalSkinTextures.get();
                    }

                    if (skinTexture != null) {
                        if (!receivedGameProfile.id().equals(profile.id())) {
                            ZombifiedPlayer.LOGGER.info("The zombified player for {} has a different UUID, using random default skin!", receivedGameProfile.name());
                            ZombifiedPlayer.uuidMissmatches.put(profile.id(), receivedGameProfile.id());
                            ZombifiedPlayer.cachedPlayerSkinsByName.put(receivedGameProfile.name(), skinTexture.body().texturePath());
                        }
                        ZombifiedPlayer.cachedPlayerSkinsByUUID.put(receivedGameProfile.id(), skinTexture.body().texturePath());
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.name(), receivedGameProfile.id());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture.body().id());
                        ZombifiedPlayer.LOGGER.info("Skin Texture URL: {}", skinTexture.body().texturePath());
                        counter = counterMax;
                        totalTries = 0;
                        receivedGameProfile = null;
                        inProgress = null;
                        gameProfileReceived = false;
                        return;
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {}", receivedGameProfile.name());
                    }
                }
            }
            if (counter > 0) {
                counter--;
            } else {
                counter = counterMax;
                totalTries++;
                if (totalTries == (maxTotalTries - 1)) {
                    if (MainConfig.getLimitSkinFetchTries()) {
                        ZombifiedPlayer.LOGGER.warn("Could not fetch a valid Skin for {}, will stop trying.", profile.name());
                    } else {
                        totalTries = 0;
                    }
                }
            }
        } catch (Exception ignored) {

        }
    }

    private GameProfile getGameProfile(GameProfile profile) {
        try {
            ResolvableProfile profileComponent = ResolvableProfile.createUnresolved(profile.name());

            CompletableFuture<Optional<PlayerSkinRenderCache.RenderInfo>> futureOptionalEntry =
                    Minecraft.getInstance().playerSkinRenderCache().lookup(profileComponent);
            Optional<PlayerSkinRenderCache.RenderInfo> optionalEntry = futureOptionalEntry.get(100, TimeUnit.MILLISECONDS);

            int tries = 5;
            while (!futureOptionalEntry.isDone() && (tries > 0)) {
                try {
                    futureOptionalEntry.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException timeoutException) {
                    tries--;
                }
            }

            return optionalEntry.map(PlayerSkinRenderCache.RenderInfo::gameProfile).orElse(null);
        } catch (Exception ignored) {
        }

        return null;
    }
}
