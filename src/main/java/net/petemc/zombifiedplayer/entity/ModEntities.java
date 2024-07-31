package net.petemc.zombifiedplayer.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModEntities {
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(ZombifiedPlayer.MOD_ID, "zombified_player"),
            EntityType.Builder.create(ZombifiedPlayerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).eyeHeight(1.74f).build());
}
