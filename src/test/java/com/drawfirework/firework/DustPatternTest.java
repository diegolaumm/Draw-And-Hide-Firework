package com.drawfirework.firework;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ParticleBudget;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class DustPatternTest {
    private static final int BLOOM_COPIES = 3; // DustStyle.BLOOM_130.particlesPerPoint()

    @Test public void heartMatchesPhase02Regression() {
        // Independent copy of the accepted Phase 02 loop; any drift in points/colour/order fails here.
        String[] mask = {".###...###.", "#####.#####", "###########", "###########",
                ".#########.", "..#######..", "...#####...", "....###....", ".....#....."};
        int[] colors = {0xff496c, 0xff7043, 0xffcc45, 0x68d875, 0x45c9e8, 0x6988ff, 0xb875ed, 0xef72c6, 0xff496c};
        var expected = new ArrayList<DustPattern.Point>();
        for (int row = 0; row < mask.length; row++) for (int col = 0; col < mask[row].length(); col++)
            if (mask[row].charAt(col) == '#') expected.add(new DustPattern.Point((col - 5) * .45f, (4 - row) * .45f, colors[row], 1));
        assertEquals(expected, DustPattern.HEART.points());
        assertEquals(63, DustPattern.HEART.points().size());
        assertEquals(189, DustPattern.HEART.coreParticles(BLOOM_COPIES));
        assertEquals(1.2f, 1.2f * DustPattern.HEART.points().getFirst().sizeScale(), 0); // exact Dust size kept
    }

    @Test public void transparencyStrategy() {
        // alpha 0 (any RGB) -> no point; partial alpha -> straight RGB, smaller opaque Dust; opaque -> size 1.
        CanvasData c = new CanvasData(4, 1, new int[]{0x00ffffff, 0x01102030, 0x80abcdef, 0xff445566});
        List<DustPattern.Point> points = DustPattern.fromCanvas(c, BLOOM_COPIES).points();
        assertEquals(3, points.size());
        assertEquals(0x102030, points.get(0).rgb());
        assertEquals(DustPattern.MIN_ALPHA_SIZE, points.get(0).sizeScale(), 0);
        assertEquals(0xabcdef, points.get(1).rgb());
        assertEquals((float) Math.sqrt(128 / 255.0), points.get(1).sizeScale(), 0);
        assertEquals(0x445566, points.get(2).rgb());
        assertEquals(1, points.get(2).sizeScale(), 0);
        assertTrue(DustPattern.fromCanvas(new CanvasData(32, 32, new int[1024]), BLOOM_COPIES).points().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> DustPattern.alphaSize(0));
        assertThrows(IllegalArgumentException.class, () -> DustPattern.alphaSize(256));
    }

    @Test public void coreBudgetNeverExceeds800() {
        int[] p = new int[65536]; var random = new Random(7);
        for (int i = 0; i < p.length; i++) p[i] = 0xff000000 | random.nextInt(1 << 24);
        CanvasData full = new CanvasData(256, 256, p);
        for (int copies = 1; copies <= 3; copies++) {
            DustPattern pattern = DustPattern.fromCanvas(full, copies);
            assertEquals(800 / copies, pattern.points().size());
            assertTrue(pattern.coreParticles(copies) <= ParticleBudget.MAX_CORE);
        }
        assertEquals(798, DustPattern.fromCanvas(full, BLOOM_COPIES).coreParticles(BLOOM_COPIES));
        assertEquals(798, DustPattern.fromCanvas(DevCanvases.dense(), BLOOM_COPIES).coreParticles(BLOOM_COPIES));
        // A pattern valid for one copy count is refused when more copies would exceed 800.
        DustPattern single = DustPattern.fromCanvas(full, 1);
        assertThrows(IllegalArgumentException.class, () -> single.coreParticles(BLOOM_COPIES));
        var tooMany = new ArrayList<DustPattern.Point>();
        for (int i = 0; i <= 800; i++) tooMany.add(new DustPattern.Point(0, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new DustPattern(tooMany));
    }

    @Test public void smallImagesAreNotInflated() {
        assertEquals(1, DustPattern.fromCanvas(new CanvasData(1, 1, new int[]{-1}), BLOOM_COPIES).points().size());
        DustPattern smiley = DustPattern.fromCanvas(DevCanvases.smiley(), BLOOM_COPIES);
        int visible = 0;
        for (int px : DevCanvases.smiley().pixels()) if ((px >>> 24) != 0) visible++;
        assertEquals(Math.min(visible, 266), smiley.points().size());
        assertTrue(smiley.points().stream().anyMatch(pt -> pt.sizeScale() < 1)); // 50% ring present
    }

    @Test public void geometryScaleAspectAndDeterminism() {
        var one = DustPattern.fromCanvas(new CanvasData(1, 1, new int[]{-1}), 3).points().getFirst();
        assertEquals(0, one.x(), 0); assertEquals(0, one.y(), 0);
        CanvasData wide = new CanvasData(8, 2, filled(16));
        var pts = DustPattern.fromCanvas(wide, 3).points();
        float step = DustPattern.CANVAS_SPAN / 8;
        assertEquals(step, pts.get(1).x() - pts.get(0).x(), 1e-6);
        assertEquals(step, pts.get(0).y() - pts.get(8).y(), 1e-6); // same spacing on both axes
        for (var pt : pts) assertTrue(Math.abs(pt.x()) < DustPattern.CANVAS_SPAN / 2 && Math.abs(pt.y()) < DustPattern.CANVAS_SPAN / 8 + 1e-6);
        assertEquals(DustPattern.fromCanvas(DevCanvases.dense(), 3), DustPattern.fromCanvas(DevCanvases.dense(), 3));
        assertNotEquals(DustPattern.fromCanvas(DevCanvases.dense(), 3), DustPattern.fromCanvas(DevCanvases.smiley(), 3));
        assertNotEquals(DustPattern.HEART, DustPattern.fromCanvas(DevCanvases.smiley(), 3));
    }

    @Test public void invalidInputsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DustPattern.Point(Float.NaN, 0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new DustPattern.Point(0, 0, 0x1000000, 1));
        assertThrows(IllegalArgumentException.class, () -> new DustPattern.Point(0, 0, 0, .1f));
        assertThrows(IllegalArgumentException.class, () -> DustPattern.fromCanvas(DevCanvases.dense(), 0));
        assertThrows(NullPointerException.class, () -> DustPattern.fromCanvas(null, 3));
        var mutable = new ArrayList<DustPattern.Point>(List.of(new DustPattern.Point(0, 0, 0, 1)));
        DustPattern copy = new DustPattern(mutable); mutable.clear();
        assertEquals(1, copy.points().size());
    }

    private static int[] filled(int n) { int[] p = new int[n]; java.util.Arrays.fill(p, 0xffaabbcc); return p; }
}
