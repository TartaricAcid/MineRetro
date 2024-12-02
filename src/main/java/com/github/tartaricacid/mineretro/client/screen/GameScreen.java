package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import com.github.tartaricacid.mineretro.config.GameConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class GameScreen extends Screen {
    private static final int GAME_WIDTH = 300;
    private static final int GAME_HEIGHT = 200;

    private final MineretroMiddleTier mineRetro;
    private final VideoManager videoManager;
    private final SoundManager soundManager;
    private final InputManager inputManager;

    public GameScreen(String corePath, String gamePath) {
        super(Component.literal("MineRetro"));

        this.mineRetro = MineretroMiddleTier.INSTANCE;
        this.mineRetro.mineretro_set_system_and_save_dir(GameConfig.SYSTEM_DIR_PATH.toString(), GameConfig.SAVE_DIR_PATH.toString());
        this.mineRetro.MineretroInit(corePath, gamePath);

        this.videoManager = new VideoManager(GAME_WIDTH, GAME_HEIGHT, this.mineRetro.mineretro_get_rotation(), this.mineRetro.mineretro_get_system_av_info().geometry);
        this.videoManager.init(this.mineRetro.mineretro_get_pixel_format());
        this.mineRetro.mineretro_set_video(this.videoManager.getVideoRefresh());

        this.soundManager = new SoundManager(this.mineRetro);
        mineRetro.mineretro_set_audio(this.soundManager.getSampleOnce());
        mineRetro.mineretro_set_audio_batch(this.soundManager.getAudioSampleBatch());

        this.inputManager = new InputManager();
        mineRetro.mineretro_set_input_poll(this.inputManager.getInputPoll());
        mineRetro.mineretro_set_input_state(this.inputManager.getInputState());
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(graphics);
        this.videoManager.resize(this.width, this.height);
        mineRetro.MineretroLoop();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
            String title = I18n.get("screen.mineretro.game_screen.warning");
            String message = I18n.get("screen.mineretro.game_screen.warning.message");
            boolean result = TinyFileDialogs.tinyfd_messageBox(title, message, "yesno", "warning", false);
            if (result) {
                this.onClose();
            }
            return true;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        mineRetro.MineretroDeinit();
        this.videoManager.clear();
        this.soundManager.close();
    }
}
