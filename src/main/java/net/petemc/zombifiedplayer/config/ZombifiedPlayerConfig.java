package net.petemc.zombifiedplayer.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

@Config(name = ZombifiedPlayer.MOD_ID)
public class ZombifiedPlayerConfig implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static ZombifiedPlayerConfig INSTANCE;

    public static void init() {
        AutoConfig.register(ZombifiedPlayerConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ZombifiedPlayerConfig.class).getConfig();
    }

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn after death (with the players inventory) | default: true")
    public boolean spawnZombifiedPlayerAfterDeath = true;
}
