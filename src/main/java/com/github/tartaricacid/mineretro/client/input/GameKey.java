package com.github.tartaricacid.mineretro.client.input;


import com.github.tartaricacid.mineretro.client.screen.FileScreen;
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
        if (event.getAction() == GLFW.GLFW_PRESS && GAME_KEY.matches(event.getKey(), event.getScanCode()) && isInGame()) {
            if (Minecraft.getInstance().player == null) {
                return;
            }
            if (Minecraft.getInstance().player instanceof LocalPlayer) {
            }
            LocalPlayer player = Minecraft.getInstance().player;
            Minecraft.getInstance().setScreen(new FileScreen());
        }
    }

    public static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 不能是加载界面
        if (mc.getOverlay() != null) {
            return false;
        }
        // 不能打开任何 GUI
        if (mc.screen != null) {
            return false;
        }
        // 当前窗口捕获鼠标操作
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 选择了当前窗口
        return mc.isWindowActive();
    }
}
