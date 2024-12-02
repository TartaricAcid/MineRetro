package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.config.GameConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileScreen extends Screen {
    private static Path corePath;
    private static Path gamePath;
    private static Component corePathMessage = Component.literal("");
    private static Component gamePathMessage = Component.literal("");
    private Component message = Component.literal("");
    private int xPos = 0;
    private int yPos = 0;

    public FileScreen() {
        super(Component.literal("File Screen"));
    }

    @Override
    protected void init() {
        this.xPos = this.width / 2;
        this.yPos = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.choose_core"), b -> {
            String title = I18n.get("screen.mineretro.file_screen.choose_core");
            String corePathStr = TinyFileDialogs.tinyfd_openFileDialog(title, GameConfig.CORE_DIR_PATH.toAbsolutePath() + "\\", null, null, false);
            if (StringUtils.isNotBlank(corePathStr)) {
                corePath = Path.of(corePathStr);
                corePathMessage = Component.translatable("screen.mineretro.file_screen.core", Paths.get(corePathStr).getFileName().toString());
            }
        }).pos(xPos - 125, yPos - 50).size(120, 50).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.choose_game"), b -> {
            String title = I18n.get("screen.mineretro.file_screen.choose_game");
            String gamePathStr = TinyFileDialogs.tinyfd_openFileDialog(title, GameConfig.GAME_DIR_PATH.toAbsolutePath() + "\\", null, null, false);
            if (StringUtils.isNotBlank(gamePathStr)) {
                gamePath = Path.of(gamePathStr);
                gamePathMessage = Component.translatable("screen.mineretro.file_screen.game", Paths.get(gamePathStr).getFileName().toString());
            }
        }).pos(xPos + 5, yPos - 50).size(120, 50).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.run_game"), b -> {
            if (corePath == null || !corePath.toFile().exists()) {
                this.message = Component.translatable("screen.mineretro.file_screen.core_not_exist");
                return;
            }
            if (gamePath == null || !gamePath.toFile().exists()) {
                this.message = Component.translatable("screen.mineretro.file_screen.game_not_exist");
                return;
            }
            String corePathString = corePath.toString();
            String gamePathString = gamePath.toString();
            if (containsChinese(corePathString)) {
                this.message = Component.translatable("screen.mineretro.file_screen.core_path_chinese");
                return;
            }
            if (containsChinese(gamePathString)) {
                this.message = Component.translatable("screen.mineretro.file_screen.game_path_chinese");
                return;
            }
            Minecraft.getInstance().setScreen(new GameScreen(corePathString, gamePathString));
        }).pos(xPos - 125, yPos + 10).size(250, 50).build());
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(graphics);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        graphics.drawCenteredString(font, corePathMessage, this.xPos, this.yPos - 90, 0xFFFFFF);
        graphics.drawCenteredString(font, gamePathMessage, this.xPos, this.yPos - 70, 0xFFFFFF);
        graphics.drawCenteredString(font, message, this.xPos, this.yPos + 70, 0xFF0000);
    }

    private static boolean containsChinese(String str) {
        // 正则表达式匹配中文字符范围
        String regex = "[\u4e00-\u9fa5]";
        return str.matches(".*" + regex + ".*");
    }
}
