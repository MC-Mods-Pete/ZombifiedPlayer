package net.petemc.zombifiedplayer.client.render.entity.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.ModModelLayers;
import net.petemc.zombifiedplayer.client.render.ZombifiedPlayerModel;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;

public class ZombificationFeatureRenderer
extends RenderLayer<ZombifiedPlayerEntityRenderState, ZombifiedPlayerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    // marks only on the body itself: on the second skin layer they would float above the body wherever the skin has no clothing
    private final ZombifiedPlayerModel wideMarksModel;
    private final ZombifiedPlayerModel slimMarksModel;

    public ZombificationFeatureRenderer(RenderLayerParent<ZombifiedPlayerEntityRenderState, ZombifiedPlayerModel> featureRendererContext, EntityModelSet modelSet) {
        super(featureRendererContext);
        this.wideMarksModel = new ZombifiedPlayerModel(modelSet.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER), false, false);
        this.slimMarksModel = new ZombifiedPlayerModel(modelSet.bakeLayer(ModModelLayers.ZOMBIFIED_PLAYER_SLIM), true, false);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, ZombifiedPlayerEntityRenderState zombifiedPlayerEntityRenderState, float v, float v1) {
        renderColoredCutoutModel(this.getParentModel(), zombifiedPlayerEntityRenderState.skinTexture, poseStack, submitNodeCollector, i, zombifiedPlayerEntityRenderState,0xFF99FF99,0);
        // The player mesh reads the own marks of the left arm and leg from zombify.png,
        // the zombie model mirrored the right ones (legacy 64x32 layout).
        ZombifiedPlayerModel marksModel = zombifiedPlayerEntityRenderState.slim ? this.slimMarksModel : this.wideMarksModel;
        renderColoredCutoutModel(marksModel, TEXTURE, poseStack, submitNodeCollector, i, zombifiedPlayerEntityRenderState,-1, 1);
    }
}
