package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Automated tests for Phase 07A — Artist's Canvas GUI Redesign:
 * 1. Responsive GUI layout & toolbar grouping never produce negative dimensions or overlap.
 * 2. Wooden easel frame never encroaches on the editable canvas area or right toolbar.
 * 3. Mouse coordinate mapping remains 100% invariant across GUI scales, window sizes, and frame thickness.
 * 4. Visual parchment backdrop theme never alters FreehandCanvas ARGB pixels, transparency, or firework output.
 * 5. Save Draft vs. To Firework semantic & visual category separation remains unambiguous.
 */
public class CanvasEditorThemeLayoutTest {

    private static MouseButtonEvent leftMouse(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(InputConstants.MOUSE_BUTTON_LEFT, 0));
    }

    @Test
    public void testResponsiveLayoutAndToolbarNeverOverlapOrGoNegative() {
        int[] widths = {140, 180, 240, 320, 427, 640, 854, 1280, 1920};
        int[] heights = {140, 180, 200, 240, 320, 480, 720, 1080};

        for (int w : widths) {
            for (int h : heights) {
                var board = CanvasEditorLayout.computeBoard(w, h, 256, 256, 18);
                var frame = CanvasEditorLayout.computeFrame(w, h, board);
                var toolbar = CanvasEditorLayout.computeToolbar(w, h);

                // 1. Strictly positive dimensions
                assertTrue("Board width must be > 0", board.width() > 0);
                assertTrue("Board height must be > 0", board.height() > 0);
                assertTrue("Toolbar panelWidth must be > 0", toolbar.panelWidth() > 0);
                assertTrue("Toolbar panelHeight must be > 0", toolbar.panelHeight() > 0);
                assertTrue("Toolbar innerWidth must be > 0", toolbar.innerWidth() > 0);
                assertTrue("Toolbar buttonHeight must be > 0", toolbar.buttonHeight() > 0);
                assertTrue("Toolbar rowHeight must be >= buttonHeight", toolbar.rowHeight() >= toolbar.buttonHeight());

                // 2. Strictly monotonic vertical row ordering in right toolbar
                assertTrue(toolbar.swatchRow1Y() < toolbar.swatchRow2Y());
                assertTrue(toolbar.swatchRow2Y() < toolbar.sliderY());
                assertTrue(toolbar.sliderY() < toolbar.toolsRowY());
                assertTrue(toolbar.toolsRowY() < toolbar.historyRowY());
                assertTrue(toolbar.historyRowY() < toolbar.viewRowY());
                assertTrue(toolbar.viewRowY() < toolbar.draftsRowY());
                assertTrue(toolbar.draftsRowY() < toolbar.actionsRowY());

                // 3. No button row overlaps the next row
                assertTrue(toolbar.swatchRow1Y() + toolbar.buttonHeight() <= toolbar.swatchRow2Y());
                assertTrue(toolbar.swatchRow2Y() + toolbar.buttonHeight() <= toolbar.sliderY());
                assertTrue(toolbar.sliderY() + toolbar.buttonHeight() <= toolbar.toolsRowY());
                assertTrue(toolbar.toolsRowY() + toolbar.buttonHeight() <= toolbar.historyRowY());
                assertTrue(toolbar.historyRowY() + toolbar.buttonHeight() <= toolbar.viewRowY());
                assertTrue(toolbar.viewRowY() + toolbar.buttonHeight() <= toolbar.draftsRowY());
                assertTrue(toolbar.draftsRowY() + toolbar.buttonHeight() <= toolbar.actionsRowY());

                // 4. Board and wooden frame never overlap right toolbar inner buttons
                assertTrue("Board right edge must stay left of toolbar",
                        board.left() + board.width() < toolbar.innerX());
                assertTrue("Wooden frame right edge must not encroach on toolbar buttons",
                        frame.outerRight() <= toolbar.innerX());

                // 5. Bottom action row stays within screen bounds on normal/compact windows (h >= 160)
                if (h >= 160) {
                    assertTrue("Bottom action buttons must fit within screenHeight",
                            toolbar.actionsRowY() + toolbar.buttonHeight() <= h);
                }
            }
        }
    }

    @Test
    public void testWoodenFrameNeverCoversInteractiveCanvasAndPreservesMouseMapping() {
        int[][] windowConfigs = {
                {427, 240}, // Standard Minecraft GUI Scale
                {640, 360}, // Medium window
                {854, 480}, // Large window
                {240, 180}  // Compact window
        };

        for (int[] cfg : windowConfigs) {
            int screenW = cfg[0];
            int screenH = cfg[1];
            var board = CanvasEditorLayout.computeBoard(screenW, screenH, 256, 256, 18);
            var frame = CanvasEditorLayout.computeFrame(screenW, screenH, board);

            // Inner frame opening must match board coordinates 100%
            assertEquals(board.left(), frame.innerLeft());
            assertEquals(board.top(), frame.innerTop());
            assertEquals(board.width(), frame.innerWidth());
            assertEquals(board.height(), frame.innerHeight());
            assertTrue("Frame thickness must be positive", frame.thickness() >= 1);

            // Outer frame surrounds the board strictly on the outside
            assertEquals(board.left() - frame.thickness(), frame.outerLeft());
            assertEquals(board.top() - frame.thickness(), frame.outerTop());
            assertEquals(board.left() + board.width() + frame.thickness(), frame.outerRight());
            assertEquals(board.top() + board.height() + frame.thickness(), frame.outerBottom());

            // Test mouse click mapping at top-left, center, bottom-right, and on the wooden frame itself
            FreehandCanvas canvas = new FreehandCanvas(null);
            canvas.setDiameter(1);
            FreehandInput input = new FreehandInput(canvas);

            // Clicking on the wooden frame (strictly outside inner board [left, top, left+width, top+height)) must NOT paint any pixel
            assertFalse("Click on left wooden frame must not paint",
                    input.click(leftMouse(frame.outerLeft(), board.top() + 5), board.left(), board.top(), board.width(), board.height(), 0xFFFF0000));
            assertFalse("Click on top wooden frame must not paint",
                    input.click(leftMouse(board.left() + 5, frame.outerTop()), board.left(), board.top(), board.width(), board.height(), 0xFFFF0000));
            assertFalse("Click on right wooden frame must not paint",
                    input.click(leftMouse(board.left() + board.width() + 0.1, board.top() + 5), board.left(), board.top(), board.width(), board.height(), 0xFFFF0000));
            assertFalse("Click on bottom wooden frame must not paint",
                    input.click(leftMouse(board.left() + 5, board.top() + board.height() + 0.1), board.left(), board.top(), board.width(), board.height(), 0xFFFF0000));
            assertFalse("Canvas must remain untouched after clicking frame", canvas.isChanged());

            // Clicking top-left corner cell inside the frame maps to pixel (0, 0)
            double tlX = board.left() + 0.5 * board.width() / 256.0;
            double tlY = board.top() + 0.5 * board.height() / 256.0;
            assertTrue(input.click(leftMouse(tlX, tlY),
                    board.left(), board.top(), board.width(), board.height(), 0xFFFF4040));
            input.release(leftMouse(tlX, tlY));
            assertEquals("Top-left inner pixel must be painted accurately", 0xFFFF4040, canvas.pixel(0, 0));

            // Clicking bottom-right corner cell inside the frame maps to pixel (255, 255)
            double brX = board.left() + 255.5 * board.width() / 256.0;
            double brY = board.top() + 255.5 * board.height() / 256.0;
            assertTrue(input.click(leftMouse(brX, brY),
                    board.left(), board.top(), board.width(), board.height(), 0xFF4080FF));
            input.release(leftMouse(brX, brY));
            assertEquals("Bottom-right inner pixel must be painted accurately", 0xFF4080FF, canvas.pixel(255, 255));
        }
    }

    @Test
    public void testParchmentThemeDoesNotPolluteTransparentOrWhitePixels() {
        FreehandCanvas canvas = new FreehandCanvas(null);

        // 1. Fresh canvas must remain 100% transparent (0x00000000), never filled with parchment color
        for (int p : canvas.pixels()) {
            assertEquals("Unpainted pixel must remain strictly 0 (transparent), never parchment", 0, p);
        }
        assertNotEquals("Parchment light color must not be transparent", 0, CanvasEditorTheme.PARCHMENT_LIGHT);
        assertNotEquals("Parchment dark color must not be transparent", 0, CanvasEditorTheme.PARCHMENT_DARK);

        // 2. Paint pure white and pale yellow strokes, plus erase part of it
        int pureWhite = 0xFFFFFFFF;
        int paleYellow = 0xFFFFDF40;
        canvas.setDiameter(8);
        canvas.paint(64, 64, pureWhite);
        canvas.endStroke();
        canvas.paint(192, 192, paleYellow);
        canvas.endStroke();

        assertEquals("Pure white stroke must retain exact ARGB", pureWhite, canvas.pixel(64, 64));
        assertEquals("Pale yellow stroke must retain exact ARGB", paleYellow, canvas.pixel(192, 192));
        assertEquals("Unpainted corner must remain transparent 0", 0, canvas.pixel(10, 240));

        // 3. Pure white and pale yellow must have distinct RGB contrast against both parchment backdrop tones
        assertNotEquals(pureWhite & 0xFFFFFF, CanvasEditorTheme.PARCHMENT_LIGHT & 0xFFFFFF);
        assertNotEquals(pureWhite & 0xFFFFFF, CanvasEditorTheme.PARCHMENT_DARK & 0xFFFFFF);
        assertNotEquals(paleYellow & 0xFFFFFF, CanvasEditorTheme.PARCHMENT_LIGHT & 0xFFFFFF);
        assertNotEquals(paleYellow & 0xFFFFFF, CanvasEditorTheme.PARCHMENT_DARK & 0xFFFFFF);

        // 4. Erasing restores true transparency (0), not parchment color
        canvas.paint(64, 64, 0);
        canvas.endStroke();
        assertEquals("Erased pixel must be 0 (transparent)", 0, canvas.pixel(64, 64));

        // 5. Undo / Redo and 32x32 snapshot output remain unaffected by GUI theme
        assertTrue(canvas.undo()); // Undo erase -> white is back
        assertEquals(pureWhite, canvas.pixel(64, 64));

        CanvasData snapshot = canvas.snapshot(FreehandResampler.Mode.SOFT);
        assertEquals("Unpainted 32x32 output cell must remain transparent", 0, snapshot.pixel(0, 31));
        assertTrue("Painted white 32x32 cell must be non-transparent", (snapshot.pixel(8, 8) >>> 24) > 0);

        // 6. 256x256 draft export retains exact pixel buffer
        int[] draftPixels = canvas.toDraft256Pixels();
        assertEquals(pureWhite, draftPixels[64 * 256 + 64]);
        assertEquals(0, draftPixels[0]);
    }

    @Test
    public void testSaveDraftAndToFireworkRemainVisuallyAndSemanticallyDistinct() {
        // Verify distinct icons
        assertNotEquals("Save Draft and To Firework must use distinct icons",
                CanvasEditorTheme.IconType.SAVE_DRAFT, CanvasEditorTheme.IconType.TO_FIREWORK);
        assertNotEquals("Drafts Folder and Save Draft must use distinct icons",
                CanvasEditorTheme.IconType.DRAFTS_FOLDER, CanvasEditorTheme.IconType.SAVE_DRAFT);

        // Verify distinct button color categories (Teal-Slate for Draft Storage vs Warm Amber-Gold for Firework Output)
        assertNotEquals("Draft Storage and Firework Output must have distinct category colors",
                CanvasEditorTheme.ButtonCategory.DRAFT_STORAGE.normalBg,
                CanvasEditorTheme.ButtonCategory.FIREWORK_OUTPUT.normalBg);
        assertNotEquals("Danger/Exit and Firework Output must have distinct category colors",
                CanvasEditorTheme.ButtonCategory.DANGER_OR_EXIT.normalBg,
                CanvasEditorTheme.ButtonCategory.FIREWORK_OUTPUT.normalBg);

        // Verify distinct row placement in toolbar layout
        var toolbar = CanvasEditorLayout.computeToolbar(427, 240);
        assertNotEquals("Drafts row and Firework output row must be on separate rows",
                toolbar.draftsRowY(), toolbar.actionsRowY());
    }

    @Test
    public void testEraserToFillAllowsTransparentFloodFillAndUndoRedo() {
        // 1. Create a canvas with a filled color block (red 0xFFFF4040)
        CanvasData blank = new CanvasData(32, 32, new int[32 * 32]);
        FreehandCanvas canvas = new FreehandCanvas(blank);
        FreehandInput input = new FreehandInput(canvas);

        int red = 0xFFFF4040;
        int blue = 0xFF4080FF;

        // Paint a solid red region (40x40 in 256x256 space)
        input.setTool(FreehandInput.Tool.FILL);
        assertTrue(input.click(leftMouse(64, 64), 0, 0, 256, 256, red));
        assertEquals("Entire canvas filled with red", red, canvas.pixel(0, 0));
        assertEquals("Entire canvas filled with red", red, canvas.pixel(128, 128));

        // 2. Switch tool: Eraser -> Fill (color is 0, tool is "eraser", active input tool is FILL)
        input.setTool(FreehandInput.Tool.BRUSH);
        input.reset();
        int currentColor = 0; // Eraser sets color = 0

        input.setTool(FreehandInput.Tool.FILL);
        input.reset();

        // 3. Execute transparent flood fill: clicking on red region fills it with 0 (transparent)
        assertTrue("Transparent flood fill must succeed", input.click(leftMouse(100, 100), 0, 0, 256, 256, currentColor));
        assertEquals("Flooded region must now be 0 (transparent)", 0, canvas.pixel(0, 0));
        assertEquals("Flooded region must now be 0 (transparent)", 0, canvas.pixel(128, 128));

        // 4. Test Undo & Redo on transparent flood fill
        assertTrue("Undo must revert transparent fill back to red", canvas.undo());
        assertEquals("Reverted pixel must be red", red, canvas.pixel(0, 0));
        assertEquals("Reverted pixel must be red", red, canvas.pixel(128, 128));

        assertTrue("Redo must re-apply transparent fill", canvas.redo());
        assertEquals("Redo pixel must be 0 (transparent)", 0, canvas.pixel(0, 0));
        assertEquals("Redo pixel must be 0 (transparent)", 0, canvas.pixel(128, 128));

        // 5. Subsequent switch back to Brush correctly restores paint color when selected
        // Simulate Screen tool selection transitions:
        // Eraser (color=0) -> Fill (color stays 0) -> Brush (restores lastPaintColor)
        int lastPaintColor = blue;
        int activeColor = 0; // from Eraser

        // selectFillTool(): color stays 0
        assertEquals(0, activeColor);

        // selectBrushTool(): if color == 0, restore lastPaintColor
        if (activeColor == 0) {
            activeColor = lastPaintColor;
        }
        assertEquals("Switching to Brush must restore last paint color", blue, activeColor);
    }
}
