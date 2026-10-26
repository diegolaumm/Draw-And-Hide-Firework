package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;

/** Private editable copy. Coordinates are GUI-scaled units supplied by Screen. */
public final class CanvasEditorModel {
    private final int width, height;
    private final int[] pixels;
    private int last = -1;

    public CanvasEditorModel(CanvasData source) {
        if (source == null) source = new CanvasData(16, 16, new int[256]);
        ItemCanvasCodecs.validate(source);
        width = source.width(); height = source.height(); pixels = source.pixels();
    }
    public int width() { return width; }
    public int height() { return height; }
    public int pixel(int x, int y) { return pixels[y * width + x]; }
    public CanvasData snapshot() { return new CanvasData(width, height, pixels); }

    public int indexAt(double x, double y, int left, int top, int cell) {
        if (cell < 1 || !Double.isFinite(x) || !Double.isFinite(y)
                || x < left || y < top || x >= left + width * cell || y >= top + height * cell) return -1;
        return ((int)(y - top) / cell) * width + (int)(x - left) / cell;
    }
    public void endStroke() { last = -1; }
    public boolean paint(double x, double y, int left, int top, int cell, int argb) {
        int next = indexAt(x, y, left, top, cell);
        if (next < 0) { endStroke(); return false; }
        // Bresenham between received mouse samples, avoiding gaps during fast drags.
        int x0 = (last < 0 ? next : last) % width, y0 = (last < 0 ? next : last) / width;
        int x1 = next % width, y1 = next / width;
        int dx = Math.abs(x1-x0), sx = x0<x1 ? 1 : -1;
        int dy = -Math.abs(y1-y0), sy = y0<y1 ? 1 : -1, error = dx+dy;
        while (true) {
            pixels[y0 * width + x0] = argb;
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2*error;
            if (e2 >= dy) { error += dy; x0 += sx; }
            if (e2 <= dx) { error += dx; y0 += sy; }
        }
        last = next;
        return true;
    }
}
