package com.github.tartaricacid.mineretro;

import com.github.tartaricacid.mineretro.config.GameConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Mineretro.MOD_ID)
public class Mineretro {
    public static final String MOD_ID = "mineretro";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Mineretro() {
        GameConfig.init();
    }
}
