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
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;


@Environment(EnvType.CLIENT)
public class ZombifiedPlayerRenderer
        extends ZombieBaseEntityRenderer<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> {

    private static Identifier TEXTURE_FALLBACK = Identifier.of("minecraft", "textures/entity/steve.png");
    private static GameProfile receivedGameProfile = null;

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
            if (receivedGameProfile == null) {
                ZombifiedPlayer.LOGGER.info("Trying to get GameProfile for {} UUID: {}", profile.getName(), profile.getId());

                SkullBlockEntity.loadProperties(profile, owner -> {
                    ZombifiedPlayerRenderer.receivedGameProfile = owner;
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
                    receivedGameProfile = null;
                } else {
                    ZombifiedPlayer.LOGGER.warn("No valid Skin was received for {}", receivedGameProfile.getName());
                    receivedGameProfile = null;
                }
            } else {
                ZombifiedPlayer.LOGGER.warn("No valid GameProfile was received for {}", profile.getName());
            }
        } catch (Exception ignored) {

        }
    }
}
