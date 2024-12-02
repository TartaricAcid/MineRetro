package com.github.tartaricacid.mineretro.client.init;

import com.github.tartaricacid.mineretro.Mineretro;
import com.github.tartaricacid.mineretro.client.input.GameKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = Mineretro.MOD_ID)
public class ClientSetupEvent {
    @SubscribeEvent
    public static void onClientSetup(RegisterKeyMappingsEvent event) {
        // 注册键位
        event.register(GameKey.GAME_KEY);
    }
}
