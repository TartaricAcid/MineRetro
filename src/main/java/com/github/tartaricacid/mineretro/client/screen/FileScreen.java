package com.github.tartaricacid.mineretro.client.screen;

import com.github.tartaricacid.mineretro.client.jna.MineretroMiddleTier;
import com.github.tartaricacid.mineretro.config.GameConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileScreen extends Screen {
    private final MineretroMiddleTier mineRetro;
    private Component corePathMessage = Component.empty();
    private Component gamePathMessage = Component.empty();
    private Component message = Component.empty();
    private boolean coreIsLoaded = false;
    private boolean gameIsLoaded = false;
    private int xPos = 0;
    private int yPos = 0;

    public FileScreen() {
        super(Component.literal("File Screen"));
        this.mineRetro = MineretroMiddleTier.INSTANCE;
        this.mineRetro.mineretro_set_system_and_save_dir(GameConfig.SYSTEM_DIR_PATH.toString(), GameConfig.SAVE_DIR_PATH.toString());
    }

    @Override
    protected void init() {
        this.xPos = this.width / 2;
        this.yPos = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.choose_core"), this::loadCore)
                .pos(xPos - 125, yPos - 50).size(120, 50).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.choose_game"), this::loadGame)
                .pos(xPos + 5, yPos - 50).size(120, 50).build());
        this.addRenderableWidget(Button.builder(Component.translatable("screen.mineretro.file_screen.run_game"), this::runGame)
                .pos(xPos - 125, yPos + 10).size(250, 50).build());
    }

    private void loadCore(Button button) {
        String corePathStr;
        String[] extensions = {"dll", "so", "dylib"};
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filter = stack.mallocPointer(extensions.length);
            for (String extension : extensions) {
                filter.put(stack.UTF8("*." + extension));
            }
            filter.flip();
            String title = I18n.get("screen.mineretro.file_screen.choose_core");
            corePathStr = TinyFileDialogs.tinyfd_openFileDialog(title, GameConfig.CORE_DIR_PATH.toAbsolutePath() + "\\", filter, "Core File", false);
        }

        if (StringUtils.isNotBlank(corePathStr)) {
            Path corePath = Paths.get(corePathStr);
            if (!corePath.toFile().exists()) {
                this.message = Component.translatable("screen.mineretro.file_screen.core_not_exist");
                return;
            }
            if (containsChinese(corePathStr)) {
                this.message = Component.translatable("screen.mineretro.file_screen.core_path_chinese");
                return;
            }
            // 先卸载旧核心，再加载新核心
            this.mineRetro.MineretroUnloadCore();
            this.mineRetro.MineretroLoadCore(corePathStr);
            this.coreIsLoaded = true;
            // 显示核心名称和版本
            String libName = this.mineRetro.mineretro_get_system_info().library_name;
            String libVersion = this.mineRetro.mineretro_get_system_info().library_version;
            this.corePathMessage = Component.translatable("screen.mineretro.file_screen.core", libName, libVersion);
            // 清空警告和游戏路径提升
            this.gamePathMessage = Component.empty();
            this.message = Component.empty();
        }
    }

    private void loadGame(Button button) {
        if (!coreIsLoaded) {
            this.message = Component.translatable("screen.mineretro.file_screen.core_not_exist");
            return;
        }

        String gamePathStr;
        var sysInfo = this.mineRetro.mineretro_get_system_info();
        String[] extensions = sysInfo.valid_extensions.split("\\|");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filter = stack.mallocPointer(extensions.length);
            for (String extension : extensions) {
                filter.put(stack.UTF8("*." + extension));
            }
            filter.flip();
            String title = I18n.get("screen.mineretro.file_screen.choose_game");
            gamePathStr = TinyFileDialogs.tinyfd_openFileDialog(title, GameConfig.GAME_DIR_PATH.toAbsolutePath() + "\\", filter, null, false);
        }

        if (StringUtils.isNotBlank(gamePathStr)) {
            Path gamePath = Paths.get(gamePathStr);
            if (!gamePath.toFile().exists()) {
                this.message = Component.translatable("screen.mineretro.file_screen.game_not_exist");
                return;
            }
            if (containsChinese(gamePathStr)) {
                this.message = Component.translatable("screen.mineretro.file_screen.game_path_chinese");
                return;
            }
            // 先尝试卸载游戏，再尝试加载游戏
            this.mineRetro.MineretroUnloadGame();
            this.gameIsLoaded = this.mineRetro.MineretroLoadGame(gamePathStr);
            if (!gameIsLoaded) {
                gamePathMessage = Component.empty();
                message = Component.translatable("screen.mineretro.file_screen.game_load_fail");
                return;
            }
            gamePathMessage = Component.translatable("screen.mineretro.file_screen.game", gamePath.getFileName().toString());
        }
    }

    private void runGame(Button button) {
        if (!this.coreIsLoaded) {
            this.message = Component.translatable("screen.mineretro.file_screen.core_not_exist");
            return;
        }
        if (!this.gameIsLoaded) {
            this.message = Component.translatable("screen.mineretro.file_screen.game_not_exist");
            return;
        }
        Minecraft.getInstance().setScreen(new GameScreen(this.mineRetro));
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(graphics);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        graphics.drawCenteredString(font, corePathMessage, this.xPos, this.yPos - 90, 0xFFFFFF);
        graphics.drawCenteredString(font, gamePathMessage, this.xPos, this.yPos - 70, 0xFFFFFF);
        graphics.drawCenteredString(font, message, this.xPos, this.yPos + 70, 0xFF0000);
    }

    @Override
    public void onClose() {
        super.onClose();
        this.mineRetro.MineretroUnloadCore();
        this.coreIsLoaded = false;
    }

    private static boolean containsChinese(String str) {
        // 正则表达式匹配中文字符范围
        String regex = "[\u4e00-\u9fa5]";
        return str.matches(".*" + regex + ".*");
    }
}
