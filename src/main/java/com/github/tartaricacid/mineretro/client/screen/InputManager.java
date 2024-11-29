package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import static com.github.tartaricacid.mineretro.client.jna.InputKeyMapping.*;

public class InputManager {
    private static Keymap[] KEY_MAPS = new Keymap[]{
            new Keymap(GLFW.GLFW_KEY_X, RETRO_DEVICE_ID_JOYPAD_A),
            new Keymap(GLFW.GLFW_KEY_Z, RETRO_DEVICE_ID_JOYPAD_B),
            new Keymap(GLFW.GLFW_KEY_A, RETRO_DEVICE_ID_JOYPAD_Y),
            new Keymap(GLFW.GLFW_KEY_S, RETRO_DEVICE_ID_JOYPAD_X),
            new Keymap(GLFW.GLFW_KEY_UP, RETRO_DEVICE_ID_JOYPAD_UP),
            new Keymap(GLFW.GLFW_KEY_DOWN, RETRO_DEVICE_ID_JOYPAD_DOWN),
            new Keymap(GLFW.GLFW_KEY_LEFT, RETRO_DEVICE_ID_JOYPAD_LEFT),
            new Keymap(GLFW.GLFW_KEY_RIGHT, RETRO_DEVICE_ID_JOYPAD_RIGHT),
            new Keymap(GLFW.GLFW_KEY_ENTER, RETRO_DEVICE_ID_JOYPAD_START),
            new Keymap(GLFW.GLFW_KEY_BACKSPACE, RETRO_DEVICE_ID_JOYPAD_SELECT),
            new Keymap(GLFW.GLFW_KEY_Q, RETRO_DEVICE_ID_JOYPAD_L),
            new Keymap(GLFW.GLFW_KEY_W, RETRO_DEVICE_ID_JOYPAD_R)
    };
    private static final int[] KEY_STATES = new int[KEY_MAPS.length];

    private final MineretroMiddleTier.InputPoll inputPoll;
    private final MineretroMiddleTier.InputState inputState;

    public InputManager() {
        this.inputPoll = () -> {
            for (int i = 0; i < KEY_MAPS.length; i++) {
                int glfwKeyId = KEY_MAPS[i].glfwKeyId;
                int retroKeyId = KEY_MAPS[i].retroKeyId;
                boolean keyDown = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), glfwKeyId);
                KEY_STATES[retroKeyId] = keyDown ? 1 : 0;
            }
        };

        this.inputState = (port, device, index, id) -> {
            if (port > 0 || index > 0 || device != RETRO_DEVICE_JOYPAD || id >= KEY_STATES.length) {
                return 0;
            }
            return KEY_STATES[id];
        };
    }

    public MineretroMiddleTier.InputPoll getInputPoll() {
        return inputPoll;
    }

    public MineretroMiddleTier.InputState getInputState() {
        return inputState;
    }

    private record Keymap(int glfwKeyId, int retroKeyId) {
    }
}
