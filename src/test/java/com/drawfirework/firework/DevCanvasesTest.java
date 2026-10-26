package com.drawfirework.firework;

import com.drawfirework.canvas.CanvasData;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.drawfirework.firework.DevCanvases.SmileyPalette.*;

public class DevCanvasesTest {
    @Test public void palettesDifferAndRepeatExactly() {
        assertNotEquals(DevCanvases.smiley(WARM), DevCanvases.smiley(COOL));
        for (var palette : DevCanvases.SmileyPalette.values()) {
            CanvasData a = DevCanvases.smiley(palette);
            assertEquals(a, DevCanvases.smiley(palette));
            int[] copy = a.pixels(); copy[0] = -1;
            assertEquals(a, DevCanvases.smiley(palette));
        }
    }

    @Test public void originalAndGeometryStayCompatible() {
        CanvasData original = DevCanvases.smiley();
        assertEquals(original, DevCanvases.smiley(ORIGINAL));
        assertEquals(16, original.width()); assertEquals(16, original.height());
        assertEquals(0x00ffffff, original.pixel(0, 0));
        assertEquals(0xffffd23f, original.pixel(8, 8));
        assertEquals(0xff4060ff, original.pixel(5, 5));
        assertEquals(0xffff3040, original.pixel(6, 10));
        assertEquals(0x8045e8e8, original.pixel(8, 1));
        for (var palette : DevCanvases.SmileyPalette.values()) {
            CanvasData c = DevCanvases.smiley(palette);
            assertEquals(original.width(), c.width()); assertEquals(original.height(), c.height());
            for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                assertEquals(original.pixel(x, y) >>> 24, c.pixel(x, y) >>> 24);
                // Every original semantic colour region must remain one colour region.
                for (int xx = 0; xx < 16; xx++)
                    assertEquals(original.pixel(x, y) == original.pixel(xx, y),
                            c.pixel(x, y) == c.pixel(xx, y));
            }
        }
    }

    @Test public void invalidPaletteRejected() {
        assertThrows(NullPointerException.class, () -> DevCanvases.smiley(null));
        assertThrows(IllegalArgumentException.class,
                () -> DevCanvases.SmileyPalette.valueOf("NEON"));
    }

    @Test public void realConversionPreservesGeometryAndBudget() {
        DustPattern warm = DustPattern.fromCanvas(DevCanvases.smiley(WARM), 3, 266);
        DustPattern cool = DustPattern.fromCanvas(DevCanvases.smiley(COOL), 3, 266);
        assertNotEquals(warm, cool);
        assertEquals(warm.points().size(), cool.points().size());
        for (int i = 0; i < warm.points().size(); i++) {
            var a = warm.points().get(i); var b = cool.points().get(i);
            assertEquals(a.x(), b.x(), 0); assertEquals(a.y(), b.y(), 0);
            assertEquals(a.sizeScale(), b.sizeScale(), 0);
        }
        for (var palette : DevCanvases.SmileyPalette.values()) {
            for (int budget : new int[]{128, 192, 266}) {
                var pattern = DustPattern.fromCanvas(DevCanvases.smiley(palette), 3, budget);
                assertTrue(pattern.points().size() <= budget);
                assertTrue(pattern.coreParticles(3) <= 798);
            }
        }
    }

    @Test public void alternatingGenerationsDoNotMutatePreviousPattern() {
        CanvasData warm = DevCanvases.smiley(WARM);
        DustPattern before = DustPattern.fromCanvas(warm, 3);
        DustPattern.fromCanvas(DevCanvases.smiley(COOL), 3);
        assertEquals(warm, DevCanvases.smiley(WARM));
        assertEquals(before, DustPattern.fromCanvas(warm, 3));
        assertEquals(DevCanvases.grid(), DevCanvases.grid());
        assertEquals(DevCanvases.rectangle(), DevCanvases.rectangle());
        assertEquals(DevCanvases.dense(), DevCanvases.dense());
    }
}
