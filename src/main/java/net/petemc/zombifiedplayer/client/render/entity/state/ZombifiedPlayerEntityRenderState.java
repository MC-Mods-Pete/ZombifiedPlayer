package net.petemc.zombifiedplayer.client.render.entity.state;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

public class ZombifiedPlayerEntityRenderState extends ZombieRenderState {
	public GameProfile gameProfile = null;
	public Identifier skinTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
}
