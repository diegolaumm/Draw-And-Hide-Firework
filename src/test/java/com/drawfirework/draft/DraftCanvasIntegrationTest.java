package com.drawfirework.draft;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import org.junit.Test;

import static org.junit.Assert.*;

public class DraftCanvasIntegrationTest {

    @Test
    public void testLoadDraftResetsHistoryAndUpdatesPixels() {
        CanvasData blank = CanvasData.blank();
        FreehandCanvas canvas = new FreehandCanvas(blank);

        // Make an initial stroke
        canvas.paint(10, 10, 0xFFFF0000);
        canvas.endStroke();
        assertTrue("Canvas should be able to undo after initial stroke", canvas.canUndo());
        long revBefore = canvas.revision();

        // Create a draft with a distinctive pixel pattern
        int[] draftPixels = new int[canvas.width() * canvas.height()];
        draftPixels[100 * canvas.width() + 100] = 0xFF00FF00;
        draftPixels[200 * canvas.width() + 200] = 0xFF0000FF;

        canvas.loadDraft(draftPixels);

        // Verification:
        // 1. Revision must increment
        assertTrue("Revision must increment after loading draft", canvas.revision() > revBefore);
        // 2. Undo stack must be cleared (cannot undo across draft boundary)
        assertFalse("Undo stack must be cleared after loading draft", canvas.canUndo());
        assertFalse("Redo stack must be empty", canvas.canRedo());
        // 3. Pixels must match draft
        assertEquals(0xFF00FF00, canvas.pixel(100, 100));
        assertEquals(0xFF0000FF, canvas.pixel(200, 200));
        assertEquals(0, canvas.pixel(10, 10)); // Old stroke gone

        // 4. New strokes can be painted and undone
        canvas.paint(50, 50, 0xFFFFFFFF);
        canvas.endStroke();
        assertTrue("New stroke should be undoable", canvas.canUndo());
        assertEquals(0xFFFFFFFF, canvas.pixel(50, 50));

        canvas.undo();
        assertEquals(0, canvas.pixel(50, 50));
        // Draft pixels must remain after undoing the new stroke
        assertEquals(0xFF00FF00, canvas.pixel(100, 100));
    }

    @Test
    public void testResetBlankResetsHistoryAndClearsPixels() {
        CanvasData blank = CanvasData.blank();
        FreehandCanvas canvas = new FreehandCanvas(blank);

        canvas.paint(10, 10, 0xFFFF0000);
        canvas.endStroke();
        assertTrue(canvas.canUndo());

        canvas.resetBlank();

        assertFalse("Undo stack must be cleared after resetBlank", canvas.canUndo());
        assertFalse(canvas.canRedo());
        assertEquals(0, canvas.pixel(10, 10));

        for (int p : canvas.pixels()) {
            assertEquals(0, p);
        }
    }

    @Test
    public void testLoadedDraftResamplingToFireworkOutput() {
        CanvasData blank = CanvasData.blank();
        FreehandCanvas canvas = new FreehandCanvas(blank);

        // Fill an 8x8 block in draft (corresponding to 1 pixel in 32x32 output)
        int[] draftPixels = new int[256 * 256];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                draftPixels[y * 256 + x] = 0xFFFF4040;
            }
        }
        canvas.loadDraft(draftPixels);

        CanvasData outputSoft = canvas.snapshot(FreehandResampler.Mode.SOFT);
        assertEquals(32, outputSoft.width());
        assertEquals(32, outputSoft.height());
        // Top-left pixel of 32x32 should be red
        assertEquals(0xFFFF4040, outputSoft.pixel(0, 0));
        // Other pixels should be transparent
        assertEquals(0, outputSoft.pixel(1, 0));
        assertEquals(0, outputSoft.pixel(0, 1));

        CanvasData outputCrisp = canvas.snapshot(FreehandResampler.Mode.CRISP);
        assertEquals(0xFFFF4040, outputCrisp.pixel(0, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadDraftRejectsInvalidLength() {
        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());
        canvas.loadDraft(new int[100]); // Wrong length
    }
}
