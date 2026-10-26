package com.drawfirework.editor;

import org.junit.Test;
import static org.junit.Assert.*;

public class SmartResamplerTest {
    @Test public void thinContoursGainCoverageButNotOpaquePixels() {
        var c = new FreehandCanvas(null);
        c.setDiameter(1);
        c.paint(8.5, 128.5, 0xFFFF0000);
        c.paint(247.5, 128.5, 0xFFFF0000);
        var soft = FreehandResampler.convert(c);
        var crisp = FreehandResampler.convert(c, FreehandResampler.Mode.CRISP);
        for (int x = 2; x < 30; x++) {
            int a = soft.pixel(x, 16) >>> 24, b = crisp.pixel(x, 16) >>> 24;
            assertTrue(b > a);
            assertTrue(b <= 2 * a);
            assertTrue(b < 255);
            assertEquals(soft.pixel(x, 16) & 0xffffff, crisp.pixel(x, 16) & 0xffffff);
        }
        assertEquals(crisp, FreehandResampler.convert(c, FreehandResampler.Mode.CRISP));
    }

    @Test public void isolatedNoiseAndTransparentPixelsNotPromoted() {
        var c = new FreehandCanvas(null);
        c.setDiameter(1);
        c.paint(12.5, 12.5, 0xFFFF0000);
        assertEquals(FreehandResampler.convert(c), FreehandResampler.convert(c, FreehandResampler.Mode.CRISP));
        var blank = new FreehandCanvas(null);
        assertEquals(blank.snapshot(), FreehandResampler.convert(blank, FreehandResampler.Mode.CRISP));
    }

    @Test public void disconnectedScatterSpanningFourRowsNotBoosted() {
        // Construct disconnected dots spanning rows 0, 2, 4, 6 within the upper-left 8x8 block (0,0)
        // Bitmask has bits at (0,0), (2,2), (4,4), (6,6)
        long disconnectedMask = (1L << (0 * 8 + 0)) | (1L << (2 * 8 + 2)) | (1L << (4 * 8 + 4)) | (1L << (6 * 8 + 6));
        assertFalse(FreehandResampler.hasContinuousContour(disconnectedMask));

        // Connected diagonal (0,0), (1,1), (2,2), (3,3)
        long connectedDiagonal = (1L << (0 * 8 + 0)) | (1L << (1 * 8 + 1)) | (1L << (2 * 8 + 2)) | (1L << (3 * 8 + 3));
        assertTrue(FreehandResampler.hasContinuousContour(connectedDiagonal));

        // Connected horizontal line
        long connectedHorizontal = (1L << (3 * 8 + 1)) | (1L << (3 * 8 + 2)) | (1L << (3 * 8 + 3)) | (1L << (3 * 8 + 4));
        assertTrue(FreehandResampler.hasContinuousContour(connectedHorizontal));

        // Connected vertical line
        long connectedVertical = (1L << (1 * 8 + 5)) | (1L << (2 * 8 + 5)) | (1L << (3 * 8 + 5)) | (1L << (4 * 8 + 5));
        assertTrue(FreehandResampler.hasContinuousContour(connectedVertical));
    }

    @Test public void continuousDiagonalStrokesGainCoverage() {
        var c = new FreehandCanvas(null);
        c.setDiameter(2);
        // Paint a diagonal line across the canvas
        c.paint(16, 16, 0xFFFF0000);
        c.paint(240, 240, 0xFFFF0000);
        var soft = FreehandResampler.convert(c);
        var crisp = FreehandResampler.convert(c, FreehandResampler.Mode.CRISP);
        for (int i = 3; i < 29; i++) {
            int a = soft.pixel(i, i) >>> 24, b = crisp.pixel(i, i) >>> 24;
            assertTrue(b >= a);
            assertEquals(soft.pixel(i, i) & 0xffffff, crisp.pixel(i, i) & 0xffffff);
        }
    }

    @Test public void modesShareHueOnCurvesDiagonalsAndColorBoundaries() {
        var c = new FreehandCanvas(null);
        c.setDiameter(3);
        for (int i = 0; i <= 360; i++) {
            double t = Math.toRadians(i);
            c.paint(128 + 80 * Math.cos(t), 128 + 80 * Math.sin(t), 0xFFFF0000);
        }
        c.endStroke();
        c.paint(10, 10, 0xFF0000FF);
        c.paint(240, 240, 0xFF0000FF);
        var soft = FreehandResampler.convert(c);
        var crisp = FreehandResampler.convert(c, FreehandResampler.Mode.CRISP);
        for (int i = 0; i < 1024; i++) {
            int a = soft.pixels()[i], b = crisp.pixels()[i];
            assertEquals(a & 0xffffff, b & 0xffffff);
            assertTrue((b >>> 24) >= (a >>> 24));
            assertTrue((b >>> 24) <= Math.min(255, 2 * (a >>> 24)));
        }
    }

    @Test public void nullSafetyAndEnumCompatibility() {
        var c = new FreehandCanvas(null);
        assertThrows(NullPointerException.class, () -> FreehandResampler.convert(null));
        assertThrows(NullPointerException.class, () -> FreehandResampler.convert(c, null));
        assertSame(FreehandResampler.Mode.CRISP, FreehandResampler.Mode.CLEAR);
        assertEquals(FreehandResampler.convert(c, FreehandResampler.Mode.SOFT), FreehandResampler.convert(c));
        assertEquals(c.snapshot(), c.snapshot(FreehandResampler.Mode.SOFT));
    }
}
