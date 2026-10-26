package com.drawfirework.firework;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.CanvasDownsampler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/** Phase 04A.1: data-flow geometry facts (pure Java; in-game rendering is not covered). */
public class DustGeometryDiagnosticsTest {
    private static final float OFFSET = .14f; // DustStyle.BLOOM_130.localOffset (absolute, not spacing-relative)

    private static int visible(CanvasData c) {
        int n = 0;
        for (int p : c.pixels()) if ((p >>> 24) != 0) n++;
        return n;
    }

    private static Set<String> keys(List<DustPattern.Point> pts, boolean mirrorX, boolean mirrorY) {
        Set<String> s = new HashSet<>();
        for (var p : pts) s.add(String.format("%.4f,%.4f", (mirrorX ? -p.x() : p.x()) + 0.0, (mirrorY ? -p.y() : p.y()) + 0.0));
        return s;
    }

    @Test public void gridAndRectAreExactAndSymmetric() {
        for (CanvasData c : new CanvasData[]{DevCanvases.grid(), DevCanvases.rectangle()}) {
            var pts = DustPattern.fromCanvas(c, 3).points();
            assertEquals("every visible pixel sampled", visible(c), pts.size());
            assertEquals(keys(pts, false, false), keys(pts, true, false)); // left/right symmetric positions
            assertEquals(keys(pts, false, false), keys(pts, false, true)); // top/bottom symmetric positions
            var xs = new HashSet<String>();
            for (var p : pts) xs.add(String.format("%.4f", p.x()));
            // Straight columns: sample x values form a uniform lattice of exactly the canvas pitch.
            double pitch = DustPattern.CANVAS_SPAN / Math.max(c.width(), c.height());
            assertEquals(c.width(), xs.size());
            for (var p : pts) {
                double col = p.x() / pitch + c.width() / 2.0 - .5;
                assertEquals(col, Math.rint(col), 1e-4);
            }
        }
        assertEquals(225, visible(DevCanvases.grid()));
        assertEquals(144, visible(DevCanvases.rectangle()));
        // Grid centre mark sits at the exact origin; rectangle aspect is 3:2.
        assertTrue(DustPattern.fromCanvas(DevCanvases.grid(), 3).points().stream()
                .anyMatch(p -> p.x() == 0 && p.y() == 0 && p.rgb() == 0xff2020));
        var r = DustPattern.fromCanvas(DevCanvases.rectangle(), 3).points();
        double w = r.stream().mapToDouble(DustPattern.Point::x).max().getAsDouble() - r.stream().mapToDouble(DustPattern.Point::x).min().getAsDouble();
        double h = r.stream().mapToDouble(DustPattern.Point::y).max().getAsDouble() - r.stream().mapToDouble(DustPattern.Point::y).min().getAsDouble();
        assertEquals(23.0 / 15.0, w / h, 1e-3); // pixel-centre extents of a 24x16 (3:2) canvas
    }

    @Test public void smileyIsSampledExactlyOnPixelLattice() {
        // Confirmed: 16x16 smiley has fewer visible pixels than the 266 budget -> downsampler is lossless.
        CanvasData c = DevCanvases.smiley();
        var pts = DustPattern.fromCanvas(c, 3).points();
        assertEquals(visible(c), pts.size());
        assertTrue(pts.size() < 266);
        double pitch = DustPattern.CANVAS_SPAN / 16;
        for (var p : pts) {
            double col = p.x() / pitch + 7.5, row = 7.5 - p.y() / pitch;
            assertEquals(col, Math.rint(col), 1e-4);
            assertEquals(row, Math.rint(row), 1e-4);
        }
    }

