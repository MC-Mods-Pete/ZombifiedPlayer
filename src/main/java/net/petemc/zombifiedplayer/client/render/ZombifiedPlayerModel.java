package net.petemc.zombifiedplayer.client.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.petemc.zombifiedplayer.entity.ZombifiedPlayerEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Zombie model (zombie arm pose) built from the player mesh: second skin layer (jacket, sleeves, pants)
 * and, for skins with the slim model, 3 pixel wide arms. Left arm and leg use their own skin area
 * instead of the mirrored right one like the zombie model does.
 */
@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerModel extends ZombieModel<ZombifiedPlayerEntity> {
    private final boolean slim;
    private final ModelPart jacket;
    private final ModelPart leftSleeve;
    private final ModelPart rightSleeve;
    private final ModelPart leftPants;
    private final ModelPart rightPants;

    public ZombifiedPlayerModel(ModelPart root, boolean slim) {
        super(root);
        this.slim = slim;
        this.jacket = root.getChild("jacket");
        this.leftSleeve = root.getChild("left_sleeve");
        this.rightSleeve = root.getChild("right_sleeve");
        this.leftPants = root.getChild("left_pants");
        this.rightPants = root.getChild("right_pants");
    }

    /** Same mesh as vanilla's player layers (ModelLayers.PLAYER / PLAYER_SLIM). */
    public static LayerDefinition createBodyLayer(boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64);
    }

    @Override
    protected @NotNull Iterable<ModelPart> bodyParts() {
        return Iterables.concat(super.bodyParts(), ImmutableList.of(this.leftPants, this.rightPants, this.leftSleeve, this.rightSleeve, this.jacket));
    }

    @Override
    public void setupAnim(@NotNull ZombifiedPlayerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        this.leftSleeve.visible = visible;
        this.rightSleeve.visible = visible;
        this.leftPants.visible = visible;
        this.rightPants.visible = visible;
        this.jacket.visible = visible;
    }

    /** Like PlayerModel: items in the hand of slim arms sit half a pixel further in. */
    @Override
    public void translateToHand(@NotNull HumanoidArm side, @NotNull PoseStack poseStack) {
        ModelPart arm = this.getArm(side);
        if (this.slim) {
            float offset = 0.5F * (side == HumanoidArm.RIGHT ? 1 : -1);
            arm.x += offset;
            arm.translateAndRotate(poseStack);
            arm.x -= offset;
        } else {
            arm.translateAndRotate(poseStack);
        }
    }
}
