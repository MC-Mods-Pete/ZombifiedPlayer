package net.petemc.zombifiedplayer.entity;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.petemc.zombifiedplayer.ZombifiedPlayer;

public class RegisterEntityAttributes {
    @Mod.EventBusSubscriber(modid = ZombifiedPlayer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.ZOMBIFIED_PLAYER.get(), ZombifiedPlayerEntity.createAttributes().build());

        }
    }

}


