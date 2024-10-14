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
    @Comment("If true, a zombified player will spawn after death | default: true")
    public boolean spawnZombifiedPlayerAfterDeath = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the Main and the Off Hand of the dead player will be transferred to the zombified player | default: true")
    public boolean transferMainandOffHandToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the armor of the dead player will be transferred to the zombified player | default: true")
    public boolean transferArmorToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
    public boolean transferInventoryToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will only spawn if the player was killed by an undead mob | default: true")
    public boolean onlyOnKilledByUndead = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players can break through doors | default: true")
    public boolean zombifiedPlayersCanBreakDoors = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players are stronger, faster and have more health | default: false")
    public boolean makeTheZombifiedPlayersStronger = false;
}
