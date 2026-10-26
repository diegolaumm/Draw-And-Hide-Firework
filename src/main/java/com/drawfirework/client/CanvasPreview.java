package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ParticleQuality;
import com.drawfirework.canvas.SamplingStrategy;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import com.drawfirework.firework.DustPattern;

/** One cached output is used both for preview and the save payload. */
public final class CanvasPreview {
    public record Output(CanvasData canvas, DustPattern pattern) {}
    private long revision = Long.MIN_VALUE;
    private FreehandResampler.Mode mode;
    private ParticleQuality quality;
    private SamplingStrategy strategy;
    private FreehandCanvas owner;
    private Output output;

    public Output get(FreehandCanvas canvas, FreehandResampler.Mode selected) {
        ParticleQuality currentQuality = HeartPrototype.activeQuality();
        SamplingStrategy currentStrategy = HeartPrototype.activeSamplingStrategy();
        if (owner != canvas || revision != canvas.revision() || mode != selected
                || quality != currentQuality || strategy != currentStrategy) {
            var data = canvas.snapshot(selected);
            output = new Output(data, HeartPrototype.canvasPattern(data, CanvasOptions.DEFAULT));
            owner = canvas;
            revision = canvas.revision();
            mode = selected;
            quality = currentQuality;
            strategy = currentStrategy;
        }
        return output;
    }
}
