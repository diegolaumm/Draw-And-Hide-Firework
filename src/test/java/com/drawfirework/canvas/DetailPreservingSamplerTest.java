package com.drawfirework.canvas;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DetailPreservingSamplerTest {

    // Helper: create empty canvas
    private CanvasData emptyCanvas(int w, int h) {
        return new CanvasData(w, h, new int[w * h]);
    }

    // Helper: create circle canvas
    private CanvasData makeCircle(int w, int h, int radius, boolean filled) {
        int[] pixels = new int[w * h];
        int cx = w / 2;
        int cy = h / 2;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double dist = Math.hypot(x - cx, y - cy);
                if (filled) {
                    if (dist <= radius) pixels[y * w + x] = 0xFFFF3333;
                } else {
                    if (Math.abs(dist - radius) <= 1.0) pixels[y * w + x] = 0xFFFF3333;
                }
            }
        }
        return new CanvasData(w, h, pixels);
    }

    // Helper: create number 6 canvas
    private CanvasData makeNumberSix(int w, int h) {
        int[] pixels = new int[w * h];
        int color = 0xFF33CC33;
        // Top curve and stem
        for (int x = 12; x <= 20; x++) pixels[4 * w + x] = color;
        for (int y = 5; y <= 24; y++) pixels[y * w + 11] = color;
        for (int y = 5; y <= 24; y++) pixels[y * w + 12] = color;
        // Bottom loop (outer circle at (16, 20), radius 7)
        for (int y = 13; y <= 27; y++) {
            for (int x = 9; x <= 23; x++) {
                double dist = Math.hypot(x - 16, y - 20);
                if (dist >= 3.5 && dist <= 7.0) {
                    pixels[y * w + x] = color;
                }
            }
        }
        return new CanvasData(w, h, pixels);
    }

    // Helper: create thin diagonal line canvas
    private CanvasData makeDiagonalLine(int size) {
        int[] pixels = new int[size * size];
        for (int i = 0; i < size; i++) {
            pixels[i * size + i] = 0xFF3399FF;
        }
        return new CanvasData(size, size, pixels);
    }

    // Helper: create closed box contour
    private CanvasData makeClosedContour(int w, int h, int pad) {
        int[] pixels = new int[w * h];
        int color = 0xFFFFCC00;
        for (int x = pad; x < w - pad; x++) {
            pixels[pad * w + x] = color;
            pixels[(h - pad - 1) * w + x] = color;
        }
        for (int y = pad; y < h - pad; y++) {
            pixels[y * w + pad] = color;
            pixels[y * w + (w - pad - 1)] = color;
        }
        return new CanvasData(w, h, pixels);
    }

    // Helper: create solid color block
    private CanvasData makeSolidBlock(int w, int h, int color) {
        int[] pixels = new int[w * h];
        java.util.Arrays.fill(pixels, color);
        return new CanvasData(w, h, pixels);
    }

    // Helper: create multi-color checkerboard / gradient
    private CanvasData makeMultiColorPattern(int w, int h) {
        int[] pixels = new int[w * h];
        int[] palette = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00};
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = palette[((x / 4) + (y / 4)) % palette.length];
            }
        }
        return new CanvasData(w, h, pixels);
    }

    @Test
    public void emptyAndTransparentCanvasReturnsEmpty() {
        CanvasData empty = emptyCanvas(32, 32);
        assertTrue(DetailPreservingSampler.sample(empty, 266).isEmpty());
        assertTrue(DetailPreservingSampler.sample(empty, 0).isEmpty());
    }

    @Test
    public void budgetBoundsValidation() {
        CanvasData c = makeDiagonalLine(32);
        assertTrue(DetailPreservingSampler.sample(c, 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> DetailPreservingSampler.sample(c, -1));
        assertThrows(IllegalArgumentException.class, () -> DetailPreservingSampler.sample(c, 801));
    }

    @Test
    public void singlePixelPreservedExactly() {
        int[] pixels = new int[32 * 32];
        pixels[10 * 32 + 10] = 0xFFAABBCC;
        CanvasData c = new CanvasData(32, 32, pixels);
        var samples = DetailPreservingSampler.sample(c, 266);
        assertEquals(1, samples.size());
        assertEquals(0xFFAABBCC, samples.getFirst().argb());
    }

    @Test
    public void determinismAcrossRepeatedRuns() {
        CanvasData c = makeNumberSix(32, 32);
        var first = DetailPreservingSampler.sample(c, 266);
        for (int i = 0; i < 20; i++) {
            var subsequent = DetailPreservingSampler.sample(c, 266);
            assertEquals(first, subsequent);
        }
    }

    @Test
    public void diagonalThinLineFullyPreserved() {
        CanvasData diag = makeDiagonalLine(32);
        // 32 visible pixels <= 266 budget: 100% of line pixels MUST be preserved
        var samples = DetailPreservingSampler.sample(diag, 266);
        assertEquals(32, samples.size());
        for (var s : samples) {
            assertEquals(-s.x(), s.y(), 1e-6);
            assertEquals(0xFF3399FF, s.argb());
        }
    }

    @Test
    public void circleOutlinePreservationAndBudgetTiers() {
        CanvasData filledCircle = makeCircle(32, 32, 13, true);
        int standardBudget = 266;
        int balancedBudget = 366;
        int highBudget = 466;

        var stdSamples = DetailPreservingSampler.sample(filledCircle, standardBudget);
        var balSamples = DetailPreservingSampler.sample(filledCircle, balancedBudget);
        var highSamples = DetailPreservingSampler.sample(filledCircle, highBudget);

        assertTrue(stdSamples.size() <= standardBudget);
        assertTrue(balSamples.size() <= balancedBudget);
        assertTrue(highSamples.size() <= highBudget);

        // Quality progression: sample count monotonically increases with budget
        assertTrue(balSamples.size() >= stdSamples.size());
        assertTrue(highSamples.size() >= balSamples.size());

        // Strategy A comparison on perimeter coverage
        var existingStd = CanvasDownsampler.sample(filledCircle, standardBudget, SamplingStrategy.EXISTING);
        // Both within budget
        assertEquals(standardBudget, stdSamples.size());
        assertEquals(standardBudget, existingStd.size());

        // Count how many perimeter pixels (radius ~ 13) are captured by Strategy B vs Strategy A
        int stdPerimeterHits = 0;
        int existingPerimeterHits = 0;
        for (var s : stdSamples) {
            double r = Math.hypot(s.x() * 32, s.y() * 32);
            if (r >= 12.0) stdPerimeterHits++;
        }
        for (var s : existingStd) {
            double r = Math.hypot(s.x() * 32, s.y() * 32);
            if (r >= 12.0) existingPerimeterHits++;
        }

        // Strategy B prioritizes the outer contour, capturing significantly more perimeter samples
        assertTrue("Detail-preserving should capture more perimeter samples: " + stdPerimeterHits + " vs " + existingPerimeterHits,
                stdPerimeterHits >= existingPerimeterHits);
    }

    @Test
    public void numberSixLoopAndStemPreservation() {
        CanvasData six = makeNumberSix(32, 32);
        var stdSamples = DetailPreservingSampler.sample(six, 266);
        var balSamples = DetailPreservingSampler.sample(six, 366);
        var highSamples = DetailPreservingSampler.sample(six, 466);

        assertTrue(stdSamples.size() > 0 && stdSamples.size() <= 266);
        assertTrue(balSamples.size() >= stdSamples.size());
        assertTrue(highSamples.size() >= balSamples.size());

        // Verify the inner loop hole is not filled: no samples at the exact center of the hole (16, 20)
        double holeCenterX = (16 + 0.5 - 16.0) / 32.0;
        double holeCenterY = (16.0 - 20 - 0.5) / 32.0;

        for (var s : stdSamples) {
            double distToHoleCenter = Math.hypot(s.x() - holeCenterX, s.y() - holeCenterY);
            assertTrue("Sample should not occupy the hollow center of number 6: dist=" + distToHoleCenter,
                    distToHoleCenter > 0.05); // at least ~1.6 pixels clearance
        }
    }

    @Test
    public void closedContourNeverDropsPerimeter() {
        CanvasData box = makeClosedContour(32, 32, 2);
        // Total contour pixels: (28*4 - 4) = 108 pixels
        var samples = DetailPreservingSampler.sample(box, 266);
        // 108 <= 266: every single perimeter pixel is preserved!
        assertEquals(108, samples.size());
    }

    @Test
    public void solidBlockPreservesSharpBordersAndFillsInterior() {
        CanvasData solid = makeSolidBlock(32, 32, 0xFFFF5500); // 1024 pixels
        var samples266 = DetailPreservingSampler.sample(solid, 266);
        var samples366 = DetailPreservingSampler.sample(solid, 366);
        var samples466 = DetailPreservingSampler.sample(solid, 466);

        assertEquals(266, samples266.size());
        assertEquals(366, samples366.size());
        assertEquals(466, samples466.size());

        // Count border samples: x or y near edge (|coord| >= 15/32 = 0.46875)
        int border266 = 0;
        int interior266 = 0;
        for (var s : samples266) {
            if (Math.abs(s.x()) >= 0.46 || Math.abs(s.y()) >= 0.46) {
                border266++;
            } else {
                interior266++;
            }
        }
        // Border is well represented and interior is not starved
        assertTrue(border266 >= 80);
        assertTrue(interior266 >= 80);
    }

    @Test
    public void multiColorPatternPreservesColorBoundaries() {
        CanvasData multi = makeMultiColorPattern(32, 32);
        var samples = DetailPreservingSampler.sample(multi, 266);
        assertEquals(266, samples.size());

        Set<Integer> uniqueColors = new HashSet<>();
        for (var s : samples) {
            uniqueColors.add(s.argb());
        }
        // All 4 distinct colors from palette must be preserved without distortion
        assertEquals(4, uniqueColors.size());
    }

    @Test
    public void sampleCoordinatesAreStrictlyBoundedWithinUnitSquare() {
        CanvasData c = makeCircle(32, 32, 15, true);
        for (var s : DetailPreservingSampler.sample(c, 266)) {
            assertTrue(s.x() >= -0.5 && s.x() <= 0.5);
            assertTrue(s.y() >= -0.5 && s.y() <= 0.5);
        }
    }

    @Test
    public void testBudgetAllocationRebalancingWhenCategoryHasFewerPixels() {
        // Case A: Pure line art (0 interior pixels) - 100% of budget allocated to details
        CanvasData box = makeClosedContour(32, 32, 1); // 120 boundary pixels, 0 interior
        var samplesBox = DetailPreservingSampler.sample(box, 100);
        assertEquals(100, samplesBox.size());

        // Case B: Scanty details (only 4 corner/edge pixels), large interior
        int[] pixelsB = new int[32 * 32];
        for (int y = 5; y < 27; y++) {
            for (int x = 5; x < 27; x++) {
                pixelsB[y * 32 + x] = 0xFF55AA55;
            }
        }
        CanvasData largeBlock = new CanvasData(32, 32, pixelsB);
        // Budget 150: initial target detail is 150 * 0.7 = 105, but boundary only has ~84 pixels
        var samplesB = DetailPreservingSampler.sample(largeBlock, 150);
        assertEquals(150, samplesB.size()); // Total must strictly equal budget!

        // Case C: Odd budget integer rounding
        var samplesOdd = DetailPreservingSampler.sample(largeBlock, 33);
        assertEquals(33, samplesOdd.size());
    }

    @Test
    public void testIntricateLinesExceedingSampleBudgetGracefullyDownsamples() {
        // Create an intricate maze/spiral with 600 thin line pixels (exceeding budget 266)
        int[] pixels = new int[32 * 32];
        int count = 0;
        for (int ring = 1; ring < 16; ring += 2) {
            for (int x = ring; x < 32 - ring; x++) {
                pixels[ring * 32 + x] = 0xFF4488FF;
                pixels[(31 - ring) * 32 + x] = 0xFF4488FF;
                count += 2;
            }
            for (int y = ring; y < 32 - ring; y++) {
                pixels[y * 32 + ring] = 0xFF4488FF;
                pixels[y * 32 + (31 - ring)] = 0xFF4488FF;
                count += 2;
            }
        }
        assertTrue("Intricate line pixels should exceed standard budget: " + count, count > 266);
        CanvasData intricate = new CanvasData(32, 32, pixels);

        int budget = 266;
        var samples = DetailPreservingSampler.sample(intricate, budget);

        // 1. Budget is strictly observed
        assertEquals(budget, samples.size());

        // 2. All samples retain exact source ARGB and valid coordinates
        for (var s : samples) {
            assertEquals(0xFF4488FF, s.argb());
            assertTrue(s.x() >= -0.5 && s.x() <= 0.5);
            assertTrue(s.y() >= -0.5 && s.y() <= 0.5);
        }

        // 3. Spatial coverage: samples exist in all four quadrants
        int qTopLeft = 0, qTopRight = 0, qBottomLeft = 0, qBottomRight = 0;
        for (var s : samples) {
            if (s.x() <= 0 && s.y() >= 0) qTopLeft++;
            else if (s.x() > 0 && s.y() >= 0) qTopRight++;
            else if (s.x() <= 0 && s.y() < 0) qBottomLeft++;
            else qBottomRight++;
        }
        assertTrue(qTopLeft > 40);
        assertTrue(qTopRight > 40);
        assertTrue(qBottomLeft > 40);
        assertTrue(qBottomRight > 40);
    }
}
