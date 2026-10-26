package com.drawfirework;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawFirework implements ModInitializer {
    public static final String MOD_ID = "drawfirework";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        com.drawfirework.component.ModDataComponents.initialize();
        com.drawfirework.item.ModItems.initialize();
        com.drawfirework.item.TestItemCommand.initialize();
        com.drawfirework.network.ServerPlayback.initialize();
        com.drawfirework.network.ServerCanvasEditor.initialize();
        LOGGER.info("Initializing Draw & Hide Firework: {} for Minecraft 26.3...", MOD_ID);
    }
}
