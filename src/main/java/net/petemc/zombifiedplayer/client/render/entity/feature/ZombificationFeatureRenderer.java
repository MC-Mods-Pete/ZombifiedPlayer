package net.petemc.zombifiedplayer.client.render.entity.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;

@OnlyIn(Dist.CLIENT)
public class ZombificationFeatureRenderer
extends RenderLayer<ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(RenderLayerParent<ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int i, ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState, float pPackedLight, float v1) {
    //public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ZombifiedPlayerEntityRenderState state, float limbAngle, float limbDistance) {
        renderColoredCutoutModel(this.getParentModel(), zombifiedPlayerEntityRenderState.skinTexture, pPoseStack, pBuffer, i, zombifiedPlayerEntityRenderState,0xFF99FF99);
        renderColoredCutoutModel(this.getParentModel(), TEXTURE, pPoseStack, pBuffer, i, zombifiedPlayerEntityRenderState,-1);
        //ZombificationFeatureRenderer.renderModel(this.getContextModel(), state.skinTexture, matrices, vertexConsumers, light, state, 0xFF99FF99);
        //ZombificationFeatureRenderer.renderModel(this.getContextModel(), TEXTURE, matrices, vertexConsumers, light, state, -1);
    }
}

