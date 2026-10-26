package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;

/** Session-only bounded raster; independent of Minecraft and rendering. */
public final class FreehandCanvas {
    public static final int SCALE = 8;
    public static final int MAX_HISTORY = 16;

    private final CanvasData original;
    private final int width, height;
    private final int[] pixels, strokeBase;
    private final byte[] coverage;
    private final ArrayDeque<int[]> undoStack = new ArrayDeque<>();
    private final ArrayDeque<int[]> redoStack = new ArrayDeque<>();

    private boolean active, changed;
    private int[] strokeSnapshot;
    private double lastX, lastY;
    private int strokeColor, diameter = 8;
    private long revision;

    public FreehandCanvas(CanvasData source) {
        original = source == null ? CanvasData.blank() : source;
        if (original.width() > 32 || original.height() > 32) {
            throw new IllegalArgumentException("Output must be 1..32");
        }
        width = original.width() * SCALE;
        height = original.height() * SCALE;
        pixels = new int[width * height];
        strokeBase = new int[pixels.length];
        coverage = new byte[pixels.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = original.pixel(x / SCALE, y / SCALE);
            }
        }
    }

    public int width() { return width; }
    public int height() { return height; }
    public int outputWidth() { return original.width(); }
    public int outputHeight() { return original.height(); }
    public long revision() { return revision; }
    public int diameter() { return diameter; }
    public boolean isChanged() { return changed; }
    public boolean isActive() { return active; }

    public int pixel(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        return pixels[y * width + x];
    }

    public int[] pixels() {
        return pixels.clone();
    }

    public void setDiameter(int value) {
        if (value < 1 || value > 64) throw new IllegalArgumentException("Brush must be 1..64");
        endStroke();
        diameter = value;
    }

    public void endStroke() {
        if (active) {
            active = false;
            if (strokeSnapshot != null) {
                if (!Arrays.equals(pixels, strokeSnapshot)) {
                    commitHistory(strokeSnapshot);
                }
                strokeSnapshot = null;
            }
        }
    }

    private void commitHistory(int[] preState) {
        undoStack.addLast(preState);
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.removeFirst();
        }
        redoStack.clear();
        changed = true;
        revision++;
    }

    public boolean canUndo() {
        return (active && strokeSnapshot != null && !Arrays.equals(pixels, strokeSnapshot)) || !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return (!active || strokeSnapshot == null || Arrays.equals(pixels, strokeSnapshot)) && !redoStack.isEmpty();
    }

    public boolean undo() {
        endStroke();
        if (undoStack.isEmpty()) return false;
        redoStack.addLast(pixels.clone());
        int[] prev = undoStack.removeLast();
        System.arraycopy(prev, 0, pixels, 0, pixels.length);
        changed = true;
        revision++;
        return true;
    }

    public boolean redo() {
        endStroke();
        if (redoStack.isEmpty()) return false;
        undoStack.addLast(pixels.clone());
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.removeFirst();
        }
        int[] next = redoStack.removeLast();
        System.arraycopy(next, 0, pixels, 0, pixels.length);
        changed = true;
        revision++;
        return true;
    }

    public boolean paint(double x, double y, int argb) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0 || x >= width || y >= height) {
            endStroke();
            return false;
        }
        if (!active || strokeColor != argb) {
            if (active) {
                endStroke();
            }
            strokeSnapshot = pixels.clone();
            System.arraycopy(pixels, 0, strokeBase, 0, pixels.length);
            Arrays.fill(coverage, (byte) 0);
            lastX = x;
            lastY = y;
            strokeColor = argb;
            active = true;
        }
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(x - lastX, y - lastY) / Math.max(0.25, diameter / 8.0)));
        for (int i = 0; i <= steps; i++) {
            stamp(lastX + (x - lastX) * i / steps, lastY + (y - lastY) * i / steps, argb);
        }
        lastX = x;
        lastY = y;
        return true;
    }

    private void stamp(double cx, double cy, int argb) {
        double radius = diameter / 2.0;
        int x0 = Math.max(0, (int) Math.floor(cx - radius - 1));
        int x1 = Math.min(width - 1, (int) Math.ceil(cx + radius));
        int y0 = Math.max(0, (int) Math.floor(cy - radius - 1));
        int y1 = Math.min(height - 1, (int) Math.ceil(cy + radius));
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                int c = (int) Math.round(255 * Math.clamp(radius + 0.5 - Math.hypot(x + 0.5 - cx, y + 0.5 - cy), 0, 1));
                int index = y * width + x;
                if (c <= (coverage[index] & 255)) continue;
                coverage[index] = (byte) c;
                int result = composite(strokeBase[index], argb, c);
                if (pixels[index] != result) {
                    pixels[index] = result;
                    changed = true;
                    revision++;
                }
            }
        }
    }

    public boolean fill(double x, double y, int targetColor) {
        endStroke();
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }
        int px = (int) Math.floor(x);
        int py = (int) Math.floor(y);
        int sourceColor = pixels[py * width + px];
        boolean sourceTransparent = (sourceColor >>> 24) == 0;
        boolean targetTransparent = (targetColor >>> 24) == 0;

        // If source already matches target, nothing to change
        if (sourceTransparent && targetTransparent) return false;
        if (!sourceTransparent && !targetTransparent && sourceColor == targetColor) return false;

        int[] before = pixels.clone();
        int normalizedTarget = targetTransparent ? 0 : targetColor;

        int total = width * height;
        int[] queue = new int[total];
        int head = 0, tail = 0;

        pixels[py * width + px] = normalizedTarget;
        queue[tail++] = py * width + px;

        while (head < tail) {
            int pos = queue[head++];
            int cx = pos % width;
            int cy = pos / width;

            if (cx > 0) {
                int left = pos - 1;
                if (matchesForFill(pixels[left], sourceColor, sourceTransparent)) {
                    pixels[left] = normalizedTarget;
                    queue[tail++] = left;
                }
            }
            if (cx < width - 1) {
                int right = pos + 1;
                if (matchesForFill(pixels[right], sourceColor, sourceTransparent)) {
                    pixels[right] = normalizedTarget;
                    queue[tail++] = right;
                }
            }
            if (cy > 0) {
                int up = pos - width;
                if (matchesForFill(pixels[up], sourceColor, sourceTransparent)) {
                    pixels[up] = normalizedTarget;
                    queue[tail++] = up;
                }
            }
            if (cy < height - 1) {
                int down = pos + width;
                if (matchesForFill(pixels[down], sourceColor, sourceTransparent)) {
                    pixels[down] = normalizedTarget;
                    queue[tail++] = down;
                }
            }
        }

        if (tail > 0) {
            commitHistory(before);
            return true;
        }
        return false;
    }

    private static boolean matchesForFill(int pixel, int sourceColor, boolean sourceTransparent) {
        if (sourceTransparent) {
            return (pixel >>> 24) == 0;
        }
        return pixel == sourceColor;
    }

    static int composite(int dst, int src, int coverage) {
        double amount = coverage / 255.0, da = (dst >>> 24) / 255.0;
        if ((src >>> 24) == 0) {
            int a = (int) Math.round(255 * da * (1 - amount));
            return a == 0 ? 0 : (a << 24) | (dst & 0xFFFFFF);
        }
        double sa = (src >>> 24) / 255.0 * amount, a = sa + da * (1 - sa);
        if (a == 0) return 0;
        int result = (int) Math.round(a * 255) << 24;
        for (int shift = 16; shift >= 0; shift -= 8)
            result |= (int) Math.round((((src >>> shift) & 255) * sa + ((dst >>> shift) & 255) * da * (1 - sa)) / a) << shift;
        return result;
    }

    public CanvasData snapshot() {
        return snapshot(FreehandResampler.Mode.SOFT);
    }

    public CanvasData snapshot(FreehandResampler.Mode mode) {
        return changed ? FreehandResampler.convert(this, mode) : original;
    }

    /**
     * Exports the canvas raster as a full 256x256 ARGB pixel array for draft persistence.
     * If the working canvas is 256x256, returns a direct copy.
     * If the canvas has smaller dimensions (e.g. 128x128 for legacy 16x16 items),
     * deterministically upsamples using nearest-neighbor block replication.
     */
    public int[] toDraft256Pixels() {
        if (width == 256 && height == 256) {
            return pixels.clone();
        }
        int[] out = new int[256 * 256];
        for (int dy = 0; dy < 256; dy++) {
            int sy = dy * height / 256;
            for (int dx = 0; dx < 256; dx++) {
                int sx = dx * width / 256;
                out[dy * 256 + dx] = pixels[sy * width + sx];
            }
        }
        return out;
    }

    /**
     * Checks if the given 256x256 draft buffer can be loaded into this canvas without any loss of detail.
     * For 256x256 canvas, this is always true.
     * For smaller canvases (e.g. 128x128 for 16x16 items), returns true only if all pixels within
     * each downsampling block (e.g. 2x2) are identical.
     */
    public boolean canLoadLosslessly(int[] draft256) {
        if (draft256 == null) return false;
        if (draft256.length != 256 * 256) {
            return draft256.length == pixels.length;
        }
        if (width == 256 && height == 256) {
            return true;
        }
        int blockW = 256 / width;
        int blockH = 256 / height;
        for (int y = 0; y < height; y++) {
            int y0 = y * blockH;
            for (int x = 0; x < width; x++) {
                int x0 = x * blockW;
                int rep = draft256[y0 * 256 + x0];
                for (int dy = 0; dy < blockH; dy++) {
                    int row = (y0 + dy) * 256;
                    for (int dx = 0; dx < blockW; dx++) {
                        if (draft256[row + (x0 + dx)] != rep) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Loads a high-resolution draft pixel buffer directly into the editable raster.
     * If the canvas is smaller than 256x256 and the draft cannot be loaded losslessly,
     * throws IllegalArgumentException without mutating the current canvas.
     * Safely clears previous undo/redo history and active strokes only on successful load.
     */
    public void loadDraft(int[] newPixels) {
        loadDraft(newPixels, false);
    }

    public void loadDraft(int[] newPixels, boolean allowLossy) {
        if (newPixels == null) {
            throw new IllegalArgumentException("Pixel buffer must not be null");
        }
        if (newPixels.length == 256 * 256) {
            if (width != 256 || height != 256) {
                if (!allowLossy && !canLoadLosslessly(newPixels)) {
                    throw new IllegalArgumentException("Cannot load draft: draft contains high-resolution details that cannot be losslessly represented on a "
                            + width + "x" + height + " canvas");
                }
            }
        } else if (newPixels.length != pixels.length) {
            throw new IllegalArgumentException("Pixel buffer length mismatch: expected 256x256 ("
                    + (256 * 256) + ") or " + pixels.length + " but got " + newPixels.length);
        }

        endStroke();
        undoStack.clear();
        redoStack.clear();

        if (newPixels.length == 256 * 256) {
            if (width == 256 && height == 256) {
                System.arraycopy(newPixels, 0, pixels, 0, pixels.length);
            } else {
                int blockW = 256 / width;
                int blockH = 256 / height;
                for (int y = 0; y < height; y++) {
                    int dy = y * blockH;
                    for (int x = 0; x < width; x++) {
                        int dx = x * blockW;
                        pixels[y * width + x] = newPixels[dy * 256 + dx];
                    }
                }
            }
        } else {
            System.arraycopy(newPixels, 0, pixels, 0, pixels.length);
        }

        Arrays.fill(strokeBase, 0);
        Arrays.fill(coverage, (byte) 0);
        changed = true;
        revision++;
    }

    /**
     * Resets the canvas to completely blank transparent pixels.
     * Safely clears undo/redo history and active strokes.
     */
    public void resetBlank() {
        endStroke();
        undoStack.clear();
        redoStack.clear();
        Arrays.fill(pixels, 0);
        Arrays.fill(strokeBase, 0);
        Arrays.fill(coverage, (byte) 0);
        changed = true;
        revision++;
    }
}
