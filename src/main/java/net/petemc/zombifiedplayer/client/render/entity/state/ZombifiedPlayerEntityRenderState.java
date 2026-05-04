package net.petemc.zombifiedplayer.client.render.entity.state;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerEntityRenderState extends ZombieRenderState {
	public GameProfile gameProfile = null;
	public Identifier skinTexture = Identifier.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
}
