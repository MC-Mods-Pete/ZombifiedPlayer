package net.petemc.zombifiedplayer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

@Environment(value=EnvType.CLIENT)
public class ZombificationFeatureRenderer
extends RenderLayer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(RenderLayerParent<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, ZombifiedPlayerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        renderColoredCutoutModel(this.getParentModel(), this.getTextureLocation(pLivingEntity), pPoseStack, pBuffer, pPackedLight, pLivingEntity,0.6f, 1.0f, 0.6f);
        // The player mesh reads the own marks of the left arm and leg from zombify.png,
        // the zombie model mirrors the right ones (legacy 64x32 layout).
        if (this.getParentModel() instanceof ZombifiedPlayerModel playerModel) {
            // marks only on the body itself: on the second skin layer they would float above the body wherever the skin has no clothing
            boolean[] previous = playerModel.hideSecondLayer();
            try {
                renderColoredCutoutModel(playerModel, TEXTURE, pPoseStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
            } finally {
                playerModel.restoreSecondLayer(previous);
            }
        } else {
            renderColoredCutoutModel(this.getParentModel(), TEXTURE, pPoseStack, pBuffer, pPackedLight, pLivingEntity, 1.0F, 1.0F, 1.0F);
        }
    }
}

