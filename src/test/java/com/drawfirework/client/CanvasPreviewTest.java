package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import com.drawfirework.firework.DustPattern;
import org.junit.Test;
import static org.junit.Assert.*;

public class CanvasPreviewTest {
    @Test public void previewAndSaveShareIdenticalCanvasData() {
        var canvas = new FreehandCanvas(null);
        canvas.paint(10, 10, 0xFFFF0000);
        canvas.endStroke();

        var preview = new CanvasPreview();
        var outSoft = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertEquals(canvas.snapshot(FreehandResampler.Mode.SOFT), outSoft.canvas());

        var outCrisp = preview.get(canvas, FreehandResampler.Mode.CRISP);
        assertEquals(canvas.snapshot(FreehandResampler.Mode.CRISP), outCrisp.canvas());
    }

    @Test public void threeParticleSampleDistributionMatchesBloom130() {
        var canvas = new FreehandCanvas(null);
        canvas.setDiameter(16);
        canvas.paint(128, 128, 0xFFFF0000);
        canvas.endStroke();

        var preview = new CanvasPreview();
        var out = preview.get(canvas, FreehandResampler.Mode.SOFT);
        var preset = DustStyle.BLOOM_130;
        int copies = preset.particlesPerPoint();
        assertEquals(3, copies);

        var points = out.pattern().points();
        assertTrue(points.size() > 0);
        int totalParticles = points.size() * copies;
        assertTrue(totalParticles <= 800);

        // Verify each point produces 3 deterministic bounded micro-offset samples
        for (int i = 0; i < points.size(); i++) {
            var pt = points.get(i);
            for (int k = 0; k < copies; k++) {
                var s = preset.sample(i, k, pt.x(), pt.y());
                double dist = Math.hypot(s.x() - pt.x(), s.y() - pt.y());
                assertTrue(dist <= preset.localOffset() + 1e-5);
            }
        }
    }

    @Test public void rectangularCanvasPreservesAspectRatioInPreview() {
        // 32 x 16 rectangular canvas (width is double the height)
        int[] pixels = new int[32 * 16];
        java.util.Arrays.fill(pixels, 0xFFFF0000);
        var rectData = new CanvasData(32, 16, pixels);
        var canvas = new FreehandCanvas(rectData);

        var preview = new CanvasPreview();
        var out = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertEquals(32, out.canvas().width());
        assertEquals(16, out.canvas().height());

        // Longest side is normalized to DustPattern.CANVAS_SPAN
        float minX = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (var pt : out.pattern().points()) {
            if (pt.x() < minX) minX = pt.x();
            if (pt.x() > maxX) maxX = pt.x();
            if (pt.y() < minY) minY = pt.y();
            if (pt.y() > maxY) maxY = pt.y();
        }

        float spanX = maxX - minX;
        float spanY = maxY - minY;
        // 32 vs 16: spanX should be approximately double spanY
        assertTrue(spanX > spanY * 1.8f);
    }

    @Test public void cacheInvalidationOnModeAndRevisionChange() {
        var canvas = new FreehandCanvas(null);
        canvas.paint(10, 10, 0xFFFF0000); canvas.endStroke();

        var preview = new CanvasPreview();
        var out1 = preview.get(canvas, FreehandResampler.Mode.SOFT);
        var out2 = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertSame(out1, out2); // cached!

        var outCrisp = preview.get(canvas, FreehandResampler.Mode.CRISP);
        assertNotSame(out1, outCrisp); // mode changed!

        canvas.paint(50, 50, 0xFF00FF00); canvas.endStroke();
        var out3 = preview.get(canvas, FreehandResampler.Mode.CRISP);
        assertNotSame(outCrisp, out3); // revision changed!
    }

    @Test public void cacheInvalidationOnQualityAndStrategyChange() {
        var canvas = new FreehandCanvas(null);
        canvas.setDiameter(16);
        canvas.paint(128, 128, 0xFFFF0000);
        canvas.endStroke();

        HeartPrototype.setActiveQuality(com.drawfirework.canvas.ParticleQuality.STANDARD);
        HeartPrototype.setActiveSamplingStrategy(com.drawfirework.canvas.SamplingStrategy.DETAIL_PRESERVING);

        var preview = new CanvasPreview();
        var outStandard = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertTrue(outStandard.pattern().points().size() <= 266);

        // Change quality to BALANCED: cache should invalidate and reflect higher budget
        HeartPrototype.setActiveQuality(com.drawfirework.canvas.ParticleQuality.BALANCED);
        var outBalanced = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertNotSame(outStandard, outBalanced);

        // Change quality to HIGH
        HeartPrototype.setActiveQuality(com.drawfirework.canvas.ParticleQuality.HIGH);
        var outHigh = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertNotSame(outBalanced, outHigh);

        // Change strategy to EXISTING
        HeartPrototype.setActiveSamplingStrategy(com.drawfirework.canvas.SamplingStrategy.EXISTING);
        var outExisting = preview.get(canvas, FreehandResampler.Mode.SOFT);
        assertNotSame(outHigh, outExisting);

        // Reset back to defaults
        HeartPrototype.setActiveQuality(com.drawfirework.canvas.ParticleQuality.DEFAULT);
        HeartPrototype.setActiveSamplingStrategy(com.drawfirework.canvas.SamplingStrategy.DEFAULT);
    }
}
