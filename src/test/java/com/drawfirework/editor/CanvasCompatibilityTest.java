package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CanvasCompatibilityTest {

    @Test
    public void test16x16LegacyCanvasToDraft256AndBack() {
        // Legacy item canvas of 16x16 gives 128x128 working raster
        CanvasData legacyData = new CanvasData(16, 16, new int[16 * 16]);
        FreehandCanvas canvas = new FreehandCanvas(legacyData);

        assertEquals(128, canvas.width());
        assertEquals(128, canvas.height());
        assertEquals(16, canvas.outputWidth());
        assertEquals(16, canvas.outputHeight());

        // Paint a distinct pixel at (10, 20)
        canvas.paint(10, 20, 0xFFFF0000);
        int paintedColor = canvas.pixel(10, 20);
        assertNotEquals(0, paintedColor);

        // Export to 256x256 draft buffer
        int[] draft256 = canvas.toDraft256Pixels();
        assertEquals(256 * 256, draft256.length);

        // Verify the 2x2 block in draft buffer at (20..21, 40..41)
        assertEquals(paintedColor, draft256[40 * 256 + 20]);
        assertEquals(paintedColor, draft256[40 * 256 + 21]);
        assertEquals(paintedColor, draft256[41 * 256 + 20]);
        assertEquals(paintedColor, draft256[41 * 256 + 21]);

        // Load the 256x256 draft into a fresh 16x16 canvas
        FreehandCanvas restoredCanvas = new FreehandCanvas(legacyData);
        assertTrue(restoredCanvas.canLoadLosslessly(draft256));
        restoredCanvas.loadDraft(draft256);

        assertEquals(128, restoredCanvas.width());
        assertEquals(128, restoredCanvas.height());
        assertArrayEquals("128x128 raster must be deterministically restored without loss",
                canvas.pixels(), restoredCanvas.pixels());

        // Verify snapshot still outputs 16x16 CanvasData for server compatibility
        CanvasData snapshot = restoredCanvas.snapshot();
        assertEquals(16, snapshot.width());
        assertEquals(16, snapshot.height());
    }

    @Test
    public void testArbitrary256DraftWithFineDetailsRefusesToLoadOn128Canvas() {
        // 1. Create an arbitrary 256x256 draft containing fine details within a 2x2 block
        // (different colors, alpha, and fine diagonal line)
        int[] draft256 = new int[256 * 256];
        // In the 2x2 block at top-left:
        draft256[0] = 0xFFFF0000;              // (0, 0) Red
        draft256[1] = 0xFF0000FF;              // (1, 0) Blue
        draft256[256] = 0x8000FF00;            // (0, 1) Semi-transparent Green
        draft256[257] = 0x00000000;            // (1, 1) Transparent

        int[] originalDraftCopy = draft256.clone();

        // 2. 128x128 working canvas from 16x16 legacy item
        CanvasData legacyData = new CanvasData(16, 16, new int[16 * 16]);
        FreehandCanvas canvas = new FreehandCanvas(legacyData);

        // Paint current canvas artwork
        canvas.paint(10, 10, 0xFFFFFF00); // Yellow
        canvas.endStroke();
        int[] currentCanvasPixelsBefore = canvas.pixels();
        long revBefore = canvas.revision();
        boolean dirtyBefore = canvas.isChanged();

        // 3. Verify canLoadLosslessly returns false
        assertFalse("Canvas must recognize that fine 256x256 details cannot be losslessly represented on 128x128",
                canvas.canLoadLosslessly(draft256));

        // 4. loadDraft must throw IllegalArgumentException and refuse to load
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> canvas.loadDraft(draft256));
        assertTrue(ex.getMessage().contains("cannot be losslessly represented"));

        // 5. Verify the original draft pixels buffer is 100% UNMODIFIED
        assertArrayEquals("Original draft pixels must not be altered", originalDraftCopy, draft256);

        // 6. Verify currently editing canvas, pixels, dirty state, and revision are completely PRESERVED
        assertArrayEquals("Current canvas pixels must remain intact when load is refused",
                currentCanvasPixelsBefore, canvas.pixels());
        assertEquals("Revision must not change when load is refused", revBefore, canvas.revision());
        assertEquals("Dirty state must not change when load is refused", dirtyBefore, canvas.isChanged());

        // 7. Verify legacy 16x16 firework output is still intact
        CanvasData out = canvas.snapshot();
        assertEquals(16, out.width());
        assertEquals(16, out.height());
    }

    @Test
    public void testArbitrary256DraftLoadsLosslesslyOnStandard256Canvas() {
        int[] draft256 = new int[256 * 256];
        draft256[0] = 0xFFFF0000;
        draft256[1] = 0xFF0000FF;
        draft256[256] = 0x8000FF00;

        // Standard 256x256 canvas from 32x32 item
        CanvasData standardData = CanvasData.blank();
        FreehandCanvas canvas = new FreehandCanvas(standardData);

        assertTrue("256x256 canvas can always losslessly load 256x256 draft",
                canvas.canLoadLosslessly(draft256));

        canvas.loadDraft(draft256);
        assertArrayEquals("All 65,536 pixels must match exactly", draft256, canvas.pixels());

        CanvasData snapshot = canvas.snapshot();
        assertEquals(32, snapshot.width());
        assertEquals(32, snapshot.height());
    }

    @Test
    public void test32x32StandardCanvasToDraft256() {
        CanvasData standardData = CanvasData.blank(); // 32x32
        FreehandCanvas canvas = new FreehandCanvas(standardData);

        assertEquals(256, canvas.width());
        assertEquals(256, canvas.height());
        assertEquals(32, canvas.outputWidth());
        assertEquals(32, canvas.outputHeight());

        canvas.paint(50, 60, 0xFF00FF00);
        int[] draft256 = canvas.toDraft256Pixels();
        assertEquals(256 * 256, draft256.length);
        assertArrayEquals(canvas.pixels(), draft256);

        FreehandCanvas restored = new FreehandCanvas(standardData);
        restored.loadDraft(draft256);
        assertArrayEquals(canvas.pixels(), restored.pixels());

        CanvasData snapshot = restored.snapshot();
        assertEquals(32, snapshot.width());
        assertEquals(32, snapshot.height());
    }

    @Test
    public void testInvalidDraftBufferLengthThrowsIllegalArgument() {
        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());
        assertThrows(IllegalArgumentException.class, () -> canvas.loadDraft(null));
        assertThrows(IllegalArgumentException.class, () -> canvas.loadDraft(new int[100]));
    }
}
