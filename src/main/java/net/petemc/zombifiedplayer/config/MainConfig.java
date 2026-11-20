package net.petemc.zombifiedplayer.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.List;

@EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MainConfig
{
    public static boolean getSpawnZombifiedPlayerAfterDeath() {
        return spawnZombifiedPlayerAfterDeath;
    }

    public static boolean getTransferMainAndOffHandToZombifiedPlayer() {
        return transferMainAndOffHandToZombifiedPlayer;
    }

    public static boolean getTransferArmorToZombifiedPlayer() {
        return transferArmorToZombifiedPlayer;
    }

    public static boolean getTransferInventoryToZombifiedPlayer() {
        return transferInventoryToZombifiedPlayer;
    }

    public static boolean getTransferCuriosOrTrinketItemsToZombifiedPlayer() {
        return transferCuriosOrTrinketItemsToZombifiedPlayer;
    }

    public static boolean getIgnoreKeepInventory() {
        return ignoreKeepInventory;
    }

    public static boolean getSpawnOnAnyDeath() {
        return spawnOnAnyDeath;
    }

    public static boolean getDisplayNameTagForZombifiedPlayer() {
        return displayNameTagForZombifiedPlayer;
    }

    public static List<String> getRandomSkinPlayerNames() {
        return randomSkinPlayerNames;
    }

    public static boolean getNaturalSpawning() {
        return naturalSpawning;
    }

    public static int getNaturalSpawnWeight() {
        return naturalSpawnWeight;
    }

    public static boolean getDisplayNameTagForNaturalZombifiedPlayers() {
        return displayNameTagForNaturalZombifiedPlayers;
    }

    public static boolean getPrintSpawnMessageInChat() {
        return printSpawnMessageInChat;
    }

    public static boolean getPrintSpawnLocationInChat() {
        return printSpawnLocationInChat;
    }

    public static boolean getZombifiedPlayersCanAlwaysBreakDoors() {
        return zombifiedPlayersCanAlwaysBreakDoors;
    }

    public static boolean getMakeTheZombifiedPlayersStronger() {
        return makeTheZombifiedPlayersStronger;
    }

    public static boolean getMakeTheZombifiedPlayersImmuneToFire() {
        return makeTheZombifiedPlayersImmuneToFire;
    }

    public static boolean getLimitSkinFetchTries() {
        return limitSkinFetchTries;
    }

    public static boolean getSpawnWhenKilledByInfection() {
        return spawnWhenKilledByInfection;
    }

    public static boolean getSpawnWhenDiedWhileInfected() {
        return spawnWhenDiedWhileInfected;
    }

    public static boolean getCorpseCompatibility() {
        return corpseCompatibility;
    }

    public static boolean getGravestoneCompatibility() {
        return gravestoneCompatibility;
    }

    public static boolean getUseCustomEyeHeight() {
        return useCustomEyeHeight;
    }

    public static float getCustomEyeHeight() {
        return customEyeHeight;
    }

    public static boolean getInfectiousModCompatibility() {
        return infectiousModCompatibility;
    }

    // Server Config
    private static final ModConfigSpec.Builder BUILDER_SERVER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SPAWN_ZOMBIFIED_PLAYER_AFTER_DEATH = BUILDER_SERVER
            .comment("If true, a zombified player will spawn after the player gets killed by an Undead | default: true")
            .define("spawnZombifiedPlayerAfterDeath", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_MAIN_AND_OFF_HAND_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the Main and the OffHand of the dead player will be transferred to the zombified player | default: true")
            .define("transferMainAndOffHandToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_ARMOR_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the armor of the dead player will be transferred to the zombified player | default: true")
            .define("transferArmorToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_INVENTORY_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
            .define("transferInventoryToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_CURIOS_TRINKET_ITEMS_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, all equipped curios/trinket items of the dead player will be transferred to the zombified player | default: true")
            .define("transferCuriosOrTrinketItemsToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue IGNORE_KEEP_INVENTORY = BUILDER_SERVER
            .comment("If true, the items are transferred to the zombified player even if the game rule keepInventory is on. If false, the player keeps the items with keepInventory and the zombified player spawns without them | default: false")
            .define("ignoreKeepInventory", false);

    private static final ModConfigSpec.BooleanValue SPAWN_ON_ANY_DEATH = BUILDER_SERVER
            .comment("If true, a zombified player will spawn no matter how the player died | default: false")
            .define("spawnOnAnyDeath", false);

    private static final ModConfigSpec.BooleanValue DISPLAY_NAME_TAG_FOR_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the name tag will be displayed above the Zombified Player | default: true")
            .define("displayNameTagForZombifiedPlayer", true);

    private static final List<String> DEFAULT_RANDOM_SKIN_PLAYER_NAMES = List.of("jeb_", "Dinnerbone", "XxRexRaptorxX", "McModsPete");

    private static final ModConfigSpec.ConfigValue<List<? extends String>> RANDOM_SKIN_PLAYER_NAMES = BUILDER_SERVER
            .comment("Player names for zombified players that do not come from a player death (/summon, spawn egg, spawner). One name is picked at random, its skin is used. Empty: Steve | default: [\"jeb_\", \"Dinnerbone\", \"XxRexRaptorxX\", \"McModsPete\"]")
            .defineListAllowEmpty("randomSkinPlayerNames", () -> DEFAULT_RANDOM_SKIN_PLAYER_NAMES, () -> "", MainConfig::isValidPlayerName);

    private static final ModConfigSpec.BooleanValue NATURAL_SPAWNING = BUILDER_SERVER
            .comment("If true, zombified players also spawn naturally in the dark wherever zombies spawn. They get a skin from randomSkinPlayerNames, drop their equipment only by chance like zombies and despawn like zombies. Only applied when the world is loaded | default: false")
            .define("naturalSpawning", false);

    private static final ModConfigSpec.IntValue NATURAL_SPAWN_WEIGHT = BUILDER_SERVER
            .comment("Spawn weight of naturally spawning zombified players, compared to zombies (95) and zombie villagers (5) in most biomes. Only applied when the world is loaded | default: 5")
            .defineInRange("naturalSpawnWeight", 5, 1, 100);

    private static final ModConfigSpec.BooleanValue DISPLAY_NAME_TAG_FOR_NATURAL_ZOMBIFIED_PLAYERS = BUILDER_SERVER
            .comment("If true, naturally spawned zombified players display the name tag \"Zombified <name>\" (needs displayNameTagForZombifiedPlayer) | default: false")
            .define("displayNameTagForNaturalZombifiedPlayers", false);

    private static final ModConfigSpec.BooleanValue PRINT_SPAWN_MESSAGE_IN_CHAT = BUILDER_SERVER
            .comment("If true, a message will be printed out in chat that a zombified player has spawned | default: true")
            .define("printSpawnMessageInChat", true);

    private static final ModConfigSpec.BooleanValue PRINT_SPAWN_LOCATION_IN_CHAT = BUILDER_SERVER
            .comment("If true, the spawn location of the zombified player will be printed out in chat | default: false")
            .define("printSpawnLocationInChat", false);

    private static final ModConfigSpec.BooleanValue ZOMBIFIED_PLAYERS_CAN_ALWAYS_BREAK_DOORS = BUILDER_SERVER
            .comment("If true, zombified players can break through wooden doors on every difficulty. If false, only on hard difficulty like zombies (both need the game rule mobGriefing) | default: false")
            .define("zombifiedPlayersCanAlwaysBreakDoors", false);

    private static final ModConfigSpec.BooleanValue MAKE_THE_ZOMBIFIED_PLAYERS_STRONGER = BUILDER_SERVER
            .comment("If true, zombified players are stronger, faster and have more health | default: false")
            .define("makeTheZombifiedPlayersStronger", false);

    private static final ModConfigSpec.BooleanValue MAKE_THE_ZOMBIFIED_PLAYERS_IMMUNE_TO_FIRE = BUILDER_SERVER
            .comment("If true, zombified players are immune to fire and lava | default: false")
            .define("makeTheZombifiedPlayersImmuneToFire", false);

    private static final ModConfigSpec.BooleanValue LIMIT_SKIN_FETCH_TRIES = BUILDER_SERVER
            .comment("If true, the mod will stop trying to fetch the player skin after several unsuccessful tries | default: true")
            .define("limitSkinFetchTries", true);

    private static final ModConfigSpec.BooleanValue SPAWN_WHEN_KILLED_BY_INFECTION = BUILDER_SERVER
            .comment("If true, spawn zombified player after death by infection (Contagion mod needed!) | default: true")
            .define("spawnWhenKilledByInfection", true);

    private static final ModConfigSpec.BooleanValue SPAWN_WHEN_DIED_WHILE_INFECTED = BUILDER_SERVER
            .comment("If true, spawn zombified player when the player dies while infected, no matter how (e.g. fall damage), not only from the infection itself (Contagion mod needed!) | default: false")
            .define("spawnWhenDiedWhileInfected", false);

    private static final ModConfigSpec.BooleanValue CORPSE_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, no corpse (Corpse mod) will spawn when a Zombified Player is created | default: false")
            .define("corpseCompatibility", false);

    private static final ModConfigSpec.BooleanValue GRAVESTONE_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, no gravestone (Gravestone mod) will spawn when a Zombified Player is created | default: false")
            .define("gravestoneCompatibility", false);

    private static final ModConfigSpec.BooleanValue USE_CUSTOM_EYE_HEIGHT = BUILDER_SERVER
            .comment("If true, the custom eye height defined by customEyeHeight will be used for the Zombified Player | default: false")
            .define("useCustomEyeHeight", false);

    private static final ModConfigSpec.DoubleValue CUSTOM_EYE_HEIGHT = BUILDER_SERVER
            .comment("The custom eye height of the Zombified Player (only used if useCustomEyeHeight is true) | default: 1.74")
            .defineInRange("customEyeHeight", 1.74, 0.0, 10.0);

    private static final ModConfigSpec.BooleanValue INFECTIOUS_MOD_COMPATIBILITY = BUILDER_SERVER
            .comment("If true, spawn zombified player when killed by Infectious Zombies (Infectious mod needed!) | default: false")
            .define("infectiousModCompatibility", false);

    public static final ModConfigSpec SPEC_SERVER = BUILDER_SERVER.build();


    // Client Config
    private static final ModConfigSpec.Builder BUILDER_CLIENT = new ModConfigSpec.Builder();
    // no client config
    public static final ModConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();


    private static boolean spawnZombifiedPlayerAfterDeath = true;
    private static List<String> randomSkinPlayerNames = DEFAULT_RANDOM_SKIN_PLAYER_NAMES;
    private static boolean transferMainAndOffHandToZombifiedPlayer = true;
    private static boolean transferArmorToZombifiedPlayer = true;
    private static boolean transferInventoryToZombifiedPlayer = true;
    private static boolean transferCuriosOrTrinketItemsToZombifiedPlayer = true;
    private static boolean ignoreKeepInventory = false;
    private static boolean spawnOnAnyDeath = false;
    private static boolean displayNameTagForZombifiedPlayer = true;
    private static boolean naturalSpawning = false;
    private static int naturalSpawnWeight = 5;
    private static boolean displayNameTagForNaturalZombifiedPlayers = false;
    private static boolean printSpawnMessageInChat = true;
    private static boolean printSpawnLocationInChat = false;
    private static boolean zombifiedPlayersCanAlwaysBreakDoors = false;
    private static boolean makeTheZombifiedPlayersStronger = false;
    private static boolean makeTheZombifiedPlayersImmuneToFire = false;
    private static boolean limitSkinFetchTries = true;
    private static boolean spawnWhenKilledByInfection = true;
    private static boolean spawnWhenDiedWhileInfected = false;
    private static boolean corpseCompatibility = false;
    private static boolean gravestoneCompatibility = false;
    private static boolean useCustomEyeHeight = false;
    private static float customEyeHeight = 1.74f;
    private static boolean infectiousModCompatibility = false;

    private static boolean isValidPlayerName(Object value) {
        return value instanceof String name && name.matches("[A-Za-z0-9_]{1,16}");
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ZombifiedPlayer.LOGGER.info("Loading Config");
        if (SPEC_SERVER.isLoaded()) {
            spawnZombifiedPlayerAfterDeath = SPAWN_ZOMBIFIED_PLAYER_AFTER_DEATH.get();
            randomSkinPlayerNames = List.copyOf(RANDOM_SKIN_PLAYER_NAMES.get());
            transferMainAndOffHandToZombifiedPlayer = TRANSFER_MAIN_AND_OFF_HAND_TO_ZOMBIFIED_PLAYER.get();
            transferArmorToZombifiedPlayer = TRANSFER_ARMOR_TO_ZOMBIFIED_PLAYER.get();
            transferInventoryToZombifiedPlayer = TRANSFER_INVENTORY_TO_ZOMBIFIED_PLAYER.get();
            transferCuriosOrTrinketItemsToZombifiedPlayer = TRANSFER_CURIOS_TRINKET_ITEMS_TO_ZOMBIFIED_PLAYER.get();
            ignoreKeepInventory = IGNORE_KEEP_INVENTORY.get();
            spawnOnAnyDeath = SPAWN_ON_ANY_DEATH.get();
            displayNameTagForZombifiedPlayer = DISPLAY_NAME_TAG_FOR_ZOMBIFIED_PLAYER.get();
            naturalSpawning = NATURAL_SPAWNING.get();
            naturalSpawnWeight = NATURAL_SPAWN_WEIGHT.get();
            displayNameTagForNaturalZombifiedPlayers = DISPLAY_NAME_TAG_FOR_NATURAL_ZOMBIFIED_PLAYERS.get();
            printSpawnMessageInChat = PRINT_SPAWN_MESSAGE_IN_CHAT.get();
            printSpawnLocationInChat = PRINT_SPAWN_LOCATION_IN_CHAT.get();
            zombifiedPlayersCanAlwaysBreakDoors = ZOMBIFIED_PLAYERS_CAN_ALWAYS_BREAK_DOORS.get();
            makeTheZombifiedPlayersStronger = MAKE_THE_ZOMBIFIED_PLAYERS_STRONGER.get();
            makeTheZombifiedPlayersImmuneToFire = MAKE_THE_ZOMBIFIED_PLAYERS_IMMUNE_TO_FIRE.get();
            limitSkinFetchTries = LIMIT_SKIN_FETCH_TRIES.get();
            spawnWhenKilledByInfection = SPAWN_WHEN_KILLED_BY_INFECTION.get();
            spawnWhenDiedWhileInfected = SPAWN_WHEN_DIED_WHILE_INFECTED.get();
            corpseCompatibility = CORPSE_COMPATIBILITY.get();
            gravestoneCompatibility = GRAVESTONE_COMPATIBILITY.get();
            useCustomEyeHeight = USE_CUSTOM_EYE_HEIGHT.get();
            customEyeHeight = CUSTOM_EYE_HEIGHT.get().floatValue();
            infectiousModCompatibility = INFECTIOUS_MOD_COMPATIBILITY.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
            // no client config
        }

    }
}
