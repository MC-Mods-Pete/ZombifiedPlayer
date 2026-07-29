package net.petemc.zombifiedplayer.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

@me.shedaniel.autoconfig.annotation.Config(name = ZombifiedPlayer.MOD_ID)
public class MainConfig implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static MainConfig INSTANCE;

    public static void init() {
        AutoConfig.register(MainConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(MainConfig.class).getConfig();
    }

    public static boolean getSpawnZombifiedPlayerAfterDeath() {
        return INSTANCE.spawnZombifiedPlayerAfterDeath;
    }

    public static boolean getTransferMainAndOffHandToZombifiedPlayer() { return INSTANCE.transferMainAndOffHandToZombifiedPlayer; }

    public static boolean getTransferArmorToZombifiedPlayer() {
        return INSTANCE.transferArmorToZombifiedPlayer;
    }

    public static boolean getTransferInventoryToZombifiedPlayer() { return INSTANCE.transferInventoryToZombifiedPlayer; }

    public static boolean getTransferCuriosOrTrinketItemsToZombifiedPlayer() { return INSTANCE.transferCuriosOrTrinketItemsToZombifiedPlayer; }

    public static boolean getSpawnOnAnyDeath() {
        return INSTANCE.spawnOnAnyDeath;
    }

    public static boolean getDisplayNameTagForZombifiedPlayer() { return INSTANCE.displayNameTagForZombifiedPlayer; }

    public static boolean getPrintSpawnMessageInChat() {
        return INSTANCE.printSpawnMessageInChat;
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

    public static boolean getMakeTheZombifiedPlayersImmuneToFire() { return INSTANCE.makeTheZombifiedPlayersImmuneToFire; }

    public static boolean getLimitSkinFetchTries() {
        return INSTANCE.limitSkinFetchTries;
    }

    public static boolean getSpawnWhenKilledByInfection() {
        return INSTANCE.spawnWhenKilledByInfection;
    }

    public static boolean getGravestoneCompatibility() {
        return INSTANCE.gravestoneCompatibility;
    }

    public static boolean getUseCustomEyeHeight() {
        return INSTANCE.useCustomEyeHeight;
    }

    public static float getCustomEyeHeight() {
        return INSTANCE.customEyeHeight;
    }

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn after the player gets killed by an Undead | default: true")
    private boolean spawnZombifiedPlayerAfterDeath = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the Main and the Off-Hand of the dead player will be transferred to the zombified player | default: true")
    private boolean transferMainAndOffHandToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the armor of the dead player will be transferred to the zombified player | default: true")
    private boolean transferArmorToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
    private boolean transferInventoryToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
    private boolean transferCuriosOrTrinketItemsToZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn no matter how the player died | default: false")
    private boolean spawnOnAnyDeath = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the zombified player will display the name of the dead player | default: true")
    private boolean displayNameTagForZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a message will be printed out in chat that a zombified player has spawned | default: true")
    private boolean printSpawnMessageInChat = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn location of the zombified player will be printed out in chat | default: false")
    private boolean printSpawnLocationInChat = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players can break through doors | default: true")
    private boolean zombifiedPlayersCanBreakDoors = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players are stronger, faster and have more health | default: false")
    private boolean makeTheZombifiedPlayersStronger = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players are immune to fire and lava | default: false")
    private boolean makeTheZombifiedPlayersImmuneToFire = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the mod will stop trying to fetch the player skin after several unsuccessful tries | default: true")
    private boolean limitSkinFetchTries = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn zombified player after death by infection (Contagion mod needed!) | default: true")
    private boolean spawnWhenKilledByInfection = true;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, no gravestone will be placed when a zombified player spawns (either Gravestones mod or Universal Graves mod needed!) | default: false")
    private boolean gravestoneCompatibility = false;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the zombified player will use a custom eye height | default: false")
    private boolean useCustomEyeHeight = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("The custom eye height of the zombified player (only used if useCustomEyeHeight is true) | default: 1.74")
    private float customEyeHeight = 1.74f;
}
