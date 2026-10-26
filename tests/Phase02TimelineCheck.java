import com.drawfirework.client.FireworkTimeline;

/** Run with Java source launcher after build; no extra test dependency. */
class Phase02TimelineCheck {
    public static void main(String[] args) {
        check(FireworkTimeline.LAUNCH_END == 20, "launch duration");
        check(FireworkTimeline.FORM_END - FireworkTimeline.LAUNCH_END == 8, "formation duration");
        check(FireworkTimeline.HOLD_END - FireworkTimeline.FORM_END == 28, "hold duration");
        check(FireworkTimeline.FADE_START - FireworkTimeline.HOLD_END == 20, "dispersion duration");
        check(FireworkTimeline.END - FireworkTimeline.FADE_START == 14, "fade duration");
        check(FireworkTimeline.spread(20) == 0 && FireworkTimeline.spread(28) == 1, "formation endpoints");
        float last = 0;
        for (int t = 20; t < 56; t++) {
            float spread = FireworkTimeline.spread(t);
            check(spread >= last && spread <= 1, "monotone formation");
            if (t >= 28) check(spread == 1, "complete shape throughout hold");
            last = spread;
        }
        float size = 1;
        for (int t = 76; t < 90; t++) {
            float next = size * FireworkTimeline.fadeScale(t);
            check(next > 0 && next < size, "strictly decreasing fade");
            size = next;
        }
        check(Math.abs(size - 1f / 15) < .00001f, "fade cumulative endpoint");
        check(FireworkTimeline.fadeScale(90) == 0, "end fully removed");
        System.out.println("PASS: phase durations, formation/hold boundaries, progressive size fade");
    }

    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
