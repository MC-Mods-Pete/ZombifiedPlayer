package net.petemc.zombifiedplayer.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class ModEntities {
    private static Identifier idZombifiedPlayer = Identifier.of(ZombifiedPlayer.MOD_ID, "zombified_player");
    private static RegistryKey<EntityType<?>> keyZombifiedPlayer = RegistryKey.of(RegistryKeys.ENTITY_TYPE, idZombifiedPlayer);
    public static final EntityType<ZombifiedPlayerEntity> ZOMBIFIED_PLAYER = Registry.register(Registries.ENTITY_TYPE, keyZombifiedPlayer,
            EntityType.Builder.create(ZombifiedPlayerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).eyeHeight(1.74f).build(keyZombifiedPlayer));
}
