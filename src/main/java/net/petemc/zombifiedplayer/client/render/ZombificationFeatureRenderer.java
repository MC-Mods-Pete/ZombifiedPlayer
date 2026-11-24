package net.petemc.zombifiedplayer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class ZombificationFeatureRenderer
        extends RenderLayer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(ZombifiedPlayer.MOD_ID,"textures/entity/zombify.png");
    /** zombify.png plus the same marks on the second skin layer (hat, jacket, sleeves, pants), so clothing does not hide them. */
    private static final ResourceLocation TEXTURE_LAYERED = new ResourceLocation(ZombifiedPlayer.MOD_ID,"textures/entity/zombify_layered.png");

    public ZombificationFeatureRenderer(AbstractZombieRenderer<ZombifiedPlayerEntity, ZombieModel<ZombifiedPlayerEntity>> featureRendererContext) {
        super(featureRendererContext);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, @NotNull ZombifiedPlayerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        renderColoredCutoutModel(this.getParentModel(), this.getTextureLocation(pLivingEntity), pPoseStack, pBuffer, pPackedLight, pLivingEntity,0.6f, 1.0f, 0.6f);
        ResourceLocation overlay = this.getParentModel() instanceof ZombifiedPlayerModel ? TEXTURE_LAYERED : TEXTURE;
        renderColoredCutoutModel(this.getParentModel(), overlay, pPoseStack, pBuffer, pPackedLight, pLivingEntity,1.0F, 1.0F, 1.0F);
    }
}

