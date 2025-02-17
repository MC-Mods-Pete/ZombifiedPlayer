package net.petemc.zombifiedplayer;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    public static boolean getSpawnZombifiedPlayerAfterDeath() {
        return spawnZombifiedPlayerAfterDeath;
    }

    public static boolean getTransferMainandOffHandToZombifiedPlayer() {
        return transferMainandOffHandToZombifiedPlayer;
    }

    public static boolean getTransferArmorToZombifiedPlayer() {
        return transferArmorToZombifiedPlayer;
    }

    public static boolean getTransferInventoryToZombifiedPlayer() {
        return transferInventoryToZombifiedPlayer;
    }

    public static boolean getSpawnOnAnyDeath() {
        return spawnOnAnyDeath;
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


    // Server Config
    private static final ModConfigSpec.Builder BUILDER_SERVER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue SPAWN_ZOMBIFIED_PLAYER_AFTER_DEATH = BUILDER_SERVER
            .comment("If true, a zombified player will spawn after the player gets killed by an Undead | default: true")
            .define("spawnZombifiedPlayerAfterDeath", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_MAIN_AND_OFF_HAND_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the Main and the OffHand of the dead player will be transferred to the zombified player | default: true")
            .define("transferMainandOffHandToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_ARMOR_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the armor of the dead player will be transferred to the zombified player | default: true")
            .define("transferArmorToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue TRANSFER_INVENTORY_TO_ZOMBIFIED_PLAYER = BUILDER_SERVER
            .comment("If true, the inventory of the dead player will be transferred to the zombified player | default: true")
            .define("transferInventoryToZombifiedPlayer", true);

    private static final ModConfigSpec.BooleanValue SPAWN_ON_ANY_DEATH = BUILDER_SERVER
            .comment("If true, a zombified player will spawn no matter how the player died | default: false")
            .define("spawnOnAnyDeath", false);

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

    static final ModConfigSpec SPEC_SERVER = BUILDER_SERVER.build();


    // Client Config
    private static final ModConfigSpec.Builder BUILDER_CLIENT = new ModConfigSpec.Builder();
    // no client config
    static final ModConfigSpec SPEC_CLIENT = BUILDER_CLIENT.build();


    private static boolean spawnZombifiedPlayerAfterDeath = true;
    private static boolean transferMainandOffHandToZombifiedPlayer = true;
    private static boolean transferArmorToZombifiedPlayer = true;
    private static boolean transferInventoryToZombifiedPlayer = true;
    private static boolean spawnOnAnyDeath = false;
    private static boolean printSpawnLocationInChat = false;
    private static boolean zombifiedPlayersCanBreakDoors = true;
    private static boolean makeTheZombifiedPlayersStronger = false;
    private static boolean makeTheZombifiedPlayersImmuneToFire = false;
    private static boolean limitSkinFetchTries = true;
    private static boolean spawnWhenKilledByInfection = true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ZombifiedPlayer.LOGGER.info("Loading Config");
        if (SPEC_SERVER.isLoaded()) {
            spawnZombifiedPlayerAfterDeath = SPAWN_ZOMBIFIED_PLAYER_AFTER_DEATH.get();
            transferMainandOffHandToZombifiedPlayer = TRANSFER_MAIN_AND_OFF_HAND_TO_ZOMBIFIED_PLAYER.get();
            transferArmorToZombifiedPlayer = TRANSFER_ARMOR_TO_ZOMBIFIED_PLAYER.get();
            transferInventoryToZombifiedPlayer = TRANSFER_INVENTORY_TO_ZOMBIFIED_PLAYER.get();
            spawnOnAnyDeath = SPAWN_ON_ANY_DEATH.get();
            printSpawnLocationInChat = PRINT_SPAWN_LOCATION_IN_CHAT.get();
            zombifiedPlayersCanBreakDoors = ZOMBIFIED_PLAYERS_CAN_BREAK_DOORS.get();
            makeTheZombifiedPlayersStronger = MAKE_THE_ZOMBIFIED_PLAYERS_STRONGER.get();
            makeTheZombifiedPlayersImmuneToFire = MAKE_THE_ZOMBIFIED_PLAYERS_IMMUNE_TO_FIRE.get();
            limitSkinFetchTries = LIMIT_SKIN_FETCH_TRIES.get();
            spawnWhenKilledByInfection = SPAWN_WHEN_KILLED_BY_INFECTION.get();
        }
        if (SPEC_CLIENT.isLoaded()) {
            // no client config
        }

    }
}
