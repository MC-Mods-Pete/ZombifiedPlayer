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
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.ZombifiedPlayerClient;
import net.petemc.zombifiedplayer.config.ZombifiedPlayerConfig;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    private GameProfile receivedGameProfile = null;

    private final int counterSteps = 30;
    private final int maxSubTries = 3;
    private final int maxTotalTries = 3;
    private final int counterMax = 2000 + (counterSteps * maxSubTries);

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE_OUTER_ARMOR)));
        this.addFeature(new ZombificationFeatureRenderer(this));
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

    public void setTexture(Identifier id) {
        TEXTURE_FALLBACK = id;
    }

    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if (((counter % counterSteps) == 0) && (counter > (counterMax - (counterSteps * maxSubTries))) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());

                    SkullBlockEntity.loadProperties(profile, owner -> {
                        receivedGameProfile = owner;
                    });
                }

                if (receivedGameProfile != null) {
                    ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());

                    MinecraftClient minecraft = MinecraftClient.getInstance();

                    Identifier skinTexture = null;

                    skinTexture = minecraft.getSkinProvider().loadSkin(receivedGameProfile);

                    if (skinTexture != null) {
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture);
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture);
                        counter = counterMax;
                        receivedGameProfile = null;
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {} yet", receivedGameProfile.getName());
                        receivedGameProfile = null;
                    }
                } else {
                    ZombifiedPlayer.LOGGER.warn("No valid GameProfile was received for {} yet", profile.getName());
                }
            }
            if (counter > 0) {
                counter--;
            } else {
                counter = counterMax;
                totalTries++;
                if (totalTries == maxTotalTries - 1) {
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
}
