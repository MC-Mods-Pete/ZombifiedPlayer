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
import net.petemc.zombifiedplayer.config.Config;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;


@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/steve.png");
    private GameProfile receivedGameProfile = null;
    private static GameProfile inProgress = null;
    private boolean gameProfileReceived = false;

    private final int counterSteps = 40;
    private final int maxSubTries = 5;
    private final int maxTotalTries = 5;
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

    public void setTexture(Identifier id) {
        TEXTURE_FALLBACK = id;
    }

    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if (inProgress == null) {
                inProgress = profile;
            }

            if (!inProgress.getId().equals(profile.getId())) {
                return;
            }

            if (((counter % counterSteps) == 0) && (counter > (counterMax - (counterSteps * maxSubTries))) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    if (counter == counterMax) {
                        ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());
                    }

                    SkullBlockEntity.loadProperties(profile, owner -> {
                        receivedGameProfile = owner;
                    });
                }

                if ((!gameProfileReceived) && (receivedGameProfile != null)) {
                    ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                    counter = counterMax;
                    totalTries = 0;
                    gameProfileReceived = true;
                }

                if (receivedGameProfile != null) {
                    MinecraftClient minecraft = MinecraftClient.getInstance();

                    Identifier skinTexture = null;
                    skinTexture = minecraft.getSkinProvider().loadSkin(receivedGameProfile);

                    if (skinTexture != null) {
                        if (!receivedGameProfile.getId().equals(profile.getId())) {
                            ZombifiedPlayer.LOGGER.info("The zombified player for {} has a different UUID, using random default skin!", receivedGameProfile.getName());
                            ZombifiedPlayerClient.uuidMissmatches.put(profile.getId(), receivedGameProfile.getId());
                            ZombifiedPlayerClient.cachedPlayerSkinsByName.put(receivedGameProfile.getName(), skinTexture);
                        }
                        ZombifiedPlayerClient.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture);
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture);
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
                if (totalTries == maxTotalTries - 1) {
                    if (Config.getLimitSkinFetchTries()) {
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
