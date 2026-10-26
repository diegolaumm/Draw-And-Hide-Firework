package com.drawfirework.canvas;

import java.util.Locale;

/**
 * Particle quality tiers controlling core particle budget and sample counts.
 * Pure Java, independent of Minecraft graphics/rendering classes for automated unit testing.
 */
public enum ParticleQuality {
    STANDARD("standard", 800, "Standard quality (800 core particles, 266 samples)"),
    BALANCED("balanced", 1100, "Balanced quality (1100 core particles, 366 samples)"),
    HIGH("high", 1400, "High quality (1400 core particles, 466 samples)");

    public static final ParticleQuality DEFAULT = BALANCED;

    private final String commandName;
    private final int coreBudget;
    private final String description;

    ParticleQuality(String commandName, int coreBudget, String description) {
        this.commandName = commandName;
        this.coreBudget = coreBudget;
        this.description = description;
    }

    public String commandName() {
        return commandName;
    }

    public int coreBudget() {
        return coreBudget;
    }

    public String description() {
        return description;
    }

    /** Maximum samples allowed for the given copies per point (e.g. 3 for Bloom 130). */
    public int sampleLimit(int copiesPerPoint) {
        if (copiesPerPoint < 1) throw new IllegalArgumentException("copiesPerPoint must be >= 1");
        return coreBudget / copiesPerPoint;
    }

    /** Maximum samples under default Bloom 130 (3 particles per sample). */
    public int sampleLimit() {
        return sampleLimit(3);
    }

    /**
     * Total core particles generated for a sample count under given copies per sample.
     * Rejects counts that exceed this tier's core budget.
     */
    public int coreCount(int samples, int copiesPerPoint) {
        if (samples < 0 || samples > sampleLimit(copiesPerPoint)) {
            throw new IllegalArgumentException("Sample count " + samples + " exceeds limit "
                    + sampleLimit(copiesPerPoint) + " for " + name() + " budget (" + coreBudget + ")");
        }
        return samples * copiesPerPoint;
    }

    public static ParticleQuality fromString(String name) {
        if (name == null || name.isBlank()) return DEFAULT;
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (ParticleQuality q : values()) {
            if (q.commandName.equals(normalized)) return q;
        }
        if ("default".equals(normalized)) return DEFAULT;
        throw new IllegalArgumentException("Unknown particle quality: " + name);
    }
}
