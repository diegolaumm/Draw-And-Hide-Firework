package com.drawfirework.client;

/** Tick boundaries for the local Dust prototype, independent of render frame rate. */
public record FireworkTimeline(int holdTicks) {
    public static final int LAUNCH_END = 20;
    public static final int FORM_END = LAUNCH_END + 8;
    public static final int HOLD_END = FORM_END + 28;
    public static final int FADE_START = HOLD_END + 20;
    public static final int END = FADE_START + 14;

    public FireworkTimeline {
        if (holdTicks < 1 || holdTicks > 100) throw new IllegalArgumentException("holdTicks");
    }

    public int holdEnd() { return FORM_END + holdTicks; }
    public int fadeStart() { return holdEnd() + 20; }
    public int end() { return fadeStart() + 14; }

    public float fadeScaleAt(int tick) {
        if (tick < fadeStart()) return 1;
        if (tick >= end()) return 0;
        return (end() - tick) / (float) (end() - tick + 1);
    }

    public static float spread(int tick) {
        return Math.clamp((tick - LAUNCH_END) / 8f, 0, 1);
    }

    /** Multiplicative size change: preserves vanilla random colors and soft sprite. */
    public static float fadeScale(int tick) {
        if (tick < FADE_START) return 1;
        if (tick >= END) return 0;
        return (END - tick) / (float) (END - tick + 1);
    }
}
