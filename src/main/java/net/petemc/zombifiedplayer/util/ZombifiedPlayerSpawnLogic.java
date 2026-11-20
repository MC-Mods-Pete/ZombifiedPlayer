package net.petemc.zombifiedplayer.util;

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
 * Single source of truth for all Zombified Player spawn conditions (same as in the Forge/NeoForge branches).
 * Both the spawn and the chat message use shouldSpawn(), so the condition is never out of sync.
 */
public class ZombifiedPlayerSpawnLogic {

    /** Only zombie-type undead trigger a spawn — skeletons, phantoms etc. are excluded. */
    private static final TagKey<EntityType<?>> ZOMBIE_UNDEAD = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath("zombifiedplayer", "zombie_undead"));

    /**
     * Returns true if a Zombified Player should spawn when this player dies.
     * Includes totem-of-undying protection check (vanilla hands + Trinkets slots).
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
     * ZombifiedPlayerEntity is included via the tag values directly. Undead Nights horde zombies count as well.
     */
    public static boolean isUndeadAttacker(Entity attacker) {
        if (attacker == null) return false;
        boolean tagMatch = attacker.getType().is(ZOMBIE_UNDEAD);
        boolean hordeMatch = (attacker instanceof LivingEntity living)
                && ModCompatibility.wasKilledByHordeZombie(living);
        boolean zombieMatch = (attacker instanceof Zombie);
        return tagMatch || hordeMatch || zombieMatch;
    }
}
