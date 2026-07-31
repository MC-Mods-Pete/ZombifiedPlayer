package net.petemc.zombifiedplayer.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.petemc.zombifiedplayer.config.MainConfig;

/**
 * Single source of truth for all Zombified Player spawn conditions.
 * Fabric port — uses TrinketsUtil instead of CuriosUtil.
 *
 * IMPORTANT TIMING NOTE (ServerPlayer.die() call order):
 *   1. Death event fires            → actual spawn happens here
 *                                      Inventory is still FULL — transfer works correctly.
 *   2. dropAllDeathLoot()           → player inventory dropped (already empty after transfer)
 *   3. broadcastSystemMessage()     → vanilla death message in chat
 *   4. ServerPlayerEntityMixin.die()→ chat notification "You have been zombified"
 *
 * Fabric: use ServerLivingEntityEvents.ALLOW_DEATH or inject into LivingEntity.die()
 * early enough (before dropAllDeathLoot). This class stays unchanged between Fabric versions.
 */
public class ZombifiedPlayerSpawnLogic {

    /** Only zombie-type undead trigger a spawn — skeletons, phantoms etc. are excluded. */
    private static final TagKey<EntityType<?>> ZOMBIE_UNDEAD = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("zombifiedplayer", "zombie_undead"));

    /**
     * Returns true if a Zombified Player should spawn when this player dies.
     * Includes totem-of-undying protection check (vanilla hands + Curios slots).
     */
    public static boolean shouldSpawn(ServerPlayer serverPlayer, DamageSource damageSource) {
        // Totem protection — vanilla hands and Trinkets slots
        if (serverPlayer.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
                || serverPlayer.getOffhandItem().is(Items.TOTEM_OF_UNDYING)
                || TrinketsUtil.checkForItemInTrinkets(serverPlayer, Items.TOTEM_OF_UNDYING.getDefaultInstance())
                || TrinketsUtil.checkForItemInTrinkets(serverPlayer, "chargedcharms:charged_totem_charm")) {
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
     * Use attacker.typeHolder().is(...) — NOT attacker.getType().is(...) which doesn't compile,
     * and NOT builtInRegistryHolder() which is @Deprecated.
     */
    public static boolean isUndeadAttacker(Entity attacker) {
        if (attacker == null) return false;
        boolean tagMatch = attacker.typeHolder().is(ZOMBIE_UNDEAD);
        boolean hordeMatch = (attacker instanceof LivingEntity living)
                && ModCompatibility.wasKilledByHordeZombie(living);
        return tagMatch || hordeMatch;
    }
}





