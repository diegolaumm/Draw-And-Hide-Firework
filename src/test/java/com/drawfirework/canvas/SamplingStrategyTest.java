package com.drawfirework.canvas;

import com.drawfirework.client.HeartPrototype;
import org.junit.Test;

import static org.junit.Assert.*;

public class SamplingStrategyTest {

    @Test
    public void defaultStrategyIsDetailPreserving() {
        assertSame(SamplingStrategy.DETAIL_PRESERVING, SamplingStrategy.DEFAULT);
    }

    @Test
    public void fromStringParsing() {
        assertSame(SamplingStrategy.EXISTING, SamplingStrategy.fromString("existing"));
        assertSame(SamplingStrategy.DETAIL_PRESERVING, SamplingStrategy.fromString("detail_preserving"));
        assertSame(SamplingStrategy.EXISTING, SamplingStrategy.fromString("EXISTING"));
        assertSame(SamplingStrategy.DEFAULT, SamplingStrategy.fromString("default"));
        assertSame(SamplingStrategy.DEFAULT, SamplingStrategy.fromString(null));
        assertSame(SamplingStrategy.DEFAULT, SamplingStrategy.fromString("   "));

        assertThrows(IllegalArgumentException.class, () -> SamplingStrategy.fromString("unknown"));
    }

    @Test
    public void canvasDownsamplerDelegation() {
        int[] pixels = new int[32 * 32];
        for (int i = 0; i < 32; i++) pixels[i * 32 + i] = 0xFFFF0000;
        CanvasData diagonal = new CanvasData(32, 32, pixels);

        var existingSamples = CanvasDownsampler.sample(diagonal, 16, SamplingStrategy.EXISTING);
        var existingBaseline = CanvasDownsampler.sample(diagonal, 16);
        assertEquals(existingBaseline, existingSamples);

        var detailSamples = CanvasDownsampler.sample(diagonal, 16, SamplingStrategy.DETAIL_PRESERVING);
        assertNotNull(detailSamples);
        assertEquals(16, detailSamples.size());
    }

    @Test
    public void heartPrototypeSamplingStrategyStateAndReset() {
        HeartPrototype prototype = new HeartPrototype();
        prototype.resetSamplingStrategy();
        assertSame(SamplingStrategy.DETAIL_PRESERVING, prototype.samplingStrategy());
        assertSame(SamplingStrategy.DETAIL_PRESERVING, HeartPrototype.activeSamplingStrategy());

        prototype.setSamplingStrategy(SamplingStrategy.EXISTING);
        assertSame(SamplingStrategy.EXISTING, prototype.samplingStrategy());
        assertSame(SamplingStrategy.EXISTING, HeartPrototype.activeSamplingStrategy());

        prototype.setSamplingStrategy(null); // null fallback to DEFAULT
        assertSame(SamplingStrategy.DETAIL_PRESERVING, prototype.samplingStrategy());

        prototype.setSamplingStrategy(SamplingStrategy.EXISTING);
        prototype.resetSamplingStrategy();
        assertSame(SamplingStrategy.DETAIL_PRESERVING, prototype.samplingStrategy());
    }
}
