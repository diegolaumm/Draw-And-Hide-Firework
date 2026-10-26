package com.drawfirework.client;

import com.drawfirework.editor.FreehandCanvas;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import java.util.Objects;

/** Screen's actual input path; coordinates are already GUI-scaled by Minecraft. */
public final class FreehandInput {
    public enum Tool { BRUSH, FILL }

    private final FreehandCanvas canvas;
    private Tool tool = Tool.BRUSH;
    private boolean drawing;

    public FreehandInput(FreehandCanvas canvas) {
        this.canvas = Objects.requireNonNull(canvas);
    }

    public void setTool(Tool tool) {
        this.tool = Objects.requireNonNull(tool);
        reset();
    }

    public Tool tool() { return tool; }

    public void reset() {
        drawing = false;
        canvas.endStroke();
    }

    private boolean paint(MouseButtonEvent e, int left, int top, int width, int height, int color) {
        if (width <= 0 || height <= 0) {
            canvas.endStroke();
            return false;
        }
        return canvas.paint((e.x() - left) * canvas.width() / (double) width,
                (e.y() - top) * canvas.height() / (double) height, color);
    }

    public boolean click(MouseButtonEvent e, int left, int top, int width, int height, int color) {
        return click(e, left, top, width, height, color, this.tool);
    }

    public boolean click(MouseButtonEvent e, int left, int top, int width, int height, int color, Tool activeTool) {
        if (e.button() != InputConstants.MOUSE_BUTTON_LEFT) return false;
        reset();
        if (activeTool == Tool.FILL) {
            if (width <= 0 || height <= 0) return false;
            return canvas.fill((e.x() - left) * canvas.width() / (double) width,
                    (e.y() - top) * canvas.height() / (double) height, color);
        }
        drawing = paint(e, left, top, width, height, color);
        return drawing;
    }

    public boolean drag(MouseButtonEvent e, int left, int top, int width, int height, int color) {
        if (e.button() != InputConstants.MOUSE_BUTTON_LEFT || !drawing || tool == Tool.FILL) return false;
        paint(e, left, top, width, height, color);
        return true;
    }

    public boolean release(MouseButtonEvent e) {
        if (e.button() != InputConstants.MOUSE_BUTTON_LEFT || !drawing) return false;
        reset();
        return true;
    }
}
