package com.drawfirework.client;

import org.junit.Test;

import static org.junit.Assert.*;

public class DraftManagerLayoutTest {

    @Test
    public void testLayoutInvariantsAcrossVariousScreenDimensions() {
        int[] widths = {140, 160, 180, 200, 240, 320, 400, 640, 800, 1920};
        int[] heights = {140, 160, 180, 200, 240, 300, 480, 600, 1080};
        int buttonCount = 7;

        for (int w : widths) {
            for (int h : heights) {
                DraftManagerLayout.LayoutResult layout = DraftManagerLayout.compute(w, h, buttonCount);

                // Minimum dimensions
                assertTrue("listWidth must be positive at " + w + "x" + h, layout.listWidth() >= 30);
                assertTrue("buttonWidth must be at least 40 at " + w + "x" + h, layout.buttonWidth() >= 40);
                assertTrue("buttonHeight must be at least 10 at " + w + "x" + h, layout.buttonHeight() >= 10);
                assertTrue("buttonSpacing must be >= 1 at " + w + "x" + h, layout.buttonSpacing() >= 1);

                // Horizontal non-overlap and containment
                assertTrue("List must be positioned within screen", layout.listX() >= 0);
                assertTrue("List and buttons must not overlap horizontally at " + w + "x" + h,
                        layout.listX() + layout.listWidth() <= layout.buttonX());
                assertTrue("Buttons must not spill off right edge at " + w + "x" + h,
                        layout.buttonX() + layout.buttonWidth() <= w);

                // Vertical containment
                assertTrue("List must not spill off bottom at " + w + "x" + h,
                        layout.listY() + layout.listHeight() <= h);

                int totalButtonsHeight = buttonCount * layout.buttonHeight() + (buttonCount - 1) * layout.buttonSpacing();
                assertTrue("Buttons must not spill off bottom at " + w + "x" + h,
                        layout.buttonStartY() + totalButtonsHeight <= h);
            }
        }
    }

    @Test
    public void testStandardDesktopDimensions() {
        DraftManagerLayout.LayoutResult layout = DraftManagerLayout.compute(800, 600, 7);
        assertEquals(20, layout.buttonHeight());
        assertEquals(6, layout.buttonSpacing());
        assertTrue(layout.buttonWidth() >= 100);
        assertTrue(layout.listWidth() >= 400);
    }
}
