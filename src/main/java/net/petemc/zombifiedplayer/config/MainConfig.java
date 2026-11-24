package net.petemc.zombifiedplayer.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.ArrayList;
import java.util.List;

@me.shedaniel.autoconfig.annotation.Config(name = ZombifiedPlayer.MOD_ID)
public class MainConfig implements ConfigData
{
    @ConfigEntry.Gui.Excluded
    public static MainConfig INSTANCE;

    @ConfigEntry.Gui.Excluded
    private static final List<String> DEFAULT_RANDOM_SKIN_PLAYER_NAMES = List.of("jeb_", "Dinnerbone", "XxRexRaptorxX", "McModsPete");

    public static void init() {
        AutoConfig.register(MainConfig.class, (definition, configClass) -> new JanksonConfigSerializer<>(definition, configClass) {
            // New file, reset button and defaults of the config screen
            @Override
            public MainConfig createDefault() {
                MainConfig config = super.createDefault();
                config.applyDefaultLists();
                return config;
            }
        });
        INSTANCE = AutoConfig.getConfigHolder(MainConfig.class).getConfig();
    }

    /** Lists missing in the file (or a new config) get their default values. */
    private void applyDefaultLists() {
        if (randomSkinPlayerNames == null) {
            randomSkinPlayerNames = new ArrayList<>(DEFAULT_RANDOM_SKIN_PLAYER_NAMES);
        }
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

    public static boolean getIgnoreKeepInventory() {
        return INSTANCE.ignoreKeepInventory;
    }

    public static boolean getSpawnOnAnyDeath() {
        return INSTANCE.spawnOnAnyDeath;
    }

    public static boolean getDisplayNameTagForZombifiedPlayer() { return INSTANCE.displayNameTagForZombifiedPlayer; }

    /** Only valid player names (letters, digits, underscore, 1 to 16 characters), others are ignored. */
    public static List<String> getRandomSkinPlayerNames() {
        return INSTANCE.randomSkinPlayerNames.stream().filter(MainConfig::isValidPlayerName).toList();
    }

    private static boolean isValidPlayerName(String name) {
        return name != null && name.matches("[A-Za-z0-9_]{1,16}");
    }

    public static boolean getNaturalSpawning() {
        return INSTANCE.naturalSpawning;
    }

    public static int getNaturalSpawnWeight() {
        return INSTANCE.naturalSpawnWeight;
    }

    public static boolean getDisplayNameTagForNaturalZombifiedPlayers() {
        return INSTANCE.displayNameTagForNaturalZombifiedPlayers;
    }

    /** Lists missing in the file get their defaults. Values edited by hand in the file: weight back into its range. */
    @Override
    public void validatePostLoad() {
        applyDefaultLists();
        naturalSpawnWeight = Math.max(1, Math.min(100, naturalSpawnWeight));
    }

    public static boolean getPrintSpawnMessageInChat() {
        return INSTANCE.printSpawnMessageInChat;
    }

    public static boolean getPrintSpawnLocationInChat() {
        return INSTANCE.printSpawnLocationInChat;
    }

    public static boolean getZombifiedPlayersCanAlwaysBreakDoors() {
        return INSTANCE.zombifiedPlayersCanAlwaysBreakDoors;
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

    public static boolean getSpawnWhenDiedWhileInfected() {
        return INSTANCE.spawnWhenDiedWhileInfected;
    }

    public static boolean getInfectiousModCompatibility() {
        return INSTANCE.infectiousModCompatibility;
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
    @Comment("If true, the items are transferred to the zombified player even if the game rule keepInventory is on. If false, the player keeps the items with keepInventory and the zombified player spawns without them | default: false")
    private boolean ignoreKeepInventory = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a zombified player will spawn no matter how the player died | default: false")
    private boolean spawnOnAnyDeath = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, the zombified player will display the name of the dead player | default: true")
    private boolean displayNameTagForZombifiedPlayer = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("Player names for zombified players that do not come from a player death (/summon, spawn egg, spawner). One name is picked at random, its skin is used. Empty: Steve | default: [\"jeb_\", \"Dinnerbone\", \"XxRexRaptorxX\", \"McModsPete\"]")
    // No default list here: Jankson (in Cloth Config) adds the names from the file to a list that already exists
    // instead of replacing it, the defaults would be added again on every start. applyDefaultLists sets them.
    private List<String> randomSkinPlayerNames = null;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players also spawn naturally in the dark wherever zombies spawn. They get a skin from randomSkinPlayerNames, drop their equipment only by chance like zombies and despawn like zombies. Only applied when the world is loaded | default: false")
    private boolean naturalSpawning = false;

    @ConfigEntry.Gui.Tooltip()
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    @Comment("Spawn weight of naturally spawning zombified players, compared to zombies (95) and zombie villagers (5) in most biomes. Only applied when the world is loaded | default: 5")
    private int naturalSpawnWeight = 5;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, naturally spawned zombified players display the name tag \"Zombified <name>\" (needs displayNameTagForZombifiedPlayer) | default: false")
    private boolean displayNameTagForNaturalZombifiedPlayers = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, a message will be printed out in chat that a zombified player has spawned | default: true")
    private boolean printSpawnMessageInChat = true;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn location of the zombified player will be printed out in chat | default: false")
    private boolean printSpawnLocationInChat = false;

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, zombified players can break through wooden doors on every difficulty. If false, only on hard difficulty like zombies (both need the game rule mobGriefing) | default: false")
    private boolean zombifiedPlayersCanAlwaysBreakDoors = false;

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

    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn zombified player when the player dies while infected, no matter how (e.g. fall damage), not only from the infection itself (Contagion mod needed!) | default: false")
    private boolean spawnWhenDiedWhileInfected = false;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Gui.Tooltip()
    @Comment("If true, spawn zombified player when killed by Infectious Zombies (Infectious mod needed!) | default: false")
    private boolean infectiousModCompatibility = false;

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
