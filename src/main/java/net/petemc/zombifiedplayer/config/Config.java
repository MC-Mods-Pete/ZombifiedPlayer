package net.petemc.zombifiedplayer.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

@me.shedaniel.autoconfig.annotation.Config(name = ZombifiedPlayer.MOD_ID)
public class Config implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static Config INSTANCE;

    public static void init() {
        AutoConfig.register(Config.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(Config.class).getConfig();
    }

    public static boolean getSpawnZombifiedPlayerAfterDeath() {
        return INSTANCE.spawnZombifiedPlayerAfterDeath;
    }

    public static boolean getTransferMainandOffHandToZombifiedPlayer() {
        return INSTANCE.transferMainandOffHandToZombifiedPlayer;
    }

    public static boolean getTransferArmorToZombifiedPlayer() {
        return INSTANCE.transferArmorToZombifiedPlayer;
    }

    public static boolean getTransferInventoryToZombifiedPlayer() {
        return INSTANCE.transferInventoryToZombifiedPlayer;
    }

    public static boolean getSpawnOnAnyDeath() {
        return INSTANCE.spawnOnAnyDeath;
    }

    public static boolean getPrintSpawnLocationInChat() {
        return INSTANCE.printSpawnLocationInChat;
    }

    public static boolean getZombifiedPlayersCanBreakDoors() {
        return INSTANCE.zombifiedPlayersCanBreakDoors;
    }

    public static boolean getMakeTheZombifiedPlayersStronger() {
        return INSTANCE.makeTheZombifiedPlayersStronger;
    }

    public static boolean getLimitSkinFetchTries() {
        return INSTANCE.limitSkinFetchTries;
    }

    public static boolean getSpawnWhenKilledByInfection() {
        return false;
    }

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn after the player gets killed by an Undead | default: true")
    private boolean spawnZombifiedPlayerAfterDeath = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the Main and the Off Hand of the dead player will be transferred to the zombified player | default: true")
    private boolean transferMainandOffHandToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the armor of the dead player will be transferred to the zombified player | default: true")
    private boolean transferArmorToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
    private boolean transferInventoryToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn no matter how the player died | default: false")
    private boolean spawnOnAnyDeath = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn location of the zombified player will printed out in chat | default: false")
    private boolean printSpawnLocationInChat = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players can break through doors | default: true")
    private boolean zombifiedPlayersCanBreakDoors = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players are stronger, faster and have more health | default: false")
    private boolean makeTheZombifiedPlayersStronger = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the mod will stop trying to fetch the player skin after several unsuccessful tries | default: true")
    private boolean limitSkinFetchTries = true;
}
