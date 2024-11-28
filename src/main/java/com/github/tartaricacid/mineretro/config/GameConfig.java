package com.github.tartaricacid.mineretro.config;

import com.github.tartaricacid.mineretro.Mineretro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameConfig {
    private static final Path CONFIG_PATH = Paths.get(".").resolve("config").resolve(Mineretro.MOD_ID);
    private static final Path CORE_PATH = CONFIG_PATH.resolve("core.dll");
    private static final Path GAME_PATH = CONFIG_PATH.resolve("game.nes");

    public static void init() {
        if (!CONFIG_PATH.toFile().isDirectory()) {
            try {
                Files.createDirectories(CONFIG_PATH);
            } catch (IOException e) {
                Mineretro.LOGGER.error(e.getMessage());
            }
        }
    }

    public static String getCorePath() {
        return CORE_PATH.toAbsolutePath().toString();
    }

    public static String getGamePath() {
        return GAME_PATH.toAbsolutePath().toString();
    }

    public static boolean isCoreExists() {
        return Files.exists(CORE_PATH);
    }

    public static boolean isGameExists() {
        return Files.exists(GAME_PATH);
    }
}
