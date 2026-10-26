package com.drawfirework.client;

import com.drawfirework.editor.CanvasEditorModel;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;

/** Screen's actual canvas event handling, isolated from Minecraft/Screen initialization for tests. */
public final class CanvasEditorInput {
    private final CanvasEditorModel model;
    private boolean drawing;

    public CanvasEditorInput(CanvasEditorModel model) { this.model = model; }
    private static boolean leftButton(MouseButtonEvent event) {
        return event.button() == InputConstants.MOUSE_BUTTON_LEFT;
    }
    public void reset() { drawing = false; model.endStroke(); }
    public boolean click(MouseButtonEvent event, int left, int top, int cell, int color) {
        if (!leftButton(event)) return false;
        reset();
        drawing = model.paint(event.x(), event.y(), left, top, cell, color);
        return drawing;
    }
    public boolean drag(MouseButtonEvent event, int left, int top, int cell, int color) {
        if (!leftButton(event) || !drawing) return false;
        model.paint(event.x(), event.y(), left, top, cell, color);
        return true; // Consume an active stroke even while outside the canvas.
    }
    public boolean release(MouseButtonEvent event) {
        if (!leftButton(event) || !drawing) return false;
        reset();
        return true;
    }
}
