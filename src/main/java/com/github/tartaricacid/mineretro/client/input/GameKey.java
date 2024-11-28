package com.github.tartaricacid.mineretro.client.input;


import com.github.tartaricacid.mineretro.client.screen.GameScreen;
import com.github.tartaricacid.mineretro.config.GameConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;


@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class GameKey {
    public static final KeyMapping GAME_KEY = new KeyMapping("key.mineretro.game.desc",
            KeyConflictContext.IN_GAME,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.category.mineretro");

    @SubscribeEvent
    public static void onGameKeyPress(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && GAME_KEY.matches(event.getKey(), event.getScanCode())) {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            LocalPlayer player = Minecraft.getInstance().player;
            if (!GameConfig.isCoreExists()) {
                player.sendSystemMessage(Component.literal("需要在 config/mineretro 文件夹下放置一个名为 core.dll 的核心文件"));
                return;
            }
            if (!GameConfig.isGameExists()) {
                player.sendSystemMessage(Component.literal("需要在 config/mineretro 文件夹下放置一个名为 game.nes 的游戏文件"));
                return;
            }
            Minecraft.getInstance().setScreen(new GameScreen(GameConfig.getCorePath(), GameConfig.getGamePath()));
        }
    }
}
