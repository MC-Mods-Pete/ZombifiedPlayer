package net.petemc.zombifiedplayer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ZombificationFeatureRenderer
        extends RenderLayer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");

    public ZombificationFeatureRenderer(AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, @NotNull ZombifiedPlayerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        renderColoredCutoutModel(this.getParentModel(), this.getTextureLocation(pLivingEntity), pPoseStack, pBuffer, pPackedLight, pLivingEntity,0xFF99FF99);
        renderColoredCutoutModel(this.getParentModel(), TEXTURE, pPoseStack, pBuffer, pPackedLight, pLivingEntity,-1);
        //coloredCutoutModelCopyLayerRender();
    }
}

