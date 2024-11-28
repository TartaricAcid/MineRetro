package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GameScreen extends Screen {
    private static final int GAME_WIDTH = 320;
    private static final int GAME_HEIGHT = 240;

    private final MineretroMiddleTier mineRetro;
    private final VideoManager videoManager;
    private final SoundManager soundManager;

    public GameScreen(String corePath, String gamePath) {
        super(Component.literal("MineRetro"));

        this.mineRetro = MineretroMiddleTier.INSTANCE;
        this.mineRetro.MineretroInit(corePath, gamePath);

        this.videoManager = new VideoManager(GAME_WIDTH, GAME_HEIGHT);
        this.videoManager.init(this.mineRetro.mineretro_get_pixel_format());
        this.mineRetro.mineretro_set_video((data, width, height, pitch) ->
                this.videoManager.getVideoRefresh().invoke(data, width, height, pitch));

        this.soundManager = new SoundManager(this.mineRetro);
        mineRetro.mineretro_set_audio_batch(this.soundManager.getAudioSampleBatch());
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(graphics);
        this.videoManager.resize((this.width - GAME_WIDTH) / 2, (this.height - GAME_HEIGHT) / 2);
        mineRetro.MineretroLoop();
    }

    @Override
    public void onClose() {
        super.onClose();
        mineRetro.MineretroDeinit();
        this.videoManager.clear();
        this.soundManager.close();
    }
}
