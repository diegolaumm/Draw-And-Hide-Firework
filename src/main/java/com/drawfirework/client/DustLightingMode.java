package com.drawfirework.client;

import java.util.Locale;

/**
 * Client-local lighting and color modes for Dust particles (Phase 05C).
 * Controls whether particles use world ambient light or fullbright coordinates,
 * and whether vanilla's random [0.48..1.00] RGB darkening is preserved or bypassed.
 */
public enum DustLightingMode {
    /** Baseline: standard Minecraft world lighting and vanilla random darkening. Comparison/fallback. */
    BASELINE(false, false, "baseline", "World lighting + vanilla randomized color"),
    /** Fullbright: 15728880 light coords (FULL_BRIGHT) with vanilla randomized color. */
    FULLBRIGHT(true, false, "fullbright", "Fullbright (15728880) + vanilla randomized color"),
    /** Fullbright + Exact RGB: 15728880 light coords and exact source RGB (no darkening). */
    FULLBRIGHT_EXACT(true, true, "fullbright_exact", "Fullbright (15728880) + exact source RGB"),
    /** Exact RGB only: world lighting with exact source RGB (no darkening). */
    EXACT_ONLY(false, true, "exact_only", "World lighting + exact source RGB");

    /** Client-local production default, also used by reset and null fallback. */
    public static final DustLightingMode DEFAULT = FULLBRIGHT_EXACT;

    /** Packed fullbright light coordinates: block light 15, sky light 15 (0x00F000F0). */
    public static final int FULL_BRIGHT_COORDS = 15728880;

    private final boolean fullbright;
    private final boolean exactRgb;
    private final String commandName;
    private final String description;

    DustLightingMode(boolean fullbright, boolean exactRgb, String commandName, String description) {
        this.fullbright = fullbright;
        this.exactRgb = exactRgb;
        this.commandName = commandName;
        this.description = description;
    }

    public boolean isFullbright() {
        return fullbright;
    }

    public boolean isExactRgb() {
        return exactRgb;
    }

    public String commandName() {
        return commandName;
    }

    public String description() {
        return description;
    }

    /**
     * Parses a mode string safely (case-insensitive, trims whitespace).
     * Accepts:
     * - "baseline", "world", "normal" -&gt; BASELINE; "default" -&gt; DEFAULT
     * - "fullbright", "bright", "fb" -&gt; FULLBRIGHT
     * - "fullbright_exact", "exact_fullbright", "full", "fbe" -&gt; FULLBRIGHT_EXACT
     * - "exact_only", "exact", "eo" -&gt; EXACT_ONLY
     * Returns null if unrecognized.
     */
    public static DustLightingMode parse(String text) {
        if (text == null) return null;
        String clean = text.trim().toLowerCase(Locale.ROOT);
        return switch (clean) {
            case "baseline", "world", "normal" -> BASELINE;
            case "default" -> DEFAULT;
            case "fullbright", "bright", "fb" -> FULLBRIGHT;
            case "fullbright_exact", "exact_fullbright", "full", "fbe" -> FULLBRIGHT_EXACT;
            case "exact_only", "exact", "eo" -> EXACT_ONLY;
            default -> null;
        };
    }

    /** Converts 24-bit 0xRRGGBB into red float [0..1]. */
    public static float red(int rgb) {
        return ((rgb >> 16) & 0xff) / 255.0f;
    }

    /** Converts 24-bit 0xRRGGBB into green float [0..1]. */
    public static float green(int rgb) {
        return ((rgb >> 8) & 0xff) / 255.0f;
    }

    /** Converts 24-bit 0xRRGGBB into blue float [0..1]. */
    public static float blue(int rgb) {
        return (rgb & 0xff) / 255.0f;
    }
}
