package com.drawfirework.editor;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;
import java.util.Objects;
import java.util.UUID;

/** Server-owned one-shot capability; actual runtime and tests share this authorization. */
public final class EditSession {
    public static final int LIFETIME_TICKS = 12000;
    private final UUID token;
    private final Object stack;
    private final int slot;
    private final CanvasData original;
    private final long opened;
    private boolean consumed;

    public EditSession(UUID token, Object stack, int slot, CanvasData original, long tick) {
        this.token = Objects.requireNonNull(token); this.stack = Objects.requireNonNull(stack);
        if (original != null) ItemCanvasCodecs.validate(original);
        this.slot = slot; this.original = original; opened = tick;
    }
    public boolean current(long tick, boolean eligible, Object held, int selected, CanvasData canvas,
                           boolean unchangedStack) {
        return !consumed && eligible && tick >= opened && tick-opened < LIFETIME_TICKS
                && held == stack && selected == slot && unchangedStack && Objects.equals(original, canvas);
    }
    public boolean save(UUID supplied, long tick, boolean eligible, Object held, int selected,
                        CanvasData current, boolean unchangedStack, CanvasData replacement, Runnable write) {
        if (!token.equals(supplied)) return false;
        boolean allowed = current(tick, eligible, held, selected, current, unchangedStack);
        consumed = true; // Reserve before mutation, including failed attempts and reentry.
        if (!allowed) return false;
        try { ItemCanvasCodecs.validate(replacement); } catch (IllegalArgumentException e) { return false; }
        // The editor has no resize tool: never silently alter the original dimensions.
        int w = original == null ? CanvasData.DEFAULT_SIZE : original.width(), h = original == null ? CanvasData.DEFAULT_SIZE : original.height();
        if (replacement.width() != w || replacement.height() != h) return false;
        write.run();
        return true;
    }
}
