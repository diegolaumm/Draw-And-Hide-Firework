package com.drawfirework.client;

import com.drawfirework.item.ModItems;
import com.drawfirework.network.EditorCanvasPayload;
import com.drawfirework.network.OpenEditorPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public final class CanvasEditorClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        var key = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.drawfirework.editor",
                InputConstants.Type.KEYBOARD, InputConstants.KEY_G, KeyMapping.Category.CREATIVE));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.gui.screen() != null || client.player == null) continue;
                if (!client.hasSingleplayerServer() || !client.player.isCreative()
                        || !client.player.getMainHandItem().is(ModItems.TEST_FIREWORK)
                        || !ClientPlayNetworking.canSend(OpenEditorPayload.TYPE)) {
                    client.player.sendOverlayMessage(Component.translatable("editor.drawfirework.denied"));
                    continue;
                }
                ClientPlayNetworking.send(new OpenEditorPayload());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(EditorCanvasPayload.OPENED_TYPE, (payload,ctx) -> {
            var world = ctx.player().level();
            ctx.client().execute(() -> {
                var client = ctx.client();
                if (client.level != world || client.player != ctx.player() || client.gui.screen() != null
                        || !client.hasSingleplayerServer() || !client.player.isCreative()
                        || !client.player.getMainHandItem().is(ModItems.TEST_FIREWORK)) return;
                client.gui.setScreen(new CanvasEditorScreen(payload.token(), payload.canvas()));
            });
        });
    }
}
