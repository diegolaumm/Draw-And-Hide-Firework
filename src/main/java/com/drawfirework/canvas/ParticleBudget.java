package com.drawfirework.canvas;

/** Counts particles, not source pixels. Decorations never borrow the core allowance. */
public record ParticleBudget(int coreParticles, int copiesPerSample, int decorationParticles) {
    public static final int MAX_CORE = 800;
    public static final int MAX_DECORATION = 200;
    public static final ParticleBudget BLOOM = new ParticleBudget(800, 3, 200);
    public ParticleBudget {
        if (coreParticles < 0 || coreParticles > MAX_CORE || copiesPerSample < 1 || copiesPerSample > MAX_CORE
                || decorationParticles < 0 || decorationParticles > MAX_DECORATION)
            throw new IllegalArgumentException("Invalid independent particle budgets");
    }
    public int sampleLimit() { return coreParticles / copiesPerSample; }
    public int coreCount(int samples) {
        if (samples < 0 || samples > sampleLimit()) throw new IllegalArgumentException("Sample budget exceeded");
        return samples * copiesPerSample;
    }
}
