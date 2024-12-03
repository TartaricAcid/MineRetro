package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class GameScreen extends Screen {
    private static final int GAME_WIDTH = 300;
    private static final int GAME_HEIGHT = 200;

    private final MineretroMiddleTier mineRetro;
    private final VideoManager videoManager;
    private final SoundManager soundManager;
    private final InputManager inputManager;

    private int xPos = 0;
    private int yPos = 0;
    private boolean paused = false;

    public GameScreen(MineretroMiddleTier mineRetro) {
        super(Component.literal("MineRetro"));
        this.mineRetro = mineRetro;

        this.videoManager = new VideoManager(this.mineRetro.mineretro_get_system_av_info().geometry, this.mineRetro.mineretro_get_rotation());
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
    protected void init() {
        super.clearWidgets();
        this.xPos = this.width / 2;
        this.yPos = this.height / 2;
        if (paused) {
            addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.game_screen.quit"), b -> {
                this.onClose();
            }).pos(this.xPos - 85, this.yPos + 10).size(80, 20).build());
            addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.game_screen.cancel"), b -> {
                this.paused = false;
                this.init();
                this.soundManager.start();
            }).pos(this.xPos + 5, this.yPos + 10).size(80, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(graphics);
        this.videoManager.resize(this.width, this.height);
        this.videoManager.renderVideo(GAME_WIDTH, GAME_HEIGHT);

        if (this.paused) {
            graphics.drawCenteredString(font, Component.translatable("screen.mineretro.game_screen.warning").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.UNDERLINE), this.xPos, this.yPos - 30, 0xFFFFFF);
            graphics.drawCenteredString(font, Component.translatable("screen.mineretro.game_screen.warning.message").withStyle(ChatFormatting.DARK_GRAY), this.xPos, this.yPos - 10, 0xFFFFFF);
            graphics.fill(this.xPos - 100, this.yPos - 40, this.xPos + 100, this.yPos + 40, 0xEE_EEEEEE);
            super.render(graphics, pMouseX, pMouseY, pPartialTick);
        } else {
            mineRetro.MineretroLoop();
        }
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (this.paused) {
                return true;
            }
            this.paused = true;
            this.init();
            this.soundManager.stop();
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
        mineRetro.MineretroUnloadCore();
        this.videoManager.clear();
        this.soundManager.close();
    }
}
