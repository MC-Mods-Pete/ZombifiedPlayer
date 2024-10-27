package net.petemc.zombifiedplayer.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModEntities {
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(Registry.ENTITY_TYPE,
            Identifier.of(ZombifiedPlayer.MOD_ID, "zombified_player"),
            EntityType.Builder.create(ZombifiedPlayerEntity::new, SpawnGroup.MONSTER)
                    .setDimensions(0.6f, 1.95f).build(Identifier.of(ZombifiedPlayer.MOD_ID, "zombified_player").toString()));
}

