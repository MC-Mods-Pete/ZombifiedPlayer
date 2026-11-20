package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.ZombifiedPlayerClient;
import net.petemc.zombifiedplayer.config.MainConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {

    private static ResourceLocation TEXTURE_FALLBACK = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    private static GameProfile receivedGameProfile;
    private static GameProfile inProgress = null;
    private boolean gameProfileReceived = false;

    private final int maxTotalTries = 5;
    private final int maxSubTries = 10;
    private final int counterMax = 2000 + maxSubTries;

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
        this.addLayer(new ZombificationFeatureRenderer(this));
    }

    public void setTexture(ResourceLocation id) {
        TEXTURE_FALLBACK = id;
    }
    
    @Override
    public ResourceLocation getTextureLocation(ZombifiedPlayerEntity entity) {
        if (entity.getGameProfile() != null) {
            if (ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(entity.getGameProfile().getId())) {
                return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(entity.getGameProfile().getId());
            }
            if (ZombifiedPlayerClient.uuidMissmatches.containsKey(entity.getGameProfile().getId())) {
                if (ZombifiedPlayerClient.cachedPlayerSkinsByName.containsKey(entity.gameProfile.getName()) ||
                        ZombifiedPlayerClient.cachedPlayerSkinsByName.containsKey(entity.gameProfile.getName().toLowerCase())) {
                    return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(ZombifiedPlayerClient.uuidMissmatches.get(entity.gameProfile.getId()));
                }
            }
            getPlayerSkinFromGameProfile(entity.getGameProfile());
        }
        return TEXTURE_FALLBACK;
    }


    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if (inProgress == null) {
                inProgress = profile;
            }

            if (!inProgress.getId().equals(profile.getId())) {
                return;
            }

            if ((counter > (counterMax - maxSubTries)) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    if (counter == counterMax) {
                        ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());
                    }

                    receivedGameProfile = getGameProfile(profile);
                }

                if ((!gameProfileReceived) && (receivedGameProfile != null)) {
                    ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                    counter = counterMax;
                    totalTries = 0;
                    gameProfileReceived = true;
                }

                if (receivedGameProfile != null) {
                    Minecraft minecraft = Minecraft.getInstance();

                    PlayerSkin skinTexture = null;
                    skinTexture = minecraft.getSkinManager().getOrLoad(receivedGameProfile).get(100, TimeUnit.MILLISECONDS);
                    int tries = 3;
                    while (!minecraft.getSkinManager().getOrLoad(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            skinTexture = minecraft.getSkinManager().getOrLoad(receivedGameProfile).get(50, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeoutException) {
                            tries--;
                        }
                    }

                    if (skinTexture != null) {
                        if (!receivedGameProfile.getId().equals(profile.getId())) {
                            ZombifiedPlayer.LOGGER.info("The zombified player for {} has a different UUID, using random default skin!", receivedGameProfile.getName());
                            ZombifiedPlayerClient.uuidMissmatches.put(profile.getId(), receivedGameProfile.getId());
                            ZombifiedPlayerClient.cachedPlayerSkinsByName.put(receivedGameProfile.getName(), skinTexture.texture());
                        }
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Skin Texture URL: {}", skinTexture.textureUrl());
                        counter = counterMax;
                        totalTries = 0;
                        receivedGameProfile = null;
                        inProgress = null;
                        gameProfileReceived = false;
                        return;
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {}", receivedGameProfile.getName());
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
                        ZombifiedPlayer.LOGGER.warn("Could not fetch a valid Skin for {}, will stop trying.", profile.getName());
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
            CompletableFuture<Optional<GameProfile>> futureOptionalGameProfile = SkullBlockEntity.fetchGameProfile(profile.getName());
            Optional<GameProfile> optionalGameProfile = futureOptionalGameProfile.get(100, TimeUnit.MILLISECONDS);
            int tries = 5;
            while (!futureOptionalGameProfile.isDone() && (tries > 0)) {
                try {
                    futureOptionalGameProfile.get(50, TimeUnit.MILLISECONDS);
                } catch (TimeoutException timeoutException) {
                    tries--;
                }
            }

            AtomicReference<GameProfile> gameProfile = new AtomicReference<>();
            optionalGameProfile.ifPresent(gameProfile::set);
            return gameProfile.get();
        } catch (Exception ignored) {
        }
        return null;
    }
}
