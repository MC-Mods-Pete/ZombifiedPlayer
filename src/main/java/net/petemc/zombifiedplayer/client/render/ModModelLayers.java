package net.petemc.zombifiedplayer.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

/**
 * Own model layers for the zombified player: player mesh (second skin layer, wide or slim arms), armor like the zombie.
 * Model replacing mods like Entity Model Features pick models by layer: with the vanilla zombie layers,
 * every zombie model from a resource pack (e.g. Bray's Zombie Overhaul) was applied to zombified players
 * and the player skin did not fit those models.
 */
@Environment(EnvType.CLIENT)
public final class ModModelLayers {
    private static final ResourceLocation ID = new ResourceLocation(ZombifiedPlayer.MOD_ID, "zombified_player");

    public static final ModelLayerLocation ZOMBIFIED_PLAYER = new ModelLayerLocation(ID, "main");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_SLIM = new ModelLayerLocation(
            new ResourceLocation(ZombifiedPlayer.MOD_ID, "zombified_player_slim"), "main");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_INNER_ARMOR = new ModelLayerLocation(ID, "inner_armor");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_OUTER_ARMOR = new ModelLayerLocation(ID, "outer_armor");

    private ModModelLayers() {
    }

    /** Body like vanilla's PLAYER / PLAYER_SLIM layers, armor like ZOMBIE_INNER_ARMOR / ZOMBIE_OUTER_ARMOR. */
    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER, () -> ZombifiedPlayerModel.createBodyLayer(false));
        EntityModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER_SLIM, () -> ZombifiedPlayerModel.createBodyLayer(true));
        EntityModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER_INNER_ARMOR,
                () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(0.5F)), 64, 32));
        EntityModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER_OUTER_ARMOR,
                () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.0F)), 64, 32));
    }
}
