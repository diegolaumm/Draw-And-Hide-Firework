package com.drawfirework.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/**
 * Modal prompt presented when navigating away or closing with unsaved draft modifications.
 * Offers explicit Save Draft, Discard Changes, or Cancel actions.
 */
public final class UnsavedChangesScreen extends Screen {
    private final Screen parent;
    private final Runnable onSaveAndProceed;
    private final Runnable onDiscardAndProceed;

    public UnsavedChangesScreen(Screen parent, Runnable onSaveAndProceed, Runnable onDiscardAndProceed) {
        super(Component.translatable("editor.drawfirework.unsaved_title"));
        this.parent = Objects.requireNonNull(parent, "parent must not be null");
        this.onSaveAndProceed = Objects.requireNonNull(onSaveAndProceed, "onSaveAndProceed must not be null");
        this.onDiscardAndProceed = Objects.requireNonNull(onDiscardAndProceed, "onDiscardAndProceed must not be null");
    }

    @Override
    protected void init() {
        int btnW = 95;
        int btnH = 20;
        int spacing = 6;
        int totalW = btnW * 3 + spacing * 2;
        int startX = (width - totalW) / 2;
        int btnY = height / 2 + 10;

        // Button 1: Save Draft
        addRenderableWidget(Button.builder(Component.translatable("editor.drawfirework.save_draft"), button -> {
            onSaveAndProceed.run();
        }).bounds(startX, btnY, btnW, btnH).build());

        // Button 2: Discard Changes
        addRenderableWidget(Button.builder(Component.translatable("editor.drawfirework.discard"), button -> {
            onDiscardAndProceed.run();
        }).bounds(startX + btnW + spacing, btnY, btnW, btnH).build());

        // Button 3: Cancel
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> cancel())
                .bounds(startX + (btnW + spacing) * 2, btnY, btnW, btnH).build());
    }

    private void cancel() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            cancel();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, CanvasEditorTheme.MODAL_BG);
        graphics.centeredText(font, title, width / 2, height / 2 - 35, CanvasEditorTheme.TEXT_ACCENT_GOLD);
        graphics.centeredText(font, Component.translatable("editor.drawfirework.unsaved_message"),
                width / 2, height / 2 - 15, CanvasEditorTheme.TEXT_TITLE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
