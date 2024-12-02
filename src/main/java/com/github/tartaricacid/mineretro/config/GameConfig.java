package com.github.tartaricacid.mineretro.config;

import com.github.tartaricacid.mineretro.Mineretro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameConfig {
    public static final Path CONFIG_DIR_PATH = Paths.get(".").resolve("config").resolve(Mineretro.MOD_ID);
    public static final Path CORE_DIR_PATH = CONFIG_DIR_PATH.resolve("core");
    public static final Path GAME_DIR_PATH = CONFIG_DIR_PATH.resolve("game");
    public static final Path SYSTEM_DIR_PATH = CONFIG_DIR_PATH.resolve("system");
    public static final Path SAVE_DIR_PATH = CONFIG_DIR_PATH.resolve("save");

    public static void init() {
        createDirectory(CONFIG_DIR_PATH);
        createDirectory(CORE_DIR_PATH);
        createDirectory(GAME_DIR_PATH);
        createDirectory(SYSTEM_DIR_PATH);
        createDirectory(SAVE_DIR_PATH);
    }

    public static void createDirectory(Path path) {
        if (!path.toFile().isDirectory()) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
