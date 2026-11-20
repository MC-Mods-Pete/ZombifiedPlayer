package net.petemc.zombifiedplayer.client.render;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

/**
 * Own model layers for the zombified player (geometry like the vanilla zombie).
 * Model replacing mods like Entity Model Features pick models by layer: with the vanilla zombie layers,
 * every zombie model from a resource pack (e.g. Bray's Zombie Overhaul) was applied to zombified players
 * and the player skin did not fit those models.
 */
@OnlyIn(Dist.CLIENT)
public final class ModModelLayers {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player");

    public static final ModelLayerLocation ZOMBIFIED_PLAYER = new ModelLayerLocation(ID, "main");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_INNER_ARMOR = new ModelLayerLocation(ID, "inner_armor");
    public static final ModelLayerLocation ZOMBIFIED_PLAYER_OUTER_ARMOR = new ModelLayerLocation(ID, "outer_armor");

    private ModModelLayers() {
    }

    /** Same definitions as vanilla's LayerDefinitions for ZOMBIE, ZOMBIE_INNER_ARMOR and ZOMBIE_OUTER_ARMOR. */
    public static void register(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ZOMBIFIED_PLAYER,
                () -> LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64));
        event.registerLayerDefinition(ZOMBIFIED_PLAYER_INNER_ARMOR,
                () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(0.5F)), 64, 32));
        event.registerLayerDefinition(ZOMBIFIED_PLAYER_OUTER_ARMOR,
                () -> LayerDefinition.create(HumanoidArmorModel.createBodyLayer(new CubeDeformation(1.0F)), 64, 32));
    }
}
