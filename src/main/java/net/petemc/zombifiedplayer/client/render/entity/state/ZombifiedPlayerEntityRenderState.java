package net.petemc.zombifiedplayer.client.render.entity.state;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ZombifiedPlayerEntityRenderState extends ZombieRenderState {
	public GameProfile gameProfile = null;
	public ResourceLocation skinTexture = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
}
