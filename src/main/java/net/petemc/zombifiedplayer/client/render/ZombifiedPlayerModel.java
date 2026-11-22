package net.petemc.zombifiedplayer.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.world.entity.HumanoidArm;
import net.petemc.zombifiedplayer.client.render.entity.state.ZombifiedPlayerEntityRenderState;
import org.jetbrains.annotations.NotNull;

/**
 * Zombie model (zombie arm pose) built from the player mesh: second skin layer (jacket, sleeves, pants)
 * and, for skins with the slim model, 3 pixel wide arms. Left arm and leg use their own skin area
 * instead of the mirrored right one like the zombie model does. Like vanilla's player mesh,
 * the second layer parts are children of head, body, arms and legs and follow them.
 */
public class ZombifiedPlayerModel extends ZombieModel<ZombifiedPlayerEntityRenderState> {
    private final boolean slim;
    private final boolean showSecondLayer;
    private final ModelPart[] secondLayer;

    public ZombifiedPlayerModel(ModelPart root, boolean slim) {
        this(root, slim, true);
    }

    /**
     * @param showSecondLayer false for a model without the second skin layer (hat, jacket, sleeves, pants), e.g. for the
     *                        zombification marks: models are drawn after all layers were submitted, so the layer cannot
     *                        hide parts of the shared model for its own pass
     */
    public ZombifiedPlayerModel(ModelPart root, boolean slim, boolean showSecondLayer) {
        super(root);
        this.slim = slim;
        this.showSecondLayer = showSecondLayer;
        this.secondLayer = new ModelPart[]{this.hat, this.body.getChild("jacket"),
                this.leftArm.getChild("left_sleeve"), this.rightArm.getChild("right_sleeve"),
                this.leftLeg.getChild("left_pants"), this.rightLeg.getChild("right_pants")};
    }

    /** Same mesh as vanilla's player layers (ModelLayers.PLAYER / PLAYER_SLIM). */
    public static LayerDefinition createBodyLayer(boolean slim) {
        return LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64);
    }

    @Override
    public void setupAnim(@NotNull ZombifiedPlayerEntityRenderState state) {
        super.setupAnim(state);
        for (ModelPart part : this.secondLayer) {
            part.visible = this.showSecondLayer;
        }
    }

    /** Like PlayerModel: items in the hand of slim arms sit half a pixel further in. */
    @Override
    public void translateToHand(@NotNull ZombifiedPlayerEntityRenderState state, @NotNull HumanoidArm side, @NotNull PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
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
