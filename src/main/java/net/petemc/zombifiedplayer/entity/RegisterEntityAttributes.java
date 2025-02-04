package net.petemc.zombifiedplayer.entity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class RegisterEntityAttributes {
    @EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.ZOMBIFIED_PLAYER.get(), ZombifiedPlayerEntity.createAttributes().build());

        }
    }

}


