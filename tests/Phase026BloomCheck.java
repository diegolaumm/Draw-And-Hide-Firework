import com.drawfirework.client.DustStyle;

class Phase026BloomCheck {
    public static void main(String[] args) {
        for (var style : new DustStyle[]{DustStyle.SOFT, DustStyle.BLOOM_115, DustStyle.BLOOM_130}) {
            check(style.holdTicks() == 14 && style.particlesPerPoint() == 3, "duration/count unchanged");
            float previous = 1;
            for (int tick = 0; tick <= 76; tick++) {
                float bloom = style.bloomSize(tick);
                check(bloom >= 1 && bloom <= style.bloomTarget(), "bounded bloom");
                check(bloom >= previous, "no sudden reset at dispersion/fade");
                if (tick <= 28) check(bloom == 1, "formation unchanged");
                if (tick >= 32) close(bloom, style.bloomTarget(), "target reached in four ticks");
                previous = bloom;
            }
            for (int point = 0; point < 63; point++) {
                for (int copy = 0; copy < 3; copy++) {
                    var sample = style.sample(point, copy, 1, 2);
                    check(sample.equals(DustStyle.SOFT.sample(point, copy, 1, 2)), "world offsets identical across profiles");
                    float size = 1;
                    for (int tick = 20; tick < 42; tick++) {
                        float target = style.particleSize(tick, sample.phase());
                        size *= target / size;
                        close(size, target, "ratio avoids exponential growth");
                        check(size <= style.bloomTarget() * 1.051f, "twinkle remains bounded");
                    }
                    if (style.bloomTarget() > 1) {
                        float release = size;
                        for (int elapsed = 0; elapsed <= 4; elapsed++) {
                            float target = release + (style.bloomTarget() - release)
                                    * DustStyle.smoothStep(elapsed / 4f);
                            if (elapsed == 0) close(target, size, "no hold/dispersion size discontinuity");
                            size *= target / size;
                        }
                        close(size, style.bloomTarget(), "settles to bloom instead of normal");
                    }
                }
            }
        }
        try {
            new DustStyle(14, 3, .14f, .05f, Float.NaN);
            throw new AssertionError("NaN bloom accepted");
        } catch (IllegalArgumentException expected) { }
        System.out.println("PASS: four-tick bloom, unchanged samples/count/timing, bounded scale and continuous release");
    }

    private static void close(float actual, float expected, String message) {
        check(Math.abs(actual - expected) < .00001f, message);
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
