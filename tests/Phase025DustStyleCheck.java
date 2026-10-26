import com.drawfirework.client.DustStyle;
import com.drawfirework.client.FireworkTimeline;

class Phase025DustStyleCheck {
    public static void main(String[] args) {
        for (DustStyle style : new DustStyle[]{DustStyle.SOFT, DustStyle.LEGACY}) {
            var timeline = style.timeline();
            check(timeline.holdEnd() - FireworkTimeline.FORM_END == style.holdTicks(), "hold duration");
            check(timeline.fadeStart() - timeline.holdEnd() == 20, "dispersion duration");
            check(timeline.end() - timeline.fadeStart() == 14, "fade duration");
            int count = 0;
            for (int point = 0; point < 63; point++) {
                for (int copy = 0; copy < style.particlesPerPoint(); copy++) {
                    var sample = style.sample(point, copy, 1, 2);
                    check(sample.equals(style.sample(point, copy, 1, 2)), "repeatable seed");
                    check(Math.hypot(sample.x() - 1, sample.y() - 2) <= style.localOffset() + .00001, "offset radius");
                    check(Math.abs(sample.z()) <= style.localOffset() * .3 + .00001, "depth bound");
                    float size = 1;
                    for (int tick = 20; tick <= timeline.holdEnd(); tick++) {
                        float next = style.holdSize(tick, sample.phase());
                        size *= next / size;
                        check(Math.abs(size - next) < .00001, "size modulation cannot accumulate");
                        check(Math.abs(next - 1) <= style.sizeVariation() + .00001, "subtle size bound");
                    }
                    check(style.holdSize(28, sample.phase()) == 1, "formation joins hold without size jump");
                    check(Math.abs(size - 1) < .00001, "dispersion starts at baseline size");
                    if (style.equals(DustStyle.LEGACY)) {
                        check(sample.x() == 1 && sample.y() == 2 && sample.z() == 0, "legacy grid preserved");
                    }
                    count++;
                }
            }
            check(count == 63 * style.particlesPerPoint() && count <= 800, "core budget");
            float size = 1;
            for (int tick = timeline.fadeStart(); tick < timeline.end(); tick++) {
                float next = size * timeline.fadeScaleAt(tick);
                check(next > 0 && next < size, "fade monotonicity");
                size = next;
            }
            check(Math.abs(size - 1f / 15) < .00001 && timeline.fadeScaleAt(timeline.end()) == 0, "fade ends");
        }
        check(DustStyle.SOFT.timeline().end() == 76 && DustStyle.LEGACY.timeline().end() == 90, "preset durations");
        reject(() -> new DustStyle(0, 3, .14f, .05f));
        reject(() -> new DustStyle(14, 4, .14f, .05f));
        reject(() -> new DustStyle(14, 3, Float.NaN, .05f));
        reject(() -> new DustStyle(14, 3, .14f, Float.POSITIVE_INFINITY));
        System.out.println("PASS: soft/legacy durations, deterministic bounded offsets, modulation, fade and budgets");
    }

    private static void reject(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Invalid preset accepted");
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
