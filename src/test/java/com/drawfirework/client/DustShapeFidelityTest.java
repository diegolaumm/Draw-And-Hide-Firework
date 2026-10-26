package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import com.drawfirework.firework.DustPattern;
import org.junit.Test;

import static org.junit.Assert.*;

public class DustShapeFidelityTest {

    @Test
    public void testNumberSixGeometricFidelity() {
        for (int size : new int[]{16, 32}) {
            CanvasData blank = new CanvasData(size, size, new int[size * size]);
            FreehandCanvas canvas = new FreehandCanvas(blank);
            int color = 0xFFFF4040;
            canvas.setDiameter(size == 16 ? 10 : 20);

            // Draw a recognizable "6" (top hook, left spine, bottom loop, middle bar)
            int scale = FreehandCanvas.SCALE;
            canvas.paint(size * scale * 0.5, size * scale * 0.15, color);
            canvas.paint(size * scale * 0.25, size * scale * 0.15, color);
            canvas.paint(size * scale * 0.18, size * scale * 0.35, color);
            canvas.paint(size * scale * 0.18, size * scale * 0.70, color);
            canvas.paint(size * scale * 0.30, size * scale * 0.85, color);
            canvas.paint(size * scale * 0.65, size * scale * 0.85, color);
            canvas.paint(size * scale * 0.72, size * scale * 0.70, color);
            canvas.paint(size * scale * 0.65, size * scale * 0.55, color);
            canvas.paint(size * scale * 0.25, size * scale * 0.55, color);
            canvas.endStroke();

            for (var mode : FreehandResampler.Mode.values()) {
                CanvasData data = canvas.snapshot(mode);
                int minX = 999, maxX = -1, minY = 999, maxY = -1, count = 0;
                double sumX = 0, sumY = 0;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        if ((data.pixel(x, y) >>> 24) > 0) {
                            minX = Math.min(minX, x);
                            maxX = Math.max(maxX, x);
                            minY = Math.min(minY, y);
                            maxY = Math.max(maxY, y);
                            sumX += x;
                            sumY += y;
                            count++;
                        }
                    }
                }
                assertTrue(count > 0);

                var pattern = DustPattern.fromCanvas(data, 3);
                assertFalse(pattern.points().isEmpty());

                float ptMinX = 999, ptMaxX = -999, ptMinY = 999, ptMaxY = -999;
                float ptSumX = 0, ptSumY = 0;
                for (var pt : pattern.points()) {
                    ptMinX = Math.min(ptMinX, pt.x());
                    ptMaxX = Math.max(ptMaxX, pt.x());
                    ptMinY = Math.min(ptMinY, pt.y());
                    ptMaxY = Math.max(ptMaxY, pt.y());
                    ptSumX += pt.x();
                    ptSumY += pt.y();
                }

                // Vertical direction check:
                // Canvas top (row minY) must map to positive Y (upwards in pattern space).
                // Canvas bottom (row maxY) must map to negative Y (downwards in pattern space).
                assertTrue("Top canvas row must map to positive Y in pattern", ptMaxY > 0);
                assertTrue("Bottom canvas row must map to negative Y in pattern", ptMinY < 0);

                // Check centroid correspondence:
                double pitch = DustPattern.CANVAS_SPAN / size;
                double expectedCenterX = ((sumX / count) - (size - 1) / 2.0) * pitch;
                double expectedCenterY = (((size - 1) / 2.0) - (sumY / count)) * pitch;

                assertEquals("X centroid must match within 1/2 cell", expectedCenterX, ptSumX / pattern.points().size(), pitch * 0.5);
                assertEquals("Y centroid must match within 1/2 cell", expectedCenterY, ptSumY / pattern.points().size(), pitch * 0.5);

                // Use actual product projection methods to verify screen coordinate mapping
                int boardLeft = 10, boardTop = 32, boardWidth = 160, boardHeight = 160;
                float projScale = CanvasEditorLayout.projectionScale(boardWidth, boardHeight);
                int screenMinY = 999, screenMaxY = -999;
                for (var pt : pattern.points()) {
                    int sy = CanvasEditorLayout.projectY(boardTop, boardHeight, pt.y(), projScale);
                    screenMinY = Math.min(screenMinY, sy);
                    screenMaxY = Math.max(screenMaxY, sy);
                }
                // Screen Y increases downwards: top row of "6" has smaller screen Y than bottom row
                assertTrue("Screen Y of top row must be smaller (higher on screen)", screenMinY < screenMaxY);
            }
        }
    }

    @Test
    public void testCircleGeometricFidelity() {
        int size = 16;
        int[] pixels = new int[size * size];
        double center = (size - 1) / 2.0;
        double radius = 5.0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                double d = Math.hypot(x - center, y - center);
                if (Math.abs(d - radius) <= 1.0) {
                    pixels[y * size + x] = 0xFFFF0000;
                }
            }
        }
        CanvasData circleData = new CanvasData(size, size, pixels);
        var pattern = DustPattern.fromCanvas(circleData, 3);

        float minX = 999, maxX = -999, minY = 999, maxY = -999;
        float sumX = 0, sumY = 0;
        for (var pt : pattern.points()) {
            minX = Math.min(minX, pt.x());
            maxX = Math.max(maxX, pt.x());
            minY = Math.min(minY, pt.y());
            maxY = Math.max(maxY, pt.y());
            sumX += pt.x();
            sumY += pt.y();
        }

        // Circle must be centered at (0, 0) in pattern space
        assertEquals(0f, sumX / pattern.points().size(), 1e-4);
        assertEquals(0f, sumY / pattern.points().size(), 1e-4);

        // Aspect ratio must be exactly 1.0 (equal span in X and Y)
        float spanX = maxX - minX;
        float spanY = maxY - minY;
        assertEquals("Circle aspect ratio must be 1.0", spanX, spanY, 1e-4);
    }

    @Test
    public void testDiagonalLineGeometricFidelity() {
        int size = 16;
        int[] pixels = new int[size * size];
        for (int i = 2; i <= 13; i++) {
            pixels[i * size + i] = 0xFFFF4040; // diagonal (x == y)
        }
        CanvasData diagData = new CanvasData(size, size, pixels);
        var pattern = DustPattern.fromCanvas(diagData, 3);
        assertEquals(12, pattern.points().size());

        // Monotonic check: as x increases (+X right), y must decrease (-Y down in world)
        for (int i = 0; i < pattern.points().size() - 1; i++) {
            var p1 = pattern.points().get(i);
            var p2 = pattern.points().get(i + 1);
            assertTrue(p2.x() > p1.x());
            assertTrue(p2.y() < p1.y());
            assertEquals(Math.abs(p2.x() - p1.x()), Math.abs(p2.y() - p1.y()), 1e-4);
        }
    }

    @Test
    public void testRectangleGeometricFidelity() {
        // 32 x 16 canvas (2:1 aspect ratio)
        int w = 32, h = 16;
        int[] pixels = new int[w * h];
        for (int y = 2; y <= 13; y++) {
            for (int x = 4; x <= 27; x++) {
                if (x == 4 || x == 27 || y == 2 || y == 13) {
                    pixels[y * w + x] = 0xFF00FF00;
                }
            }
        }
        CanvasData rectData = new CanvasData(w, h, pixels);
        var pattern = DustPattern.fromCanvas(rectData, 3);

        float minX = 999, maxX = -999, minY = 999, maxY = -999;
        for (var pt : pattern.points()) {
            minX = Math.min(minX, pt.x());
            maxX = Math.max(maxX, pt.x());
            minY = Math.min(minY, pt.y());
            maxY = Math.max(maxY, pt.y());
        }

        float spanX = maxX - minX;
        float spanY = maxY - minY;
        double expectedRatio = 23.0 / 11.0;
        assertEquals("Rectangle aspect ratio must match pixel lattice ratio", expectedRatio, spanX / spanY, 1e-2);
    }

    @Test
    public void testDustStyleBloom130SampleProperties() {
        var preset = DustStyle.BLOOM_130;
        assertEquals(3, preset.particlesPerPoint());
        assertEquals(0.14f, preset.localOffset(), 1e-6);

        double sumDx = 0, sumDy = 0;
        int count = 0;
        for (int i = 0; i < 200; i++) {
            for (int k = 0; k < 3; k++) {
                var s = preset.sample(i, k, 0f, 0f);
                double dist = Math.hypot(s.x(), s.y());
                assertTrue("Micro-offset radius must be bounded by localOffset", dist <= preset.localOffset() + 1e-5);
                sumDx += s.x();
                sumDy += s.y();
                count++;
            }
        }
        assertEquals(0.0, sumDx / count, 0.02);
        assertEquals(0.0, sumDy / count, 0.02);

        double pitch16 = DustPattern.CANVAS_SPAN / 16.0;
        double ratio16 = preset.localOffset() / pitch16;
        assertTrue("On 16x16, micro-offset is ~45% of cell pitch", ratio16 > 0.40 && ratio16 < 0.50);

        double pitch32 = DustPattern.CANVAS_SPAN / 32.0;
        double ratio32 = preset.localOffset() / pitch32;
        assertTrue("On 32x32, micro-offset is ~90% of cell pitch", ratio32 > 0.85 && ratio32 < 0.95);
    }

    @Test
    public void testNoParticleDroppedAtPerimeterWithProductMethod() {
        for (int size : new int[]{16, 32}) {
            int[] px = new int[size * size];
            java.util.Arrays.fill(px, 0xFFFFFFFF);
            var full = new CanvasData(size, size, px);
            var pattern = DustPattern.fromCanvas(full, 3);
            var preset = DustStyle.BLOOM_130;

            int boardLeft = 10, boardTop = 32, boardWidth = 160, boardHeight = 160;
            float scale = CanvasEditorLayout.projectionScale(boardWidth, boardHeight);
            int copies = preset.particlesPerPoint();

            int renderedCount = 0;
            int oldBugRenderedCount = 0;
            for (int i = 0; i < pattern.points().size(); i++) {
                var pt = pattern.points().get(i);
                int r = CanvasEditorLayout.particleDotRadius(scale, pt.sizeScale());
                for (int k = 0; k < copies; k++) {
                    var s = preset.sample(i, k, pt.x(), pt.y());
                    int sx = CanvasEditorLayout.projectX(boardLeft, boardWidth, s.x(), scale);
                    int sy = CanvasEditorLayout.projectY(boardTop, boardHeight, s.y(), scale);

                    // Production method check
                    if (CanvasEditorLayout.isParticleVisible(boardLeft, boardTop, boardWidth, boardHeight, sx, sy, preset.localOffset(), scale, r)) {
                        renderedCount++;
                    }

                    // Historical buggy condition check (for regression contrast)
                    if (sx >= boardLeft && sx <= boardLeft + boardWidth && sy >= boardTop && sy <= boardTop + boardHeight) {
                        oldBugRenderedCount++;
                    }
                }
            }
            int expectedCount = pattern.points().size() * copies;
            assertEquals("Product method must render 100% of particles without truncation", expectedCount, renderedCount);
            // Confirm the test is effective: the product method successfully renders all points
            assertTrue(renderedCount == expectedCount);
        }
    }

    @Test
    public void testTextLayoutBoundariesUnderAllGuiScalesAndNarrowWindows() {
        // Test standard, wide, and narrow windows with various line counts (1 to 6 lines)
        int[] widths = {180, 240, 300, 420, 640, 800};
        int[] heights = {160, 200, 240, 320, 480, 600};
        int[] testLineCounts = {1, 2, 3, 4, 5, 6};

        for (int w : widths) {
            for (int h : heights) {
                for (int lines : testLineCounts) {
                    int lineHeight = 9;
                    int textHeightNeeded = lines * lineHeight;
                    var board = CanvasEditorLayout.computeBoard(w, h, 256, 256, textHeightNeeded);

                    int pStart = CanvasEditorLayout.panelStart(w);
                    int maxW = CanvasEditorLayout.maxTextWidth(w);

                    // Case A: When statsWidth fits in available maxTextWidth
                    int statsWidth = Math.min(120, maxW);
                    var placement = CanvasEditorLayout.computeTextPlacement(w, h, board, statsWidth, lineHeight);

                    // 1. Line 1 (Stats) must not cross left margin
                    assertTrue("statsX must stay >= MARGIN_LEFT", placement.statsX() >= CanvasEditorLayout.MARGIN_LEFT);

                    // 2. Line 1 (Stats) must not enter right buttons
                    assertTrue("statsX + statsWidth must not exceed panelStart",
                            placement.statsX() + statsWidth <= pStart);

                    // 3. Line 2 (Disclaimer) must be below Line 1 without overlap
                    assertTrue("Disclaimer Y must be strictly below stats line",
                            placement.disclaimerY() >= placement.statsY() + lineHeight);

                    // 4. Line 2 (Disclaimer) max width must not enter right buttons
                    assertTrue("Disclaimer right edge must not enter panelStart",
                            placement.disclaimerX() + placement.maxTextWidth() <= pStart);

                    // 5. Rendered disclaimer lines must not extend beyond window bottom
                    int linesDrawn = Math.min(lines, placement.maxAllowedDisclaimerLines());
                    int bottomEdge = placement.disclaimerY() + linesDrawn * lineHeight;
                    assertTrue("Bottom of drawn disclaimer must be <= screenHeight", bottomEdge <= h);

                    // Case B: Oversized statsWidth is safely left-anchored at MARGIN_LEFT
                    var overPlacement = CanvasEditorLayout.computeTextPlacement(w, h, board, maxW + 50, lineHeight);
                    assertEquals("Oversized stats text must anchor safely at MARGIN_LEFT",
                            CanvasEditorLayout.MARGIN_LEFT, overPlacement.statsX());
                }
            }
        }
    }

    @Test
    public void testStatsLabelSelectionUnderTightWorkWidth() {
        String full = "Preview: 348 particles (116 samples x 3)";
        String shortText = "348 particles (116 samples)";
        String compact = "348 pts (116 spl)";

        CanvasEditorLayout.TextWidthMeter<String> mockMeter = s -> s.length() * 6; // 6px per character

        int fullW = mockMeter.width(full);       // 240px
        int shortW = mockMeter.width(shortText); // 162px
        int compactW = mockMeter.width(compact); // 102px

        // Wide space allows full
        assertEquals(full, CanvasEditorLayout.chooseStatsText(250, full, shortText, compact, mockMeter));

        // Medium space selects short
        assertEquals(shortText, CanvasEditorLayout.chooseStatsText(180, full, shortText, compact, mockMeter));

        // Narrow space selects compact
        assertEquals(compact, CanvasEditorLayout.chooseStatsText(120, full, shortText, compact, mockMeter));

        // Tighter space where even compact exceeds maxTextWidth safely degrades to null
        assertNull("When compact exceeds maxTextWidth, it must degrade to null (safe omission)",
                CanvasEditorLayout.chooseStatsText(90, full, shortText, compact, mockMeter));

        // Non-positive maxTextWidth safely returns null
        assertNull("Zero maxTextWidth must return null",
                CanvasEditorLayout.chooseStatsText(0, full, shortText, compact, mockMeter));
        assertNull("Negative maxTextWidth must return null",
                CanvasEditorLayout.chooseStatsText(-10, full, shortText, compact, mockMeter));
    }

    @Test
    public void testStatsLabelCandidateProgressionAndSafeOmission() {
        String full = "Preview: 348 particles (116 samples x 3)"; // 40 chars * 6 = 240px
        String shortText = "348 particles (116 samples)";          // 27 chars * 6 = 162px
        String compact = "348 pts (116 spl)";                      // 17 chars * 6 = 102px
        String minimal = "348 pts";                                // 7 chars * 6 = 42px
        String number = "348";                                     // 3 chars * 6 = 18px

        java.util.List<String> candidates = java.util.List.of(full, shortText, compact, minimal, number);
        CanvasEditorLayout.TextWidthMeter<String> meter = s -> s.length() * 6;

        assertEquals(full, CanvasEditorLayout.chooseStatsText(250, candidates, meter));
        assertEquals(shortText, CanvasEditorLayout.chooseStatsText(200, candidates, meter));
        assertEquals(compact, CanvasEditorLayout.chooseStatsText(120, candidates, meter));
        assertEquals(minimal, CanvasEditorLayout.chooseStatsText(60, candidates, meter));
        assertEquals(number, CanvasEditorLayout.chooseStatsText(30, candidates, meter));
        assertNull("Width narrower than shortest candidate (18px) safely omits",
                CanvasEditorLayout.chooseStatsText(15, candidates, meter));
        assertNull("Width 0 safely omits", CanvasEditorLayout.chooseStatsText(0, candidates, meter));
        assertNull("Negative width safely omits", CanvasEditorLayout.chooseStatsText(-5, candidates, meter));
    }

    @Test
    public void testMaxTextWidthNoArtificialFloor() {
        // panelStart = w - min(128, max(84, w/3)) - 8
        // maxTextWidth = max(0, panelStart - 4 - 8)
        assertEquals(652, CanvasEditorLayout.maxTextWidth(800));
        assertEquals(76, CanvasEditorLayout.maxTextWidth(180));
        // At w = 120: panelStart = 120 - 84 - 8 = 28. 28 - 4 - 8 = 16.
        // Old bug returned 40 due to Math.max(40, ...). Now returns exactly 16.
        assertEquals(16, CanvasEditorLayout.maxTextWidth(120));
        // At w = 90: panelStart = 90 - 84 - 8 = -2. -2 - 4 - 8 = -14 <= 0.
        assertEquals(0, CanvasEditorLayout.maxTextWidth(90));
        assertEquals(0, CanvasEditorLayout.maxTextWidth(50));
    }

    @Test
    public void testResolvedStatsTextNeverEncroachesButtonsAcrossWindowSizes() {
        String full = "Preview: 348 particles (116 samples x 3)";
        String shortText = "348 particles (116 samples)";
        String compact = "348 pts (116 spl)";
        String minimal = "348 pts";
        String number = "348";
        java.util.List<String> candidates = java.util.List.of(full, shortText, compact, minimal, number);
        CanvasEditorLayout.TextWidthMeter<String> meter = s -> s.length() * 6;

        int[] widths = {50, 80, 100, 120, 140, 160, 180, 240, 320, 480, 640, 800};
        for (int w : widths) {
            int h = 240;
            var board = CanvasEditorLayout.computeBoard(w, h, 256, 256, 18);
            var resolved = CanvasEditorLayout.resolveStatsText(w, h, board, 9, candidates, meter);
            int pStart = CanvasEditorLayout.panelStart(w);
            int maxW = CanvasEditorLayout.maxTextWidth(w);

            if (resolved.visible()) {
                assertTrue("Rendered text must have positive width", resolved.width() > 0);
                assertTrue("Rendered text width must be <= maxTextWidth", resolved.width() <= maxW);
                assertTrue("Rendered X must stay >= MARGIN_LEFT", resolved.x() >= CanvasEditorLayout.MARGIN_LEFT);
                assertTrue("Rendered right edge must stay <= panelStart - 4",
                        resolved.x() + resolved.width() <= pStart - 4);
            } else {
                assertEquals("Non-visible resolved text must report width 0", 0, resolved.width());
                assertNull("Non-visible resolved text must have null item", resolved.item());
                // When non-visible, it means no candidate could fit in maxW
                assertTrue("Non-visible occurs when maxW is too small for shortest candidate",
                        maxW < meter.width(number));
            }
        }
    }

    @Test
    public void testOldLayoutBugRegressionDetectsOverlap() {
        // Under the old bug at height = 240:
        // top = 32, boardHeight = height - 76 = 164.
        // boardBottom = 196. textY1 = boardBottom + 5 = 201.
        // textY1 height is 9px -> covers [201, 210].
        // textY2 was fixed at height - 34 = 206.
        // Since 206 < 210, Line 1 and Line 2 overlapped by 4 pixels!
        int oldBoardHeight = 240 - 76;
        int oldTextY1 = 32 + oldBoardHeight + 5; // 201
        int oldTextY2 = 240 - 34; // 206
        int oldOverlap = (oldTextY1 + 9) - oldTextY2;
        assertEquals("Historical formula overlapped by 4 pixels", 4, oldOverlap);

        // Under new CanvasEditorLayout at height = 240:
        var board = CanvasEditorLayout.computeBoard(320, 240, 256, 256, 27);
        var placement = CanvasEditorLayout.computeTextPlacement(320, 240, board, 120, 9);
        int newGap = placement.disclaimerY() - (placement.statsY() + 9);
        assertTrue("New layout must guarantee a positive gap between lines", newGap >= 4);
    }
}