    @Test public void layoutScalingKeepsCentreAspectColoursAndCount() {
        for (CanvasData c : new CanvasData[]{DevCanvases.rectangle(), DevCanvases.dense(), DevCanvases.smiley()}) {
            var base = DustPattern.fromCanvas(c, 3);
            for (float f : new float[]{1f, 1.25f, 1.5f}) {
                var s = base.scaled(f);
                assertEquals(base.points().size(), s.points().size());
                for (int i = 0; i < s.points().size(); i++) {
                    var a = base.points().get(i); var b = s.points().get(i);
                    assertEquals(a.x() * f, b.x(), 1e-6); assertEquals(a.y() * f, b.y(), 1e-6);
                    assertEquals(a.rgb(), b.rgb()); assertEquals(a.sizeScale(), b.sizeScale(), 0);
                }
            }
        }
        assertSame(DustPattern.HEART, DustPattern.HEART.scaled(1f));
        for (float bad : new float[]{.99f, 1.51f, Float.NaN, Float.POSITIVE_INFINITY, 0, -1})
            assertThrows(IllegalArgumentException.class, () -> DustPattern.HEART.scaled(bad));
        // Heart is untouched by the new diagnostics.
        assertEquals(63, DustPattern.HEART.points().size());
    }

    @Test public void sampleBudgetsStayWithin800Particles() {
        for (CanvasData c : new CanvasData[]{DevCanvases.dense(), DevCanvases.grid()})
            for (int b : new int[]{128, 192, 266}) {
                var p = DustPattern.fromCanvas(c, 3, b);
                assertTrue(p.points().size() <= b);
                assertTrue(p.coreParticles(3) <= 800);
            }
        assertEquals(128, DustPattern.fromCanvas(DevCanvases.dense(), 3, 128).points().size());
        assertEquals(192, DustPattern.fromCanvas(DevCanvases.dense(), 3, 192).points().size());
        assertEquals(266, DustPattern.fromCanvas(DevCanvases.dense(), 3, 266).points().size());
        // maxSamples can only lower, never raise, the 800-particle limit.
        assertEquals(266, DustPattern.fromCanvas(DevCanvases.dense(), 3, 100000).points().size());
        assertThrows(IllegalArgumentException.class, () -> DustPattern.fromCanvas(DevCanvases.dense(), 3, 0));
        assertEquals(DustPattern.fromCanvas(DevCanvases.dense(), 3), DustPattern.fromCanvas(DevCanvases.dense(), 3, 266));
    }

    @Test public void offsetToSpacingDiagnostic() {
        // The micro offset radius is absolute (0.14 block) while sample pitch shrinks with canvas size.
        double heart = OFFSET / DustPattern.HEART_SPACING;
        double smiley = OFFSET / (DustPattern.CANVAS_SPAN / 16);
        double grid = OFFSET / (DustPattern.CANVAS_SPAN / 25);
        double dense = OFFSET / (DustPattern.CANVAS_SPAN / 32);
        System.out.printf("offset/pitch: heart %.2f smiley %.2f grid %.2f dense32 %.2f%n", heart, smiley, grid, dense);
        assertTrue(heart < smiley && smiley < grid && grid < dense);
        assertTrue(dense > .85);
    }

    @Test public void denseDownsamplingIsIrregularAtLowBudget() {
        // 1024 visible pixels -> 266: adaptive cells, so pitch is not a uniform lattice (correct per Phase 03
        // contract, but visually "uneven"). Report nearest-neighbour spread; no algorithm change.
        for (int b : new int[]{128, 192, 266}) {
            var pts = CanvasDownsampler.sample(DevCanvases.dense(), b);
            double min = Double.MAX_VALUE, max = 0, sum = 0;
            for (var a : pts) {
                double nn = Double.MAX_VALUE;
                for (var o : pts) if (o != a) nn = Math.min(nn, Math.hypot(a.x() - o.x(), a.y() - o.y()));
                min = Math.min(min, nn); max = Math.max(max, nn); sum += nn;
            }
            System.out.printf("dense32 budget %d: nearest-neighbour pitch (x pixel) min %.2f mean %.2f max %.2f%n",
                    b, min * 32, sum / pts.size() * 32, max * 32);
            assertTrue(max > min); // irregular
        }
    }
}
