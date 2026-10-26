package com.drawfirework.firework;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.CanvasDownsampler;
import com.drawfirework.canvas.ParticleBudget;
import com.drawfirework.canvas.ParticleQuality;
import com.drawfirework.canvas.SamplingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Renderer-neutral core image for the Dust lifecycle (pure Java; no client classes).
 * Points are local billboard offsets in blocks (+X right, +Y up) before the formation spread.
 */
public record DustPattern(List<Point> points) {
    /** Phase 02 heart spacing; canvases use the same visual footprint (11 heart columns). */
    public static final float HEART_SPACING = .45f;
    public static final float CANVAS_SPAN = 11 * HEART_SPACING;
    /** Lower bound for semi-transparent samples so alpha 1..254 remains visible but smaller. */
    public static final float MIN_ALPHA_SIZE = .35f;

    private static final String[] HEART_MASK = {
            ".###...###.", "#####.#####", "###########", "###########",
            ".#########.", "..#######..", "...#####...", "....###....", ".....#....."
    };
    private static final int[] HEART_COLORS = {
            0xff496c, 0xff7043, 0xffcc45, 0x68d875, 0x45c9e8,
            0x6988ff, 0xb875ed, 0xef72c6, 0xff496c
    };
    /** Exact Phase 02 regression pattern: 63 points, row-major, row colour gradient. */
    public static final DustPattern HEART = heart();

    /**
     * @param rgb       opaque 24-bit colour (straight, not premultiplied)
     * @param sizeScale Dust base-size multiplier; 1 for opaque pixels, smaller for partial alpha
     */
    public record Point(float x, float y, int rgb, float sizeScale) {
        public Point {
            if (!Float.isFinite(x) || !Float.isFinite(y) || (rgb & 0xff000000) != 0
                    || !Float.isFinite(sizeScale) || sizeScale < MIN_ALPHA_SIZE || sizeScale > 1)
                throw new IllegalArgumentException("Invalid Dust point");
        }
    }

    public DustPattern {
        points = List.copyOf(Objects.requireNonNull(points, "points"));
        if (points.size() > ParticleBudget.MAX_CORE) throw new IllegalArgumentException("Too many core points");
    }

    /** Core particles spawned for {@code copiesPerPoint}; rejects anything above the 800 core budget. */
    public int coreParticles(int copiesPerPoint) {
        return new ParticleBudget(ParticleBudget.MAX_CORE, copiesPerPoint, 0).coreCount(points.size());
    }

    /** Core particles spawned for {@code copiesPerPoint} checked against a custom core budget limit. */
    public int coreParticles(int copiesPerPoint, int maxCoreBudget) {
        if (copiesPerPoint < 1) throw new IllegalArgumentException("copiesPerPoint must be positive");
        if (maxCoreBudget < 0) throw new IllegalArgumentException("maxCoreBudget cannot be negative");
        int limit = maxCoreBudget / copiesPerPoint;
        if (points.size() > limit) {
            throw new IllegalArgumentException("Sample count " + points.size() + " exceeds budget " + maxCoreBudget);
        }
        return points.size() * copiesPerPoint;
    }

    /** Core particles spawned for {@code copiesPerPoint} checked against quality tier core budget. */
    public int coreParticles(int copiesPerPoint, ParticleQuality quality) {
        Objects.requireNonNull(quality, "quality");
        return coreParticles(copiesPerPoint, quality.coreBudget());
    }

    /**
     * Downsamples according to the specified particle quality tier and sampling strategy.
     */
    public static DustPattern fromCanvas(CanvasData canvas, int copiesPerPoint, ParticleQuality quality, SamplingStrategy strategy) {
        Objects.requireNonNull(canvas, "canvas");
        Objects.requireNonNull(quality, "quality");
        return fromCanvas(canvas, copiesPerPoint, quality.sampleLimit(copiesPerPoint), quality.coreBudget(), strategy);
    }

    /**
     * Downsamples according to the specified particle quality tier with default detail-preserving strategy.
     */
    public static DustPattern fromCanvas(CanvasData canvas, int copiesPerPoint, ParticleQuality quality) {
        return fromCanvas(canvas, copiesPerPoint, quality, SamplingStrategy.DEFAULT);
    }

