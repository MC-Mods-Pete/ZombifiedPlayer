package net.petemc.zombifiedplayer.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

public class ModEntities {
    private static final Identifier idZombifiedPlayer = Identifier.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player");
    private static final ResourceKey<EntityType<?>> keyZombifiedPlayer = ResourceKey.create(Registries.ENTITY_TYPE, idZombifiedPlayer);
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(BuiltInRegistries.ENTITY_TYPE, keyZombifiedPlayer,
            // Same spawn rules as zombies: on the ground, in the dark (natural spawning, spawners)
            FabricEntityType.Builder.createMob(ZombifiedPlayerEntity::new, MobCategory.MONSTER,
                            mob -> mob.spawnPlacement(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                    Monster::checkMonsterSpawnRules))
                    // like zombies: removed in peaceful (zombified players from a player death are kept, see checkDespawn)
                    .notInPeaceful()
                    .sized(0.6f, 1.95f)
                    .eyeHeight(MainConfig.getUseCustomEyeHeight() ? MainConfig.getCustomEyeHeight() : 1.74f)
                    .build(keyZombifiedPlayer));
}
