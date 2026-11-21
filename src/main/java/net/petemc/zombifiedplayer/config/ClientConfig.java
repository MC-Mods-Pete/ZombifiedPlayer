package net.petemc.zombifiedplayer.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

/**
 * Client only settings (config/zombifiedplayer-client.json5). Registered in onInitializeClient, which runs before the
 * game creates its options and selects the resource packs, so it is always loaded when BuiltinPacks needs it.
 */
@Environment(EnvType.CLIENT)
@me.shedaniel.autoconfig.annotation.Config(name = ZombifiedPlayer.MOD_ID + "-client")
public class ClientConfig implements ConfigData {
    private static ConfigHolder<ClientConfig> holder;

    @Comment("Set by the mod after it turned on the built-in resource pack 'Zombified Player: Shambling Animation' (needs Entity Model Features) at the first start. Turn the pack on or off under Options > Resource Packs. Set this to false to have the pack turned on again at the next start | default: false")
    private boolean shamblingAnimationPackTurnedOn = false;

    public static void init() {
        holder = AutoConfig.register(ClientConfig.class, JanksonConfigSerializer::new);
    }

    /** Whether the built-in shambling animation pack was already turned on once (true if the config is missing: leave the packs alone). */
    public static boolean getShamblingAnimationPackTurnedOn() {
        return holder == null || holder.getConfig().shamblingAnimationPackTurnedOn;
    }

    public static void setShamblingAnimationPackTurnedOn() {
        if (holder != null) {
            holder.getConfig().shamblingAnimationPackTurnedOn = true;
            holder.save();
        }
    }
}
