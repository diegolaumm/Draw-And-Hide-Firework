package com.drawfirework.client;

import java.util.SplittableRandom;

/** Development presets only; offsets are generated once, never randomized per tick. */
public record DustStyle(int holdTicks, int particlesPerPoint, float localOffset, float sizeVariation,
                        float bloomTarget) {
    public static final DustStyle SOFT = new DustStyle(14, 3, .14f, .05f);
    public static final DustStyle BLOOM_115 = new DustStyle(14, 3, .14f, .05f, 1.15f);
    public static final DustStyle BLOOM_130 = new DustStyle(14, 3, .14f, .05f, 1.30f);
    /** Phase 04A.1 diagnostic only: Bloom 130 without the 3-particle micro offset (offsets and z depth = 0). */
    public static final DustStyle BLOOM_130_PLAIN = new DustStyle(14, 3, 0f, .05f, 1.30f);
    public static final DustStyle LEGACY = new DustStyle(28, 1, 0, 0);
    public static final int BLOOM_TICKS = 4;
    private static final long SEED = 0x4452415746495245L;

    public DustStyle(int holdTicks, int particlesPerPoint, float localOffset, float sizeVariation) {
        this(holdTicks, particlesPerPoint, localOffset, sizeVariation, 1);
    }

    public DustStyle {
        if (holdTicks < 1 || holdTicks > 100 || particlesPerPoint < 1 || particlesPerPoint > 3
                || !Float.isFinite(localOffset) || localOffset < 0 || localOffset > .2f
                || !Float.isFinite(sizeVariation) || sizeVariation < 0 || sizeVariation > .1f
                || !Float.isFinite(bloomTarget) || bloomTarget < 1 || bloomTarget > 1.30f) {
            throw new IllegalArgumentException("Unsafe Dust development preset");
        }
    }

    public Sample sample(int pointIndex, int particleIndex, float x, float y) {
        var random = new SplittableRandom(SEED + pointIndex * 31L + particleIndex);
        double angle = random.nextDouble() * Math.PI * 2;
        double radius = Math.sqrt(random.nextDouble()) * localOffset;
        return new Sample(x + (float) (Math.cos(angle) * radius),
                y + (float) (Math.sin(angle) * radius),
                (float) ((random.nextDouble() * 2 - 1) * localOffset * .3),
                (float) (random.nextDouble() * Math.PI * 2));
    }

    public float holdSize(int tick, float phase) {
        if (tick < FireworkTimeline.FORM_END || tick >= FireworkTimeline.FORM_END + holdTicks) return 1;
        double progress = (tick - FireworkTimeline.FORM_END) / (double) holdTicks;
        return 1 + sizeVariation * (float) (Math.sin(progress * Math.PI) * Math.sin(tick * .45 + phase));
    }

    public static float smoothStep(float progress) {
        float t = Math.clamp(progress, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public float bloomSize(int tick) {
        return 1 + (bloomTarget - 1) * smoothStep((tick - FireworkTimeline.FORM_END) / (float) BLOOM_TICKS);
    }

    public float particleSize(int tick, float phase) {
        return holdSize(tick, phase) * bloomSize(tick);
    }

    public FireworkTimeline timeline() { return new FireworkTimeline(holdTicks); }

    public record Sample(float x, float y, float z, float phase) {}
}
