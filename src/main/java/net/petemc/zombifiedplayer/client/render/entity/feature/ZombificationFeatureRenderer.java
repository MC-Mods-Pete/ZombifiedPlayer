/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.petemc.zombifiedplayer.client.render.entity.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;

@Environment(value=EnvType.CLIENT)
public class ZombificationFeatureRenderer
extends FeatureRenderer<ZombifiedPlayerEntityRenderState, ZombieEntityModel<ZombifiedPlayerEntityRenderState>> {
    private static final Identifier TEXTURE = Identifier.of(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(FeatureRendererContext<ZombifiedPlayerEntityRenderState, ZombieEntityModel<ZombifiedPlayerEntityRenderState>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ZombifiedPlayerEntityRenderState state, float limbAngle, float limbDistance) {
        ZombificationFeatureRenderer.renderModel(this.getContextModel(), TEXTURE, matrices, vertexConsumers, light, state, -1);
    }
}

