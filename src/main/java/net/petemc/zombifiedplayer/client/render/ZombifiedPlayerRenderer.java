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
import net.petemc.zombifiedplayer.client.render.entity.feature.ZombificationFeatureRenderer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombifiedPlayerEntityRenderState, ZombieEntityModel<ZombifiedPlayerEntityRenderState>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    private GameProfile receivedGameProfile;

    private final int maxTotalTries = 5;
    private final int maxSubTries = 10;
    private final int counterMax = 2000 + maxSubTries;

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererFactory.Context ctx) {
        super(
                ctx,
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY)),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY_INNER_ARMOR)),
                new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_BABY_OUTER_ARMOR))
        );
        this.addFeature(new ZombificationFeatureRenderer(this));
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
    public Identifier getTexture(ZombifiedPlayerEntityRenderState zombieEntityRenderState) {
        if (zombieEntityRenderState.gameProfile != null) {
            if (!ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(zombieEntityRenderState.gameProfile.getId())) {
                receivedGameProfile = null;
                getPlayerSkinFromGameProfile(zombieEntityRenderState.gameProfile);
            }
            if (ZombifiedPlayerClient.cachedPlayerSkinsByUUID.containsKey(zombieEntityRenderState.gameProfile.getId())) {
                zombieEntityRenderState.skinTexture = ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(zombieEntityRenderState.gameProfile.getId());
                return ZombifiedPlayerClient.cachedPlayerSkinsByUUID.get(zombieEntityRenderState.gameProfile.getId());
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

                    Optional<SkinTextures> optionalSkinTextures;
                    SkinTextures skinTexture = null;
                    optionalSkinTextures = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(300, TimeUnit.MILLISECONDS);
                    int tries = 5;
                    while (!minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).isDone() && (tries > 0)) {
                        try {
                            optionalSkinTextures = minecraft.getSkinProvider().fetchSkinTextures(receivedGameProfile).get(300, TimeUnit.MILLISECONDS);
                        } catch (TimeoutException timeoutException) {
                            tries--;
                        }
                    }

                    if (optionalSkinTextures.isPresent()) {
                        skinTexture = optionalSkinTextures.get();
                    }


                    if (skinTexture != null) {
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture.texture());
                        ZombifiedPlayer.LOGGER.info("Skin Texture URL: {}", skinTexture.textureUrl());
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {}", receivedGameProfile.getName());
                        receivedGameProfile = null;
                    }
                } else {
                    ZombifiedPlayer.LOGGER.warn("No valid GameProfile was received for {}", profile.getName());
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
