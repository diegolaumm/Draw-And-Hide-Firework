package com.drawfirework.client;

import com.drawfirework.canvas.ParticleQuality;
import com.drawfirework.firework.DevCanvases;
import com.drawfirework.firework.DustPattern;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParticleQualityTest {

    @Test
    public void standardBudgetAndSampleLimits() {
        ParticleQuality q = ParticleQuality.STANDARD;
        assertEquals(800, q.coreBudget());
        assertEquals(266, q.sampleLimit(3));
        assertEquals(266, q.sampleLimit());
        assertEquals(798, q.coreCount(266, 3));
        assertEquals(0, q.coreCount(0, 3));
        assertThrows(IllegalArgumentException.class, () -> q.coreCount(267, 3));
        assertThrows(IllegalArgumentException.class, () -> q.coreCount(-1, 3));
        assertThrows(IllegalArgumentException.class, () -> q.sampleLimit(0));
    }

    @Test
    public void balancedBudgetAndSampleLimits() {
        ParticleQuality q = ParticleQuality.BALANCED;
        assertEquals(1100, q.coreBudget());
        assertEquals(366, q.sampleLimit(3));
        assertEquals(366, q.sampleLimit());
        assertEquals(1098, q.coreCount(366, 3));
        assertTrue(q.coreCount(366, 3) <= 1100);
        assertThrows(IllegalArgumentException.class, () -> q.coreCount(367, 3));
    }

    @Test
    public void highBudgetAndSampleLimits() {
        ParticleQuality q = ParticleQuality.HIGH;
        assertEquals(1400, q.coreBudget());
        assertEquals(466, q.sampleLimit(3));
        assertEquals(466, q.sampleLimit());
        assertEquals(1398, q.coreCount(466, 3));
        assertTrue(q.coreCount(466, 3) <= 1400);
        assertThrows(IllegalArgumentException.class, () -> q.coreCount(467, 3));
    }

    @Test
    public void fromStringParsingAndDefault() {
        assertSame(ParticleQuality.BALANCED, ParticleQuality.DEFAULT);
        assertSame(ParticleQuality.STANDARD, ParticleQuality.fromString("standard"));
        assertSame(ParticleQuality.BALANCED, ParticleQuality.fromString("balanced"));
        assertSame(ParticleQuality.HIGH, ParticleQuality.fromString("high"));
        assertSame(ParticleQuality.STANDARD, ParticleQuality.fromString("STANDARD"));
        assertSame(ParticleQuality.STANDARD, ParticleQuality.fromString("  standard  "));
        assertSame(ParticleQuality.BALANCED, ParticleQuality.fromString("default"));
        assertSame(ParticleQuality.BALANCED, ParticleQuality.fromString(null));
        assertSame(ParticleQuality.BALANCED, ParticleQuality.fromString("   "));

        assertThrows(IllegalArgumentException.class, () -> ParticleQuality.fromString("ultra"));
        assertThrows(IllegalArgumentException.class, () -> ParticleQuality.fromString("invalid"));
    }

    @Test
    public void heartPrototypeQualityStateAndReset() {
        HeartPrototype prototype = new HeartPrototype();
        prototype.resetQuality();
        assertSame(ParticleQuality.BALANCED, prototype.quality());
        assertSame(ParticleQuality.BALANCED, HeartPrototype.activeQuality());

        prototype.setQuality(ParticleQuality.STANDARD);
        assertSame(ParticleQuality.STANDARD, prototype.quality());
        assertSame(ParticleQuality.STANDARD, HeartPrototype.activeQuality());

        prototype.setQuality(ParticleQuality.BALANCED);
        assertSame(ParticleQuality.BALANCED, prototype.quality());
        assertSame(ParticleQuality.BALANCED, HeartPrototype.activeQuality());

        prototype.setQuality(ParticleQuality.HIGH);
        assertSame(ParticleQuality.HIGH, prototype.quality());
        assertSame(ParticleQuality.HIGH, HeartPrototype.activeQuality());

        prototype.setQuality(null); // null fallback to DEFAULT (BALANCED)
        assertSame(ParticleQuality.BALANCED, prototype.quality());

        prototype.setQuality(ParticleQuality.HIGH);
        prototype.resetQuality();
        assertSame(ParticleQuality.BALANCED, prototype.quality());
    }

    @Test
    public void explicitBudget266RespectsExplicitValueAcrossAllQualityTiers() {
        var dense = DevCanvases.dense();
        var explicit266 = new CanvasOptions(100, 266, false, true);
        var explicit192 = new CanvasOptions(100, 192, false, true);
        var explicit999 = new CanvasOptions(100, 999, false, true);

        try {
            // 1. Standard quality (max 266)
            HeartPrototype.setActiveQuality(ParticleQuality.STANDARD);
            DustPattern stdDefault = HeartPrototype.canvasPattern(dense, CanvasOptions.DEFAULT);
            assertEquals(266, stdDefault.points().size());

            DustPattern stdExplicit266 = HeartPrototype.canvasPattern(dense, explicit266);
            assertEquals(266, stdExplicit266.points().size());

            DustPattern stdExplicit192 = HeartPrototype.canvasPattern(dense, explicit192);
            assertEquals(192, stdExplicit192.points().size());

            // 2. Balanced quality (max 366)
            HeartPrototype.setActiveQuality(ParticleQuality.BALANCED);
            DustPattern balDefault = HeartPrototype.canvasPattern(dense, CanvasOptions.DEFAULT);
            assertEquals(366, balDefault.points().size()); // Unspecified: expands to 366

            DustPattern balExplicit266 = HeartPrototype.canvasPattern(dense, explicit266);
            assertEquals(266, balExplicit266.points().size()); // Explicit 266: strictly respects 266!

            DustPattern balExplicit192 = HeartPrototype.canvasPattern(dense, explicit192);
            assertEquals(192, balExplicit192.points().size()); // Explicit 192: strictly respects 192!

            // 3. High quality (max 466)
            HeartPrototype.setActiveQuality(ParticleQuality.HIGH);
            DustPattern highDefault = HeartPrototype.canvasPattern(dense, CanvasOptions.DEFAULT);
            assertEquals(466, highDefault.points().size()); // Unspecified: expands to 466

            DustPattern highExplicit266 = HeartPrototype.canvasPattern(dense, explicit266);
            assertEquals(266, highExplicit266.points().size()); // Explicit 266: strictly respects 266!

            DustPattern highExplicit999 = HeartPrototype.canvasPattern(dense, explicit999);
            assertEquals(466, highExplicit999.points().size()); // Explicit 999: safely clamped to High's 466 limit!
        } finally {
            HeartPrototype.setActiveQuality(ParticleQuality.DEFAULT);
        }
    }
}
