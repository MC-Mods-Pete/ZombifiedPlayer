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
 * Single source of truth for all Zombified Player spawn conditions.
 * Platform-neutral — usable on both NeoForge and Fabric without changes.
 *
 * IMPORTANT TIMING NOTE (ServerPlayer.die() call order):
 *   1. LivingDeathEvent fires       → actual spawn (PlayerDeathEvents / Fabric callback)
 *                                      Inventory is still FULL here — transfer works correctly.
 *   2. dropAllDeathLoot()           → player inventory is dropped (already empty after transfer)
 *   3. death message                → vanilla death message in chat (only if showDeathMessages is on)
 *   4. ServerPlayerEntityMixin.die()→ chat notification "You have been zombified", injected before
 *                                      removeEntitiesOnShoulder(), which always runs after the death message part
 *
 * Only the death event calls shouldSpawn(), the mixin uses the remembered result (setSpawnedThisDeath).
 *
 * Fabric port: replace the NeoForge LivingDeathEvent handler with a Fabric callback
 * (e.g. ServerLivingEntityEvents.ALLOW_DEATH). This class stays unchanged.
 */
public class ZombifiedPlayerSpawnLogic {

    /** Players for whom a Zombified Player was spawned in the current death (set by the death event, read by the mixin). */
    private static final Set<UUID> SPAWNED_THIS_DEATH = ConcurrentHashMap.newKeySet();

    /** Called for every death that reaches the death event, so a stale value never survives to the next death. */
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
            ResourceLocation.fromNamespaceAndPath("zombifiedplayer", "zombie_undead"));

    /**
     * Returns true if a Zombified Player should spawn when this player dies.
     * Includes totem-of-undying protection check (vanilla hands, Curios and Accessories slots).
     */
    public static boolean shouldSpawn(ServerPlayer serverPlayer, DamageSource damageSource) {
        // Totem protection — vanilla hands, Curios and Accessories slots
        if (serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                || CuriosUtil.checkForItemInCurios(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance())
                || CuriosUtil.checkForItemInCurios(serverPlayer, "chargedcharms:charged_totem_charm")
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
     * ZombifiedPlayerEntity is included via the tag values directly.
     * Mutants and Zombies: blister_zombie, zombie_brute, split_head_zombie and mutant_zombie are optional tag entries.
     * Use attacker.getType().is(...) — NOT attacker.typeHolder().is(...) which doesn't compile,
     * and NOT builtInRegistryHolder() which is @Deprecated.
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
        if (mobId == null) return false;
        String id = mobId.toString().toLowerCase();
        return MainConfig.getInfectiousModCompatibility() && id.contains("infectious") && id.contains("zombie");
    }
}





