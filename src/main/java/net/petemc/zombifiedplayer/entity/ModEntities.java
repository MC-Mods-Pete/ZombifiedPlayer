package net.petemc.zombifiedplayer.entity;

import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModEntities {
    public static final DeferredRegister<net.minecraft.world.entity.EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ZombifiedPlayer.MOD_ID);

    public static final RegistryObject<net.minecraft.world.entity.EntityType<ZombifiedPlayerEntity>> ZOMBIFIED_PLAYER =
            ENTITY_TYPES.register("zombified_player", () -> net.minecraft.world.entity.EntityType.Builder.of(ZombifiedPlayerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f)
                    .build("zombified_player"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
