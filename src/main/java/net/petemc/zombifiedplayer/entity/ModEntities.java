package net.petemc.zombifiedplayer.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.zombifiedplayer.ZombifiedPlayer;
import net.petemc.zombifiedplayer.config.MainConfig;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ZombifiedPlayer.MOD_ID);

    public static final Supplier<EntityType<ZombifiedPlayerEntity>> ZOMBIFIED_PLAYER =
            ENTITY_TYPES.register("zombified_player", () -> net.minecraft.world.entity.EntityType.Builder.of(ZombifiedPlayerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(MainConfig.getUseCustomEyeHeight() ? MainConfig.getCustomEyeHeight() : 1.74f)
                    .build("zombified_player"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