    /**
     * Diagnostic and budget-controlled overload supporting custom core particle limits and sampling strategies.
     */
    public static DustPattern fromCanvas(CanvasData canvas, int copiesPerPoint, int maxSamples, int maxCoreBudget, SamplingStrategy strategy) {
        Objects.requireNonNull(canvas, "canvas");
        if (copiesPerPoint < 1) throw new IllegalArgumentException("copiesPerPoint must be positive");
        if (maxSamples < 1) throw new IllegalArgumentException("maxSamples must be positive");
        if (maxCoreBudget < 0) throw new IllegalArgumentException("maxCoreBudget cannot be negative");
        int limit = Math.min(maxSamples, maxCoreBudget / copiesPerPoint);
        var samples = CanvasDownsampler.sample(canvas, limit, strategy == null ? SamplingStrategy.DEFAULT : strategy);
        var points = new ArrayList<Point>(samples.size());
        for (var s : samples) {
            points.add(new Point((float) (s.x() * CANVAS_SPAN), (float) (s.y() * CANVAS_SPAN),
                    s.argb() & 0xffffff, alphaSize(s.argb() >>> 24)));
        }
        return new DustPattern(points);
    }

    /**
     * Downsamples within the 800-particle core budget for the given copies per sample.
     * Alpha 0 never produces a point (downsampler excludes it); alpha 1..255 keeps straight RGB
     * and maps alpha to a smaller opaque Dust size because vanilla Dust has no alpha blending.
     */
    public static DustPattern fromCanvas(CanvasData canvas, int copiesPerPoint) {
        return fromCanvas(canvas, copiesPerPoint, ParticleBudget.MAX_CORE);
    }

    /**
     * Phase 04A.1 diagnostic overload: {@code maxSamples} may only lower the sample count below the
     * 800-core-particle limit for {@code copiesPerPoint}; it can never raise it.
     */
    public static DustPattern fromCanvas(CanvasData canvas, int copiesPerPoint, int maxSamples) {
        Objects.requireNonNull(canvas, "canvas");
        int limit = new ParticleBudget(ParticleBudget.MAX_CORE, copiesPerPoint, 0).sampleLimit();
        if (maxSamples < 1) throw new IllegalArgumentException("maxSamples must be positive");
        limit = Math.min(limit, maxSamples);
        var samples = CanvasDownsampler.sample(canvas, limit);
        var points = new ArrayList<Point>(samples.size());
        for (var s : samples) {
            points.add(new Point((float) (s.x() * CANVAS_SPAN), (float) (s.y() * CANVAS_SPAN),
                    s.argb() & 0xffffff, alphaSize(s.argb() >>> 24)));
        }
        return new DustPattern(points);
    }

    /**
     * Whole-pattern layout scale about the fixed centre (0,0): multiplies sample spacing only.
     * Colours, aspect ratio, point count and Dust particle size are unchanged. Allowed 1.0..1.5.
     */
    public DustPattern scaled(float factor) {
        if (!Float.isFinite(factor) || factor < 1f || factor > 1.5f)
            throw new IllegalArgumentException("Layout scale must be 1.0..1.5");
        if (factor == 1f) return this;
        var scaledPoints = new ArrayList<Point>(points.size());
        for (Point p : points) scaledPoints.add(new Point(p.x() * factor, p.y() * factor, p.rgb(), p.sizeScale()));
        return new DustPattern(scaledPoints);
    }

    /** Area-proportional size for partial alpha; exactly 1 for opaque pixels. */
    public static float alphaSize(int alpha) {
        if (alpha <= 0 || alpha > 255) throw new IllegalArgumentException("Visible alpha must be 1..255");
        if (alpha == 255) return 1;
        return Math.max(MIN_ALPHA_SIZE, (float) Math.sqrt(alpha / 255.0));
    }

    private static DustPattern heart() {
        var points = new ArrayList<Point>();
        for (int row = 0; row < HEART_MASK.length; row++) {
            for (int col = 0; col < HEART_MASK[row].length(); col++) {
                if (HEART_MASK[row].charAt(col) != '#') continue;
                // Same float expressions as the accepted Phase 02 HeartPrototype loop.
                points.add(new Point((col - 5) * HEART_SPACING, (4 - row) * HEART_SPACING, HEART_COLORS[row], 1));
            }
        }
        return new DustPattern(points);
    }
}
