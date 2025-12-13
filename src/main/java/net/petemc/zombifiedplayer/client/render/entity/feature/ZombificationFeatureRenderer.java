package net.petemc.zombifiedplayer.client.render.entity.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;

public class ZombificationFeatureRenderer
extends RenderLayer<ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(RenderLayerParent<ZombifiedPlayerEntityRenderState, ZombieModel<ZombifiedPlayerEntityRenderState>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState, float v, float v1) {
        renderColoredCutoutModel(this.getParentModel(), zombifiedPlayerEntityRenderState.skinTexture, poseStack, submitNodeCollector, i, zombifiedPlayerEntityRenderState,0xFF99FF99,0);
        renderColoredCutoutModel(this.getParentModel(), TEXTURE, poseStack, submitNodeCollector, i, zombifiedPlayerEntityRenderState,-1, 1);
    }
}

