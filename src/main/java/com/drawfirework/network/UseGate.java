package com.drawfirework.network;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.canvas.ItemCanvasCodecs;

/** One gate per player; reserve before calling transport, preventing reentrant sends. */
public final class UseGate {
    private long last = Long.MIN_VALUE;
    public boolean send(long tick, boolean creative, boolean correctItem, CanvasData canvas, Runnable transport) {
        if (!creative || !correctItem) return false;
        try { ItemCanvasCodecs.validate(canvas); } catch (IllegalArgumentException e) { return false; }
        if (last != Long.MIN_VALUE && tick >= last && tick-last < 10) return false;
        last=tick;
        transport.run();
        return true;
    }
}
