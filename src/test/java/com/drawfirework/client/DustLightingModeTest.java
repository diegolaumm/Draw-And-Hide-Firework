package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.firework.DevCanvases;
import com.drawfirework.firework.DustPattern;
import org.junit.Test;

import static org.junit.Assert.*;

public class DustLightingModeTest {

    @Test
    public void testBaselineComparisonMode() {
        DustLightingMode mode = DustLightingMode.BASELINE;
        assertFalse("Baseline must not use fullbright", mode.isFullbright());
        assertFalse("Baseline must not override vanilla color randomization", mode.isExactRgb());
        assertEquals("baseline", mode.commandName());
    }

    @Test
    public void testAllModeProperties() {
        assertTrue(DustLightingMode.FULLBRIGHT.isFullbright());
        assertFalse(DustLightingMode.FULLBRIGHT.isExactRgb());
        assertEquals("fullbright", DustLightingMode.FULLBRIGHT.commandName());

        assertTrue(DustLightingMode.FULLBRIGHT_EXACT.isFullbright());
        assertTrue(DustLightingMode.FULLBRIGHT_EXACT.isExactRgb());
        assertEquals("fullbright_exact", DustLightingMode.FULLBRIGHT_EXACT.commandName());

        assertFalse(DustLightingMode.EXACT_ONLY.isFullbright());
        assertTrue(DustLightingMode.EXACT_ONLY.isExactRgb());
        assertEquals("exact_only", DustLightingMode.EXACT_ONLY.commandName());
    }

    @Test
    public void testSafeParsing() {
        assertEquals(DustLightingMode.BASELINE, DustLightingMode.parse("baseline"));
        assertEquals(DustLightingMode.BASELINE, DustLightingMode.parse("BASELINE"));
        assertEquals(DustLightingMode.BASELINE, DustLightingMode.parse(" world "));
        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.parse("default"));
        assertEquals(DustLightingMode.BASELINE, DustLightingMode.parse("normal"));

