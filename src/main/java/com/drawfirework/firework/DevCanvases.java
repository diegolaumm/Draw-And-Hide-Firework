package com.drawfirework.firework;

import com.drawfirework.canvas.CanvasData;

/** Runtime-generated development presets; not player content or an item source. */
public final class DevCanvases {
    private DevCanvases() {}

    /** Finite palettes share exactly the same smiley geometry and alpha mask. */
    public enum SmileyPalette {
        ORIGINAL(0xffffd23f, 0x8045e8e8, 0xff4060ff, 0xffff3040),
        WARM(0xffffb347, 0x80ff7043, 0xffa83232, 0xffff4060),
        COOL(0xff45c9e8, 0x806988ff, 0xff3040a0, 0xffb875ed);

        private final int face, ring, eyes, mouth;

        SmileyPalette(int face, int ring, int eyes, int mouth) {
            this.face = face; this.ring = ring; this.eyes = eyes; this.mouth = mouth;
        }
    }

    /** 16x16 face: opaque yellow/blue/red, a 50% alpha cyan ring and a fully transparent border. */
    public static CanvasData smiley() {
        return smiley(SmileyPalette.ORIGINAL);
    }

    /** Rejects null; the enum deliberately exposes no arbitrary colour or dimension parameters. */
    public static CanvasData smiley(SmileyPalette palette) {
        java.util.Objects.requireNonNull(palette, "palette");
        int n = 16;
        int[] p = new int[n * n];
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            double dx = x + .5 - 8, dy = y + .5 - 8, r = Math.sqrt(dx * dx + dy * dy);
            int c = 0x00ffffff; // alpha 0 with retained RGB must stay invisible
            if (r <= 5.5) c = palette.face;
            else if (r <= 6.8) c = palette.ring;
            if ((y == 5 || y == 6) && (x == 5 || x == 10)) c = palette.eyes;
            if (y == 10 && x >= 5 && x <= 10 || y == 9 && (x == 4 || x == 11)) c = palette.mouth;
            p[y * n + x] = c;
        }
        return new CanvasData(n, n, p);
    }

    /** 32x32 opaque hue gradient: 1024 visible pixels, enough to saturate the core budget. */
    public static CanvasData dense() {
        int n = 32;
        int[] p = new int[n * n];
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            float hue = x / (float) n, light = .55f + .4f * (1 - y / (float) n);
            p[y * n + x] = 0xff000000 | hsv(hue, .85f, light);
        }
        return new CanvasData(n, n, p);
    }

    /**
     * 25x25 diagnostic grid: yellow vertical and cyan horizontal lines every 6 px, red centre cross.
     * 225 visible pixels (below the 266-sample Bloom limit), so a full budget samples every pixel.
     */
    public static CanvasData grid() {
        int n = 25;
        int[] p = new int[n * n];
        for (int y = 0; y < n; y++) for (int x = 0; x < n; x++) {
            if (x % 6 == 0) p[y * n + x] = 0xffffe040;
            if (y % 6 == 0) p[y * n + x] = 0xff40e0ff;
        }
        for (int[] d : new int[][]{{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}}) p[(12 + d[1]) * n + 12 + d[0]] = 0xffff2020;
        return new CanvasData(n, n, p);
    }

    /**
     * 24x16 (3:2) outline rectangle, 2 px white border, differently coloured 2x2 corners
     * (TL red, TR green, BL blue, BR yellow). 144 visible pixels.
     */
    public static CanvasData rectangle() {
        int w = 24, h = 16;
        int[] p = new int[w * h];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (x >= 2 && x < w - 2 && y >= 2 && y < h - 2) continue;
            boolean left = x < 2, right = x >= w - 2, top = y < 2, bottom = y >= h - 2;
            int c = 0xffffffff;
            if (left && top) c = 0xffff3030;
            else if (right && top) c = 0xff30e050;
            else if (left && bottom) c = 0xff3070ff;
            else if (right && bottom) c = 0xffffe030;
            p[y * w + x] = c;
        }
        return new CanvasData(w, h, p);
    }

    private static int hsv(float h, float s, float v) {
        float c = v * s, hp = h * 6, x = c * (1 - Math.abs(hp % 2 - 1)), m = v - c;
        float r, g, b;
        switch ((int) hp) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            default -> { r = c; g = 0; b = x; }
        }
        return Math.round((r + m) * 255) << 16 | Math.round((g + m) * 255) << 8 | Math.round((b + m) * 255);
    }
}
