package net.petemc.zombifiedplayer.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

public class ModEntities {
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player"),
            EntityType.Builder.of(ZombifiedPlayerEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(MainConfig.getUseCustomEyeHeight() ? MainConfig.getCustomEyeHeight() : 1.74f)
                    .build("zombified_player"));
}
