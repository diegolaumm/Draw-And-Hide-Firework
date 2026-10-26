package com.drawfirework.editor;

/** Independent open/save cooldowns allow an immediate legitimate save after opening. */
public final class EditorRequestGate {
    private long lastOpen = Long.MIN_VALUE, lastSave = Long.MIN_VALUE;
    public boolean admit(long tick, boolean opening) {
        long last = opening ? lastOpen : lastSave;
        if (last != Long.MIN_VALUE && tick >= last && tick-last < 10) return false;
        if (opening) lastOpen = tick; else lastSave = tick;
        return true;
    }
}
