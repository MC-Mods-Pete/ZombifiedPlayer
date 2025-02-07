package net.petemc.zombifiedplayer.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ZombifiedPlayer.MOD_ID);

    public static final Supplier<EntityType<ZombifiedPlayerEntity>> ZOMBIFIED_PLAYER =
            ENTITY_TYPES.register("zombified_player", () -> net.minecraft.world.entity.EntityType.Builder.of(ZombifiedPlayerEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.95f).build(ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ZombifiedPlayer.MOD_ID, "zombified_player"))));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
