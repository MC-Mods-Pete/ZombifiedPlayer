package net.petemc.zombifiedplayer.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModEntities {
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.tryBuild(ZombifiedPlayer.MOD_ID, "zombified_player"),
            EntityType.Builder.of(ZombifiedPlayerEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .build(ResourceLocation.tryBuild(ZombifiedPlayer.MOD_ID, "zombified_player").toString()));
}
