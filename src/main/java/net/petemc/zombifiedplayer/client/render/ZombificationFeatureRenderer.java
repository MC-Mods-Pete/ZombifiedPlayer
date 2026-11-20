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
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(RenderLayerParent<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, ZombifiedPlayerEntity zombifiedPlayerEntity, float f, float g, float h, float j, float k, float l) {
        ZombificationFeatureRenderer.renderColoredCutoutModel(this.getParentModel(), this.getTextureLocation(zombifiedPlayerEntity), matrixStack, vertexConsumerProvider, i, zombifiedPlayerEntity, 0xFF99FF99);
        ZombificationFeatureRenderer.renderColoredCutoutModel(this.getParentModel(), TEXTURE, matrixStack, vertexConsumerProvider, i, zombifiedPlayerEntity, -1);
    }
}