        assertEquals(DustLightingMode.FULLBRIGHT, DustLightingMode.parse("fullbright"));
        assertEquals(DustLightingMode.FULLBRIGHT, DustLightingMode.parse("bright"));
        assertEquals(DustLightingMode.FULLBRIGHT, DustLightingMode.parse("fb"));

        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.parse("fullbright_exact"));
        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.parse("exact_fullbright"));
        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.parse("full"));
        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.parse("fbe"));

        assertEquals(DustLightingMode.EXACT_ONLY, DustLightingMode.parse("exact_only"));
        assertEquals(DustLightingMode.EXACT_ONLY, DustLightingMode.parse("exact"));
        assertEquals(DustLightingMode.EXACT_ONLY, DustLightingMode.parse("eo"));

        assertNull(DustLightingMode.parse(null));
        assertNull(DustLightingMode.parse(""));
        assertNull(DustLightingMode.parse("unknown_mode"));
        assertNull(DustLightingMode.parse("12345"));
    }

    @Test
    public void testFullbrightConstantMatchesMinecraftFormat() {
        // In Minecraft LightCoordsUtil.pack(15, 15) = (15 << 4) | (15 << 20) = 240 | 15728640 = 15728880 (0x00F000F0)
        assertEquals(15728880, DustLightingMode.FULL_BRIGHT_COORDS);
        assertEquals(0x00F000F0, DustLightingMode.FULL_BRIGHT_COORDS);
        int block = (DustLightingMode.FULL_BRIGHT_COORDS >> 4) & 15;
        int sky = (DustLightingMode.FULL_BRIGHT_COORDS >> 20) & 15;
        assertEquals("Block light coordinate must be 15", 15, block);
        assertEquals("Sky light coordinate must be 15", 15, sky);
    }

    @Test
    public void testRgbComponentExtraction() {
        int white = 0xffffff;
        assertEquals(1.0f, DustLightingMode.red(white), 1e-5f);
        assertEquals(1.0f, DustLightingMode.green(white), 1e-5f);
        assertEquals(1.0f, DustLightingMode.blue(white), 1e-5f);

        int black = 0x000000;
        assertEquals(0.0f, DustLightingMode.red(black), 1e-5f);
        assertEquals(0.0f, DustLightingMode.green(black), 1e-5f);
        assertEquals(0.0f, DustLightingMode.blue(black), 1e-5f);

        int red = 0xff0000;
        assertEquals(1.0f, DustLightingMode.red(red), 1e-5f);
        assertEquals(0.0f, DustLightingMode.green(red), 1e-5f);
        assertEquals(0.0f, DustLightingMode.blue(red), 1e-5f);

        int trailGold = 0xffcc75;
        assertEquals(255 / 255.0f, DustLightingMode.red(trailGold), 1e-5f);
        assertEquals(204 / 255.0f, DustLightingMode.green(trailGold), 1e-5f);
        assertEquals(117 / 255.0f, DustLightingMode.blue(trailGold), 1e-5f);
    }

    @Test
    public void testMockGlowingParticleAccess() {
        class MockParticle implements GlowingParticleAccess {
            private boolean fullbright;

            @Override
            public void drawfirework$setFullbright(boolean fullbright) {
                this.fullbright = fullbright;
            }

            @Override
            public boolean drawfirework$isFullbright() {
                return this.fullbright;
            }
        }

        MockParticle p = new MockParticle();
        assertFalse("Default must be not fullbright", p.drawfirework$isFullbright());
        p.drawfirework$setFullbright(true);
        assertTrue("Setter must enable fullbright", p.drawfirework$isFullbright());
        p.drawfirework$setFullbright(false);
        assertFalse("Setter must be reversible", p.drawfirework$isFullbright());
    }

    @Test
    public void testLightingModeDoesNotAlterCanvasDownsamplingOrBudget() {
        CanvasData smiley = DevCanvases.smiley();
        DustPattern pattern1 = DustPattern.fromCanvas(smiley, 3);
        DustPattern pattern2 = DustPattern.fromCanvas(smiley, 3);

        assertEquals("Point counts must remain identical", pattern1.points().size(), pattern2.points().size());
        assertEquals("Core particles must remain identical", pattern1.coreParticles(3), pattern2.coreParticles(3));
        for (int i = 0; i < pattern1.points().size(); i++) {
            var p1 = pattern1.points().get(i);
            var p2 = pattern2.points().get(i);
            assertEquals(p1.x(), p2.x(), 1e-6f);
            assertEquals(p1.y(), p2.y(), 1e-6f);
            assertEquals(p1.rgb(), p2.rgb());
            assertEquals(p1.sizeScale(), p2.sizeScale(), 1e-6f);
        }
    }

    @Test
    public void testHeartPrototypeLightingModeLifecycle() {
        HeartPrototype prototype = new HeartPrototype();
        assertEquals("Production default must be FULLBRIGHT_EXACT", DustLightingMode.FULLBRIGHT_EXACT, DustLightingMode.DEFAULT);
        assertEquals("Initial mode must be FULLBRIGHT_EXACT", DustLightingMode.FULLBRIGHT_EXACT, prototype.lightingMode());

        prototype.setLightingMode(DustLightingMode.BASELINE);
        assertEquals(DustLightingMode.BASELINE, prototype.lightingMode());

        prototype.setLightingMode(DustLightingMode.FULLBRIGHT);
        assertEquals(DustLightingMode.FULLBRIGHT, prototype.lightingMode());

        prototype.setLightingMode(DustLightingMode.FULLBRIGHT_EXACT);
        assertEquals(DustLightingMode.FULLBRIGHT_EXACT, prototype.lightingMode());

        prototype.setLightingMode(DustLightingMode.EXACT_ONLY);
        assertEquals(DustLightingMode.EXACT_ONLY, prototype.lightingMode());

        // Null safe fallback:
        prototype.setLightingMode(null);
        assertEquals("Null must fall back to production default", DustLightingMode.FULLBRIGHT_EXACT, prototype.lightingMode());
    }

    @Test
    public void testResetRestoresProductionDefaultFromEveryMode() {
        HeartPrototype prototype = new HeartPrototype();
        for (DustLightingMode mode : DustLightingMode.values()) {
            prototype.setLightingMode(mode);
            prototype.resetLightingMode();
            assertEquals("Reset from " + mode, DustLightingMode.FULLBRIGHT_EXACT, prototype.lightingMode());
        }
    }
}
