package com.drawfirework.canvas;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class CanvasTest {
    private static CanvasData filled(int w, int h, int color) {
        int[] p = new int[w * h]; Arrays.fill(p, color); return new CanvasData(w, h, p);
    }
    private static CanvasData heart(int size) {
        String[] mask = {"01100110", "11111111", "11111111", "11111111", "01111110", "00111100", "00011000", "00000000"};
        int[] p = new int[size * size];
        for (int y = 0; y < size; y++) for (int x = 0; x < size; x++)
            if (mask[y * 8 / size].charAt(x * 8 / size) == '1') p[y * size + x] = 0xffff0044;
        return new CanvasData(size, size, p);
    }
    private static void rejects(Runnable r) { assertThrows(IllegalArgumentException.class, r::run); }
    private static void invariants(CanvasData c, int budget) {
        List<CanvasDownsampler.Sample> samples = CanvasDownsampler.sample(c, budget);
        assertTrue(samples.size() <= budget);
        assertEquals(samples, CanvasDownsampler.sample(c, budget));
        int visible = 0;
        for (int p : c.pixels()) if ((p >>> 24) != 0) visible++;
        assertEquals(Math.min(visible, budget), samples.size());
        assertEquals(visible, samples.stream().mapToInt(CanvasDownsampler.Sample::weight).sum());
        var positions = new HashSet<String>();
        double scale = Math.max(c.width(), c.height());
        for (var s : samples) {
            int x = (int) Math.round(s.x() * scale + c.width() / 2.0 - .5);
            int y = (int) Math.round(c.height() / 2.0 - s.y() * scale - .5);
            assertEquals(c.pixel(x, y), s.argb());
            assertTrue(s.alpha() > 0 && s.alpha() <= 1);
            assertTrue(Math.abs(s.x()) < .5 && Math.abs(s.y()) < .5);
            assertTrue(positions.add(x + "," + y));
        }
    }
    @Test public void immutableAndValueEquality() {
        int[] p = {0x80112233}; CanvasData c = new CanvasData(1, 1, p);
        p[0] = 0; c.pixels()[0] = 0;
        assertEquals(0x80112233, c.pixel(0, 0));
        assertEquals(c, new CanvasData(1, 1, new int[]{0x80112233}));
        assertEquals(c.hashCode(), new CanvasData(1, 1, new int[]{0x80112233}).hashCode());
        assertEquals(1024, CanvasData.blank().pixelCount());
        assertThrows(IndexOutOfBoundsException.class, () -> c.pixel(1, 0));
    }
    @Test public void invalidDimensionsAndLengths() {
        for (int n : new int[]{-1, 0, 257, Integer.MAX_VALUE, Integer.MIN_VALUE}) {
            rejects(() -> new CanvasData(n, n, new int[0]));
            rejects(() -> new CanvasData(1, n, new int[0]));
        }
        rejects(() -> new CanvasData(2, 2, new int[3]));
        rejects(() -> new CanvasData(2, 2, new int[5]));
        assertThrows(NullPointerException.class, () -> new CanvasData(1, 1, null));
    }
    @Test public void onePixelAndTinyBudgets() {
        CanvasData c = filled(1, 1, 0x80ff0000);
        invariants(c, 1); invariants(c, 800);
        var s = CanvasDownsampler.sample(c, 1).getFirst();
        assertEquals(0, s.x(), 0); assertEquals(0, s.y(), 0); assertEquals(128 / 255.0, s.alpha(), 0);
        assertTrue(CanvasDownsampler.sample(c, 0).isEmpty());
        rejects(() -> CanvasDownsampler.sample(c, -1)); rejects(() -> CanvasDownsampler.sample(c, 801));
        invariants(heart(16), 1); invariants(heart(16), 2);
        assertThrows(UnsupportedOperationException.class, () -> CanvasDownsampler.sample(c, 1).clear());
    }
    @Test public void transparentAndIsolatedPixel() {
        invariants(filled(256, 256, 0x00ffffff), 266);
        int[] p = new int[65536]; p[253 * 256 + 3] = 0x0100ff00;
        CanvasData c = new CanvasData(256, 256, p); invariants(c, 266);
        var s = CanvasDownsampler.sample(c, 266).getFirst();
        assertEquals((3.5 - 128) / 256, s.x(), 0); assertEquals(1 / 255.0, s.alpha(), 0);
    }
    @Test public void heartsAndTransparentBorder() {
        for (int n : new int[]{8, 16}) for (int b : new int[]{1, 8, 32, 266}) invariants(heart(n), b);
        int[] p = new int[32 * 32]; p[16 * 32 + 16] = 0xff123456;
        var s = CanvasDownsampler.sample(new CanvasData(32, 32, p), 10).getFirst();
        assertEquals(.5 / 32, s.x(), 0); assertEquals(-.5 / 32, s.y(), 0);
    }
    @Test public void denseColorAndMaximumSize() {
        for (int size : new int[]{32, 256}) {
            int[] p = new int[size * size]; var random = new Random(42);
            for (int i = 0; i < p.length; i++) p[i] = 0xff000000 | random.nextInt(1 << 24);
            CanvasData c = new CanvasData(size, size, p);
            invariants(c, 266); invariants(c, 800);
            assertEquals(c, CanvasSerialization.fromJson(CanvasSerialization.toJson(c)));
            assertTrue(CanvasSerialization.toJson(c).length() < CanvasSerialization.MAX_JSON_BYTES);
        }
    }
    @Test public void diagonalAndCheckerboard() {
        int[] line = new int[65536], checker = new int[65536];
        for (int y = 0; y < 256; y++) for (int x = 0; x < 256; x++) {
            if (x == y) line[y * 256 + x] = 0xffff0000;
            checker[y * 256 + x] = ((x + y) % 2 == 0) ? 0xffff0000 : 0xff0000ff;
        }
        invariants(new CanvasData(256, 256, line), 266);
        invariants(new CanvasData(256, 256, line), 16);
        invariants(new CanvasData(256, 256, checker), 266);
        for (var s : CanvasDownsampler.sample(new CanvasData(256, 256, line), 16)) assertEquals(-s.x(), s.y(), 0);
    }
    @Test public void rectanglePreservesScale() {
        CanvasData c = filled(8, 2, 0x40abcdef); invariants(c, 100);
        var points = CanvasDownsampler.sample(c, 100);
        assertEquals(1.0 / 8, points.get(1).x() - points.get(0).x(), 0);
        assertEquals(1.0 / 8, points.get(0).y() - points.get(8).y(), 0);
        invariants(filled(1, 256, 0xffaabbcc), 17);
        invariants(filled(256, 1, 0xffaabbcc), 17);
    }
    @Test public void jsonAndCodecRoundtrip() {
        CanvasData c = new CanvasData(2, 2, new int[]{0x00123456, 0x01010203, 0x80abcdef, 0xffffffff});
        assertEquals(c, CanvasSerialization.fromJson(CanvasSerialization.toJson(c)));
        var encoded = CanvasSerialization.CODEC.encodeStart(JsonOps.INSTANCE, c).getOrThrow();
        assertEquals(c, CanvasSerialization.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertEquals(c, CanvasSerialization.fromJson(encoded.toString()));
        assertEquals(c, CanvasSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(CanvasSerialization.toJson(c))).getOrThrow());
    }
    @Test public void malformedJsonRejected() {
        String good = CanvasSerialization.toJson(filled(1, 1, -1));
        for (String bad : new String[]{"{}", "null", "[]", good + "{}", good.replace("\"version\":1", "\"version\":2"),
                good.replace("\"width\":1", "\"width\":0"), good.replace("\"width\":1", "\"width\":257"),
                good.replace("\"width\":1", "\"width\":1.5"), good.replace("\"width\":1", "\"width\":\"1\""),
                good.replace("\"width\":1", "\"width\":2147483648"), good.replace("ffffffff", "ffff"),
                good.replace("ffffffff", "gggggggg"), good.replace("ffffffff", "ffffffffffffffff"),
                good.replace("argb32-hex", "unknown"), good.replace("\"pixels\":\"ffffffff\"", "\"pixels\":[-1]"),
                good.replace("\"width\":1,", ""), good.replace("{", "{\"width\":1,"),
                good.replace("{", "{\"extra\":{},"), " ".repeat(CanvasSerialization.MAX_JSON_BYTES + 1),
                "[".repeat(10000), good + "中", good.replace("1,", "1e0,")}) rejects(() -> CanvasSerialization.fromJson(bad));
    }
    @Test public void codecRejectsInvalidData() {
        String good = CanvasSerialization.toJson(filled(1, 1, -1));
        for (String bad : new String[]{"{}", good.replace("\"version\":1", "\"version\":2"),
                good.replace("\"width\":1", "\"width\":1.5"), good.replace("\"width\":1", "\"width\":4294967297"),
                good.replace("\"width\":1", "\"width\":1e10000000"),
                good.replace("\"width\":1", "\"width\":0"), good.replace("\"height\":1", "\"height\":-1"),
                good.replace("\"width\":1", "\"width\":257"), good.replace("ffffffff", "abcd"),
                good.replace("argb32-hex", "no"), good.replace("ffffffff", "0".repeat(524296))})
            assertTrue(CanvasSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(bad)).isError());
    }
    @Test public void independentParticleBudgets() {
        assertEquals(266, ParticleBudget.BLOOM.sampleLimit()); assertEquals(798, ParticleBudget.BLOOM.coreCount(266));
        rejects(() -> ParticleBudget.BLOOM.coreCount(267));
        rejects(() -> new ParticleBudget(801, 3, 0)); rejects(() -> new ParticleBudget(800, 0, 0));
        rejects(() -> new ParticleBudget(800, 3, 201));
        assertEquals(0, new ParticleBudget(2, 3, 200).sampleLimit());
        assertEquals(800, new ParticleBudget(800, 1, 0).sampleLimit());
    }
    @Test public void randomizedDeterminismAndBounds() {
        var random = new Random(913);
        for (int i = 0; i < 60; i++) {
            int w = random.nextInt(64) + 1, h = random.nextInt(64) + 1; int[] p = new int[w * h];
            for (int j = 0; j < p.length; j++) if (random.nextInt(5) == 0) p[j] = random.nextInt();
            invariants(new CanvasData(w, h, p), random.nextInt(266) + 1);
        }
    }
    @Test public void compareCandidateStrategies() {
        // A single off-grid pixel is missed by regular nearest-neighbour; area average dilutes alpha.
        int[] sparse = new int[1024]; sparse[0] = 0xffff0000;
        CanvasData c = new CanvasData(32, 32, sparse);
        int nearestVisible = 0; double averageAlpha = 0;
        for (int by = 0; by < 4; by++) for (int bx = 0; bx < 4; bx++) {
            if ((c.pixel(bx * 8 + 4, by * 8 + 4) >>> 24) != 0) nearestVisible++;
            int alpha = 0;
            for (int y = by * 8; y < by * 8 + 8; y++) for (int x = bx * 8; x < bx * 8 + 8; x++) alpha += c.pixel(x, y) >>> 24;
            averageAlpha = Math.max(averageAlpha, alpha / 64.0);
        }
        assertEquals(0, nearestVisible); assertEquals(255 / 64.0, averageAlpha, 0);
        assertEquals(255, CanvasDownsampler.sample(c, 16).getFirst().argb() >>> 24);
        System.out.println("Candidate probe: nearest visible=0; area alpha=3.984375; adaptive visible=1 alpha=255");
        int[] checker = new int[1024];
        for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) checker[y * 32 + x] = ((x + y) % 2 == 0) ? 0xffff0000 : 0xff0000ff;
        var samples = CanvasDownsampler.sample(new CanvasData(32, 32, checker), 16);
        long red = samples.stream().filter(s -> s.argb() == 0xffff0000).count();
        assertTrue("Both checker colours should survive this fixture", red > 0 && red < 16);
        System.out.println("Checker 32x32 -> 16: adaptive red=" + red + ", blue=" + (16 - red) + "; area RGB=(127.5,0,127.5); palette retained but aliasing possible");
        assertTrue(samples.stream().allMatch(s -> s.argb() == 0xffff0000 || s.argb() == 0xff0000ff));
    }

    @Test public void maximumSizeProcessingProbe() {
        int[] p = new int[65536]; var random = new Random(23);
        for (int i = 0; i < p.length; i++) p[i] = 0xff000000 | random.nextInt(1 << 24);
        CanvasData c = new CanvasData(256, 256, p);
        for (int i = 0; i < 5; i++) CanvasDownsampler.sample(c, 266);
        var bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        boolean measureAllocation = bean.isThreadAllocatedMemorySupported();
        if (measureAllocation && !bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
        long thread = Thread.currentThread().threadId();
        long beforeBytes = measureAllocation ? bean.getThreadAllocatedBytes(thread) : 0;
        long start = System.nanoTime();
        for (int i = 0; i < 20; i++) assertEquals(266, CanvasDownsampler.sample(c, 266).size());
        double millis = (System.nanoTime() - start) / 1_000_000.0 / 20;
        long bytes = measureAllocation ? (bean.getThreadAllocatedBytes(thread) - beforeBytes) / 20 : -1;
        System.out.println("Local JVM probe 256x256 -> 266, 5 warmups/20 iterations: mean ms=" + millis + ", allocated bytes/op=" + bytes);
        System.out.println("Maximum JSON characters=" + CanvasSerialization.toJson(c).length());
        // Diagnostic only: no hardware-dependent speed threshold and no game FPS claim.
    }
}
