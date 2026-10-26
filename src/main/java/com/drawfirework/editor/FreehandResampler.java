package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import java.util.Objects;

/** Premultiplied area reduction, with optional bounded local contour compensation. */
public final class FreehandResampler {
    public enum Mode {
        SOFT,
        CRISP;
        /** Backward compatibility alias. */
        public static final Mode CLEAR = CRISP;
    }

    private static final long NOT_COL0 = 0xFEFEFEFEFEFEFEFEL;
    private static final long NOT_COL7 = 0x7F7F7F7F7F7F7F7FL;

    private FreehandResampler() {}

    public static CanvasData convert(FreehandCanvas source) {
        return convert(source, Mode.SOFT);
    }

    public static CanvasData convert(FreehandCanvas source, Mode mode) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(mode, "mode");
        int w = source.outputWidth(), h = source.outputHeight(), scale = FreehandCanvas.SCALE;
        int[] output = new int[w * h];
        int scaleSq = scale * scale; // 64 for scale=8
        int halfScaleSq = scaleSq / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                long alpha = 0, red = 0, green = 0, blue = 0;
                int max = 0;
                long occupiedMask = 0;
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        int p = source.pixel(x * scale + sx, y * scale + sy);
                        int a = p >>> 24;
                        alpha += a;
                        red += (long) ((p >>> 16) & 255) * a;
                        green += (long) ((p >>> 8) & 255) * a;
                        blue += (long) (p & 255) * a;
                        max = Math.max(max, a);
                        if (a >= 32) {
                            occupiedMask |= (1L << (sy * scale + sx));
                        }
                    }
                }
                int a = (int) ((alpha + halfScaleSq) / scaleSq);
                if (mode == Mode.CRISP && a > 0 && a < max && hasContinuousContour(occupiedMask)) {
                    // At most 2x area alpha and never above the source alpha. RGB is unchanged.
                    a = Math.min(max, Math.min(a * 2, (int) Math.round(Math.sqrt(a * (double) max))));
                }
                if (a != 0) {
                    output[y * w + x] = (a << 24)
                            | ((int) ((red + alpha / 2) / alpha) << 16)
                            | ((int) ((green + alpha / 2) / alpha) << 8)
                            | (int) ((blue + alpha / 2) / alpha);
                }
            }
        }
        return new CanvasData(w, h, output);
    }

    static long dilate8(long b) {
        long l = (b & NOT_COL0) >>> 1;
        long r = (b & NOT_COL7) << 1;
        long horizontal = b | l | r;
        return horizontal | (horizontal >>> 8) | (horizontal << 8);
    }

    static boolean hasContinuousContour(long mask) {
        if (mask == 0) return false;
        long remaining = mask;
        while (remaining != 0) {
            int seed = Long.numberOfTrailingZeros(remaining);
            long component = 0;
            long frontier = 1L << seed;
            while (frontier != 0) {
                component |= frontier;
                remaining &= ~frontier;
                long next = dilate8(frontier) & mask & remaining;
                frontier = next;
            }
            int minX = 8, maxX = -1, minY = 8, maxY = -1;
            long temp = component;
            while (temp != 0) {
                int bit = Long.numberOfTrailingZeros(temp);
                int x = bit & 7;
                int y = bit >> 3;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y < minY) minY = y;
                if (y > maxY) maxY = y;
                temp &= temp - 1;
            }
            int spanX = maxX - minX + 1;
            int spanY = maxY - minY + 1;
            if (Math.max(spanX, spanY) >= 4) {
                return true;
            }
        }
        return false;
    }
}
