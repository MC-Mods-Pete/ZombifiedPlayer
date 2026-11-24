package net.petemc.zombifiedplayer.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for all Zombified Player spawn conditions (same as in the Forge/NeoForge branches).
 * Only the spawn calls shouldSpawn(), the chat message uses the remembered result (setSpawnedThisDeath).
 */
public class ZombifiedPlayerSpawnLogic {

    /** Players for whom a Zombified Player was spawned in the current death (set at the start of die(), read for the chat message). */
    private static final Set<UUID> SPAWNED_THIS_DEATH = ConcurrentHashMap.newKeySet();

    /** Called for every death, so a stale value never survives to the next death. */
    public static void setSpawnedThisDeath(ServerPlayer serverPlayer, boolean spawned) {
        if (spawned) {
            SPAWNED_THIS_DEATH.add(serverPlayer.getUUID());
        } else {
            SPAWNED_THIS_DEATH.remove(serverPlayer.getUUID());
        }
    }

    /** Returns whether a Zombified Player was spawned for this death and resets the flag. */
    public static boolean takeSpawnedThisDeath(ServerPlayer serverPlayer) {
        return SPAWNED_THIS_DEATH.remove(serverPlayer.getUUID());
    }

    /** Only zombie-type undead trigger a spawn — skeletons, phantoms etc. are excluded. */
    private static final TagKey<EntityType<?>> ZOMBIE_UNDEAD = TagKey.create(
            Registries.ENTITY_TYPE,
            new ResourceLocation("zombifiedplayer", "zombie_undead"));

    /**
     * Returns true if a Zombified Player should spawn when this player dies.
     * Includes totem-of-undying protection check (vanilla hands, Trinkets and Accessories slots).
     */
    public static boolean shouldSpawn(ServerPlayer serverPlayer, DamageSource damageSource) {
        // Totem protection — vanilla hands, Trinkets and Accessories slots
        if (serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                || TrinketsUtil.checkForItemInTrinkets(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance())
                || TrinketsUtil.checkForItemInTrinkets(serverPlayer, "chargedcharms:charged_totem_charm")
                || AccessoriesUtil.checkForItemInAccessories(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance())
                || AccessoriesUtil.checkForItemInAccessories(serverPlayer, "chargedcharms:charged_totem_charm")) {
            return false;
        }

        return MainConfig.getSpawnOnAnyDeath()
                || (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection())
                || (MainConfig.getSpawnWhenDiedWhileInfected() && ModCompatibility.isInfected(serverPlayer))
                || (isUndeadAttacker(damageSource.getEntity()) && MainConfig.getSpawnZombifiedPlayerAfterDeath());
    }

    /**
     * Tag-based check against zombifiedplayer:zombie_undead — only zombie variants,
     * NOT skeletons, phantoms, withers etc. (those are in minecraft:undead but excluded here).
     * ZombifiedPlayerEntity is included via the tag values directly. Undead Nights horde zombies count as well.
     * Mutants and Zombies: blister_zombie, zombie_brute, split_head_zombie and mutant_zombie are optional tag entries.
     */
    public static boolean isUndeadAttacker(Entity attacker) {
        if (attacker == null) return false;
        boolean tagMatch = attacker.getType().is(ZOMBIE_UNDEAD);
        boolean hordeMatch = (attacker instanceof LivingEntity living)
                && ModCompatibility.wasKilledByHordeZombie(living);
        boolean zombieMatch = (attacker instanceof Zombie);
        boolean infectiousZombieMatch = hasInfectiousZombieId(attacker);
        return tagMatch || hordeMatch || zombieMatch || infectiousZombieMatch;
    }

    /**
     * Check if the entity's mob ID contains both "infectious" and "zombie".
     * Only returns true if infectiousModCompatibility is enabled in the config.
     */
    private static boolean hasInfectiousZombieId(Entity entity) {
        if (!MainConfig.getInfectiousModCompatibility()) return false;
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String id = mobId.toString().toLowerCase();
        return id.contains("infectious") && id.contains("zombie");
    }
}
