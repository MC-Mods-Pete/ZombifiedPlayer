package net.petemc.zombifiedplayer.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.Config;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerRenderer
        extends AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {

    private static ResourceLocation TEXTURE_FALLBACK = new ResourceLocation("minecraft","textures/entity/player/wide/steve.png");
    private GameProfile receivedGameProfile = null;

    private final int counterSteps = 40;
    private final int maxSubTries = 5;
    private final int maxTotalTries = 5;
    private final int counterMax = 2000 + (counterSteps * maxSubTries);

    private int counter = counterMax;
    private int totalTries = 0;

    public ZombifiedPlayerRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)));
        this.addLayer(new ZombificationFeatureRenderer(this));
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(ZombifiedPlayerEntity entity) {
        if (ZombifiedPlayer.cachedPlayerSkinsByUUID.containsKey(entity.getGameProfile().getId())) {
            return ZombifiedPlayer.cachedPlayerSkinsByUUID.get(entity.getGameProfile().getId());
        }
        if (ZombifiedPlayer.cachedPlayerSkinsByName.containsKey(entity.getGameProfile().getName())) {
            return ZombifiedPlayer.cachedPlayerSkinsByName.get(entity.getGameProfile().getName());
        }
        if (entity.getGameProfile() != null) {
            getPlayerSkinFromGameProfile(entity.getGameProfile());
        }
        return TEXTURE_FALLBACK;
    }

    public void setTexture(ResourceLocation id) {
        TEXTURE_FALLBACK = id;
    }

    public void getPlayerSkinFromGameProfile(GameProfile profile) {
        try {
            if (((counter % counterSteps) == 0) && (counter > (counterMax - (counterSteps * maxSubTries))) && (totalTries < maxTotalTries)) {
                if (receivedGameProfile == null) {
                    if (counter == counterMax) {
                        ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());
                    }

                    SkullBlockEntity.updateGameprofile(profile, owner -> {
                        receivedGameProfile = owner;
                    });

                    if (receivedGameProfile != null) {
                        ZombifiedPlayer.LOGGER.info("Successfully received GameProfile for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        counter = counterMax;
                        totalTries = 0;
                    }
                }

                if (receivedGameProfile != null) {
                    Minecraft minecraft = Minecraft.getInstance();

                    ResourceLocation skinTexture = null;
                    skinTexture = minecraft.getSkinManager().getInsecureSkinLocation(receivedGameProfile);

                    if (skinTexture != null) {
                        if (!receivedGameProfile.getId().equals(profile.getId())) {
                            ZombifiedPlayer.LOGGER.info("The zombified player for {} has a different UUID, using random default skin!", receivedGameProfile.getName());
                        }
                        ZombifiedPlayer.cachedPlayerSkinsByUUID.put(receivedGameProfile.getId(), skinTexture);
                        ZombifiedPlayer.cachedPlayerSkinsByName.put(receivedGameProfile.getName(), skinTexture);
                        if (!receivedGameProfile.getName().equals(profile.getName())) {
                            ZombifiedPlayer.cachedPlayerSkinsByName.put(profile.getName(), skinTexture);
                        }
                        ZombifiedPlayer.LOGGER.info("Successfully received Skin for {}, UUID: {}", receivedGameProfile.getName(), receivedGameProfile.getId());
                        ZombifiedPlayer.LOGGER.info("Skin Texture: {}", skinTexture);
                        counter = counterMax;
                        totalTries = 0;
                        receivedGameProfile = null;
                        return;
                    } else {
                        ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {} yet", receivedGameProfile.getName());
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
