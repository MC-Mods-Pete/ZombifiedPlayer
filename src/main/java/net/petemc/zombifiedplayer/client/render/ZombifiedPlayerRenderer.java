package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.ZombifiedPlayerClient;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    private GameProfile receivedGameProfile;

    private final int maxTotalTries = 5;
    private final int maxSubTries = 10;
    private final int counterMax = 2000 + maxSubTries;

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)));
        this.addFeature(new ZombificationFeatureRenderer(this));
    }

    public void setTexture(Identifier id) {
        TEXTURE_FALLBACK = id;
    }
    
    @Override
    public Identifier getTexture(ZombifiedPlayerEntity entity) {
        if (entity.getGameProfile() != null) {
            if (!ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(entity.getGameProfile().getId())) {
                getPlayerSkinFromGameProfile(entity.getGameProfile());
            } else if (ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(entity.getGameProfile().getId())) {
                return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(entity.getGameProfile().getId());
            }
        }
        return TEXTURE_FALLBACK;
    }


    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if ((counter > (counterMax - maxSubTries)) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());

                    receivedGameProfile = getGameProfile(profile);

                    if (receivedGameProfile != null) {
                        counter = 0;
                        ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                    } else {
                        counter--;
                    }
                }

                if (receivedGameProfile != null) {

                    MinecraftClient minecraft = MinecraftClient.getInstance();

                    SkinTextures skinTexture = null;
                    skinTexture = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(300, TimeUnit.MILLISECONDS);
                    int tries = 3;
                    while (!minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            skinTexture = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(300, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeoutException) {
                            tries--;
                        }
                    }

                    if (skinTexture != null) {
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Skin Texture URL: {}", skinTexture.textureUrl());
                        receivedGameProfile = null;
                        counter = 0;
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {}", receivedGameProfile.getName());
                        counter--;
                    }
                }
            }
            if (counter > 0) {
                counter--;
            } else {
                counter = counterMax;
                totalTries++;
                if (totalTries == (maxTotalTries - 1)) {
                    if (ZombifiedPlayerConfig.INSTANCE.limitSkinFetchTries) {
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
            CompletableFuture<Optional<GameProfile>> futureOptionalGameProfile = SkullBlockEntity.fetchProfileByName(profile.getName());
            Optional<GameProfile> optionalGameProfile = futureOptionalGameProfile.get(300, TimeUnit.MILLISECONDS);
            int tries = 5;
            while (!futureOptionalGameProfile.isDone() && (tries > 0)) {
                try {
                    futureOptionalGameProfile.get(300, TimeUnit.MILLISECONDS);
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
