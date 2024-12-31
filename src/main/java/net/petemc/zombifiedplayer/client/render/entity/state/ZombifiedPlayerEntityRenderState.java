package net.petemc.zombifiedplayer.client.render.entity.state;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerEntityRenderState extends ZombieEntityRenderState {
	public GameProfile gameProfile = null;
	public Identifier skinTexture = Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
}
