package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.texture.PlayerSkinCache;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.ZombifiedPlayerClient;
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombieEntityModel<ZombifiedPlayerEntityRenderState>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    private static GameProfile receivedGameProfile;
    private static GameProfile inProgress = null;
    private boolean gameProfileReceived = false;

    private final int maxTotalTries = 5;
    private final int maxSubTries = 10;
    private final int counterMax = 2000 + maxSubTries;

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererFactory.Context ctx) {
        this(ctx, EntityModelLayers.ZOMBIE, EntityModelLayers.ZOMBIE_BABY, EntityModelLayers.ZOMBIE_EQUIPMENT, EntityModelLayers.ZOMBIE_BABY_EQUIPMENT);
        this.addFeature(new ZombificationFeatureRenderer(this));
    }

    public ZombifiedPlayerRenderer(EntityRendererFactory.Context ctx, EntityModelLayer layer, EntityModelLayer legsArmorLayer, EquipmentModelData<EntityModelLayer> equipmentModelData, EquipmentModelData<EntityModelLayer> equipmentModelData2) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(layer)), new ZombieEntityModel<>(ctx.getPart(legsArmorLayer)), EquipmentModelData.mapToEntityModel(equipmentModelData, ctx.getEntityModels(), ZombieEntityModel::new), EquipmentModelData.mapToEntityModel(equipmentModelData2, ctx.getEntityModels(), ZombieEntityModel::new));
    }

    @Override
    public ZombifiedPlayerEntityRenderState createRenderState() {
        return new ZombifiedPlayerEntityRenderState();
    }

    @Override
    public void updateRenderState(ZombifiedPlayerEntity zombifiedPlayerEntity, ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState, float f) {
        super.updateRenderState(zombifiedPlayerEntity, zombifiedPlayerEntityRenderState, f);
        zombifiedPlayerEntityRenderState.gameProfile = zombifiedPlayerEntity.getGameProfile();
    }

    public void setTexture(Identifier id) {
        TEXTURE_FALLBACK = id;
    }

    @Override
    public Identifier getTexture(ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState) {
        if (zombifiedPlayerEntityRenderState.gameProfile != null) {
            if (ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(zombifiedPlayerEntityRenderState.gameProfile.id())) {
                zombifiedPlayerEntityRenderState.skinTexture = ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(zombifiedPlayerEntityRenderState.gameProfile.id());
                return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(zombifiedPlayerEntityRenderState.gameProfile.id());
            }
            if (ZombifiedPlayerClient.uuidMissmatches.containsKey(zombifiedPlayerEntityRenderState.gameProfile.id())) {
                if (ZombifiedPlayerClient.cachedPlayerSkinsByName.containsKey(zombifiedPlayerEntityRenderState.gameProfile.name()) ||
                        ZombifiedPlayerClient.cachedPlayerSkinsByName.containsKey(zombifiedPlayerEntityRenderState.gameProfile.name().toLowerCase())) {
                    zombifiedPlayerEntityRenderState.skinTexture = ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(ZombifiedPlayerClient.uuidMissmatches.get(zombifiedPlayerEntityRenderState.gameProfile.id()));
                    return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(ZombifiedPlayerClient.uuidMissmatches.get(zombifiedPlayerEntityRenderState.gameProfile.id()));
                }
            }
            if (zombifiedPlayerEntityRenderState.gameProfile != null) {
                getPlayerSkinFromGameProfile(zombifiedPlayerEntityRenderState.gameProfile);
            }
        }
        return TEXTURE_FALLBACK;
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
                    MinecraftClient minecraft = MinecraftClient.getInstance();

                    Optional<SkinTextures> optionalSkinTextures;
                    SkinTextures skinTexture = null;
                    optionalSkinTextures = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(100, TimeUnit.MILLISECONDS);
                    int tries = 5;
                    while (!minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            optionalSkinTextures = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(50, TimeUnit.MILLISECONDS);
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
                            ZombifiedPlayerClient.uuidMissmatches.put(profile.id(), receivedGameProfile.id());
                            ZombifiedPlayerClient.cachedPlayerSkinsByName.put(receivedGameProfile.name(), skinTexture.body().texturePath());
                        }
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.id(), skinTexture.body().texturePath());
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
            ProfileComponent profileComponent = ProfileComponent.ofDynamic(profile.name());

            CompletableFuture<Optional<PlayerSkinCache.Entry>> futureOptionalEntry =
                    MinecraftClient.getInstance().getPlayerSkinCache().getFuture(profileComponent);
            Optional<PlayerSkinCache.Entry> optionalEntry = futureOptionalEntry.get(100, TimeUnit.MILLISECONDS);

            int tries = 5;
            while (!futureOptionalEntry.isDone() && (tries > 0)) {
                try {
                    futureOptionalEntry.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException timeoutException) {
                    tries--;
                }
            }

            return optionalEntry.map(PlayerSkinCache.Entry::getProfile).orElse(null);
        } catch (Exception ignored) {
        }
        return null;
    }
}
