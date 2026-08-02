package net.petemc.zombifiedplayer.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

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

    public static boolean getSpawnOnAnyDeath() {
        return spawnOnAnyDeath;
    }

    public static boolean getDisplayNameTagForZombifiedPlayer() {
        return displayNameTagForZombifiedPlayer;
    }

    public static boolean getPrintSpawnMessageInChat() {
        return printSpawnMessageInChat;
    }

    public static boolean getPrintSpawnLocationInChat() {
        return printSpawnLocationInChat;
    }

    public static boolean getZombifiedPlayersCanBreakDoors() {
        return zombifiedPlayersCanBreakDoors;
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

    private static final ModConfigSpec.BooleanValue SPAWN_ON_ANY_DEATH = BUILDER_SERVER
            .comment("If true, a zombified player will spawn no matter how the player died | default: false")
            .define("spawnOnAnyDeath", false);

    private static final ModConfigSpec.BooleanValue DISPLAY_NAME_TAG_FOR_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the name tag will be displayed above the Zombified Player | default: true")
            .define("displayNameTagForZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue PRINT_SPAWN_MESSAGE_IN_CHAT = BUILDER_SERVER
            .comment("If true, a message will be printed out in chat that a zombified player has spawned | default: true")
            .define("printSpawnMessageInChat", true);

    private static final ModConfigSpec.BooleanValue PRINT_SPAWN_LOCATION_IN_CHAT = BUILDER_SERVER
            .comment("If true, the spawn location of the zombified player will be printed out in chat | default: false")
            .define("printSpawnLocationInChat", false);

    private static final ModConfigSpec.BooleanValue ZOMBIFIED_PLAYERS_CAN_BREAK_DOORS = BUILDER_SERVER
            .comment("If true, zombified players can break through doors | default: true")
            .define("zombifiedPlayersCanBreakDoors", true);

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
    private static boolean transferMainAndOffHandToZombifiedPlayer = true;
    private static boolean transferArmorToZombifiedPlayer = true;
    private static boolean transferInventoryToZombifiedPlayer = true;
    private static boolean transferCuriosOrTrinketItemsToZombifiedPlayer = true;
    private static boolean spawnOnAnyDeath = false;
    private static boolean displayNameTagForZombifiedPlayer = true;
    private static boolean printSpawnMessageInChat = true;
    private static boolean printSpawnLocationInChat = false;
    private static boolean zombifiedPlayersCanBreakDoors = true;
    private static boolean makeTheZombifiedPlayersStronger = false;
    private static boolean makeTheZombifiedPlayersImmuneToFire = false;
    private static boolean limitSkinFetchTries = true;
    private static boolean spawnWhenKilledByInfection = true;
    private static boolean corpseCompatibility = false;
    private static boolean gravestoneCompatibility = false;
    private static boolean useCustomEyeHeight = false;
    private static float customEyeHeight = 1.74f;
    private static boolean infectiousModCompatibility = false;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ZombifiedPlayer.LOGGER.info("Loading Config");
        if (SPEC_SERVER.isLoaded()) {
            spawnZombifiedPlayerAfterDeath = SPAWN_ZOMBIFIED_PLAYER_AFTER_DEATH.get();
            transferMainAndOffHandToZombifiedPlayer = TRANSFER_MAIN_AND_OFF_HAND_TO_ZOMBIFIED_PLAYER.get();
            transferArmorToZombifiedPlayer = TRANSFER_ARMOR_TO_ZOMBIFIED_PLAYER.get();
            transferInventoryToZombifiedPlayer = TRANSFER_INVENTORY_TO_ZOMBIFIED_PLAYER.get();
            transferCuriosOrTrinketItemsToZombifiedPlayer = TRANSFER_CURIOS_TRINKET_ITEMS_TO_ZOMBIFIED_PLAYER.get();
            spawnOnAnyDeath = SPAWN_ON_ANY_DEATH.get();
            displayNameTagForZombifiedPlayer = DISPLAY_NAME_TAG_FOR_ZOMBIFIED_PLAYER.get();
            printSpawnMessageInChat = PRINT_SPAWN_MESSAGE_IN_CHAT.get();
            printSpawnLocationInChat = PRINT_SPAWN_LOCATION_IN_CHAT.get();
            zombifiedPlayersCanBreakDoors = ZOMBIFIED_PLAYERS_CAN_BREAK_DOORS.get();
            makeTheZombifiedPlayersStronger = MAKE_THE_ZOMBIFIED_PLAYERS_STRONGER.get();
            makeTheZombifiedPlayersImmuneToFire = MAKE_THE_ZOMBIFIED_PLAYERS_IMMUNE_TO_FIRE.get();
            limitSkinFetchTries = LIMIT_SKIN_FETCH_TRIES.get();
            spawnWhenKilledByInfection = SPAWN_WHEN_KILLED_BY_INFECTION.get();
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
