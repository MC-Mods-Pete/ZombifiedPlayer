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

/**
 * Single source of truth for all Zombified Player spawn conditions.
 * Platform-neutral — usable on both NeoForge and Fabric without changes.
 *
 * IMPORTANT TIMING NOTE (ServerPlayer.die() call order):
 *   1. LivingDeathEvent fires       → actual spawn (PlayerDeathEvents / Fabric callback)
 *                                      Inventory is still FULL here — transfer works correctly.
 *   2. dropAllDeathLoot()           → player inventory is dropped (already empty after transfer)
 *   3. broadcastSystemMessage()     → vanilla death message in chat
 *   4. ServerPlayerEntityMixin.die()→ chat notification "You have been zombified"
 *
 * Both callers (event + mixin) call shouldSpawn() so the condition is never out of sync.
 *
 * Fabric port: replace the NeoForge LivingDeathEvent handler with a Fabric callback
 * (e.g. ServerLivingEntityEvents.ALLOW_DEATH). This class stays unchanged.
 */
public class ZombifiedPlayerSpawnLogic {

    /** Only zombie-type undead trigger a spawn — skeletons, phantoms etc. are excluded. */
    private static final TagKey<EntityType<?>> ZOMBIE_UNDEAD = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("zombifiedplayer", "zombie_undead"));

    /**
     * Returns true if a Zombified Player should spawn when this player dies.
     * Includes totem-of-undying protection check (vanilla hands + Curios slots).
     */
    public static boolean shouldSpawn(ServerPlayer serverPlayer, DamageSource damageSource) {
        // Totem protection — vanilla hands and Curios slots
        if (serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                || CuriosUtil.checkForItemInCurios(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance())
                || CuriosUtil.checkForItemInCurios(serverPlayer, "chargedcharms:charged_totem_charm")) {
            return false;
        }

        return MainConfig.getSpawnOnAnyDeath()
                || (ModCompatibility.diedFromInfection(serverPlayer) && MainConfig.getSpawnWhenKilledByInfection())
                || (isUndeadAttacker(damageSource.getEntity()) && MainConfig.getSpawnZombifiedPlayerAfterDeath());
    }

    /**
     * Tag-based check against zombifiedplayer:zombie_undead — only zombie variants,
     * NOT skeletons, phantoms, withers etc. (those are in minecraft:undead but excluded here).
     * ZombifiedPlayerEntity is included via the tag values directly.
     * Use attacker.getType().is(...) — NOT attacker.typeHolder().is(...) which doesn't compile,
     * and NOT builtInRegistryHolder() which is @Deprecated.
     */
    public static boolean isUndeadAttacker(Entity attacker) {
        if (attacker == null) return false;
        boolean tagMatch = attacker.getType().is(ZOMBIE_UNDEAD);
        boolean hordeMatch = (attacker instanceof LivingEntity living)
                && ModCompatibility.wasKilledByHordeZombie(living);
        boolean zombieMatch = (attacker instanceof Zombie);
        boolean mutantsZombiesMatch = hasMutantsZombiesId(attacker);
        boolean infectiousZombieMatch = hasInfectiousZombieId(attacker);
        return tagMatch || hordeMatch || zombieMatch || mutantsZombiesMatch || infectiousZombieMatch;
    }

    /**
     * Check if the entity's mob ID contains mobs from the "mutantszombies" mod.
     */
    private static boolean hasMutantsZombiesId(Entity entity) {
        ResourceLocation mobId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (mobId == null) return false;
        String id = mobId.toString().toLowerCase();
        boolean flag = id.contains("mutantszombies");
        boolean flag2 = id.contains("blister") || id.contains("brute") || id.contains("split");
        return flag && flag2;
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





