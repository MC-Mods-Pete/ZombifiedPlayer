package net.petemc.zombifiedplayer.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;

@Environment(value=EnvType.CLIENT)
public class ZombificationFeatureRenderer
extends FeatureRenderer<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> {
    private static final Identifier TEXTURE = Identifier.of(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(FeatureRendererContext<ZombifiedPlayerEntity, ZombieEntityModel<ZombifiedPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, ZombifiedPlayerEntity zombifiedPlayerEntity, float f, float g, float h, float j, float k, float l) {
        ZombificationFeatureRenderer.renderModel(this.getContextModel(), this.getTexture(zombifiedPlayerEntity), matrixStack, vertexConsumerProvider, i, zombifiedPlayerEntity, 0xFF99FF99);
        ZombificationFeatureRenderer.renderModel(this.getContextModel(), TEXTURE, matrixStack, vertexConsumerProvider, i, zombifiedPlayerEntity, -1);
    }
}

