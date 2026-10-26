package com.drawfirework.canvas;

import java.util.Arrays;
import java.util.Objects;

/** Immutable, row-major, straight (not premultiplied) ARGB. Alpha-zero RGB is retained. */
public final class CanvasData {
    public static final int VERSION = 1;
    public static final int DEFAULT_SIZE = 32;
    public static final int MAX_SIZE = 256;
    private final int width, height;
    private final int[] pixels;

    public CanvasData(int width, int height, int[] pixels) {
        int area = checkedArea(width, height);
        Objects.requireNonNull(pixels, "pixels");
        if (pixels.length != area) throw new IllegalArgumentException("Pixel count must equal width * height");
        this.width = width;
        this.height = height;
        this.pixels = pixels.clone();
    }

    public static int checkedArea(int width, int height) {
        if (width < 1 || height < 1 || width > MAX_SIZE || height > MAX_SIZE)
            throw new IllegalArgumentException("Canvas dimensions must be 1..256");
        return Math.toIntExact((long) width * height);
    }

    public static CanvasData blank() { return new CanvasData(DEFAULT_SIZE, DEFAULT_SIZE, new int[DEFAULT_SIZE * DEFAULT_SIZE]); }
    public int version() { return VERSION; }
    public int width() { return width; }
    public int height() { return height; }
    public int pixelCount() { return pixels.length; }
    public int[] pixels() { return pixels.clone(); }
    public int pixel(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        return pixels[y * width + x];
    }
    @Override public boolean equals(Object other) {
        return other instanceof CanvasData c && width == c.width && height == c.height && Arrays.equals(pixels, c.pixels);
    }
    @Override public int hashCode() { return 31 * (31 * width + height) + Arrays.hashCode(pixels); }
}
