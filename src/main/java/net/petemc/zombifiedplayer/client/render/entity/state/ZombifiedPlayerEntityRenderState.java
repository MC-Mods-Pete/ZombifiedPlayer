package net.petemc.zombifiedplayer.client.render.entity.state;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;

@Environment(EnvType.CLIENT)
public class ZombifiedPlayerEntityRenderState extends ZombieEntityRenderState {
	public GameProfile gameProfile = null;
}
