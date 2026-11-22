package net.petemc.zombifiedplayer.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

/**
 * Own model layers for the zombified player: player mesh (second skin layer, wide or slim arms), armor like the zombie.
 * Model replacing mods like Entity Model Features pick models by layer: with the vanilla zombie layers,
 * every zombie model from a resource pack (e.g. Bray's Zombie Overhaul) was applied to zombified players
 * and the player skin did not fit those models.
 */
@Environment(EnvType.CLIENT)
public final class ModModelLayers {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player");

    public static final ModelLayerLocation ZOMBIFIED_PLAYER = new ModelLayerLocation(ID, "main");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_SLIM = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player_slim"), "main");
    /** Like vanilla's armor layer sets (ModelLayers.ZOMBIE_ARMOR): helmet, chestplate, leggings, boots. */
    public static final ArmorModelSet<ModelLayerLocation> ZOMBIFIED_PLAYER_ARMOR = new ArmorModelSet<>(
            new ModelLayerLocation(ID, "helmet"), new ModelLayerLocation(ID, "chestplate"),
            new ModelLayerLocation(ID, "leggings"), new ModelLayerLocation(ID, "boots"));

    private static final CubeDeformation INNER_ARMOR_DEFORMATION = new CubeDeformation(0.5F);
    private static final CubeDeformation OUTER_ARMOR_DEFORMATION = new CubeDeformation(1.0F);

    private ModModelLayers() {
    }

    /** Body like vanilla's PLAYER / PLAYER_SLIM layers, armor like ZOMBIE_ARMOR. */
    public static void register() {
        ModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER, () -> ZombifiedPlayerModel.createBodyLayer(false));
        ModelLayerRegistry.registerModelLayer(ZOMBIFIED_PLAYER_SLIM, () -> ZombifiedPlayerModel.createBodyLayer(true));
        ModelLayerRegistry.registerArmorModelLayers(ZOMBIFIED_PLAYER_ARMOR,
                () -> HumanoidModel.createArmorMeshSet(INNER_ARMOR_DEFORMATION, OUTER_ARMOR_DEFORMATION)
                        .map(mesh -> LayerDefinition.create(mesh, 64, 32)));
    }
}
