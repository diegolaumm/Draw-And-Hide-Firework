package com.drawfirework.canvas;

import java.util.Locale;

/**
 * Sampling strategy choices for CanvasDownsampler (pure Java, client and server safe).
 */
public enum SamplingStrategy {
    EXISTING("existing", "Existing occupancy-adaptive subdivision baseline"),
    DETAIL_PRESERVING("detail_preserving", "Detail-preserving contour and edge adaptive sampling");

    public static final SamplingStrategy DEFAULT = DETAIL_PRESERVING;

    private final String commandName;
    private final String description;

    SamplingStrategy(String commandName, String description) {
        this.commandName = commandName;
        this.description = description;
    }

    public String commandName() {
        return commandName;
    }

    public String description() {
        return description;
    }

    public static SamplingStrategy fromString(String name) {
        if (name == null || name.isBlank()) return DEFAULT;
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (SamplingStrategy s : values()) {
            if (s.commandName.equals(normalized)) return s;
        }
        if ("default".equals(normalized)) return DEFAULT;
        throw new IllegalArgumentException("Unknown sampling strategy: " + name);
    }
}
