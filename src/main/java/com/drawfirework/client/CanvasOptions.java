package com.drawfirework.client;

/** Canvas command defaults, shared by the command tree and runtime plane factory. */
public record CanvasOptions(int scalePercent, int budget, boolean plain, boolean fixed) {
    public static final int UNSPECIFIED_BUDGET = -1;
    public static final CanvasOptions DEFAULT = new CanvasOptions(100, UNSPECIFIED_BUDGET, false, true);
    public static final int[] SCALES = {100, 125, 150};
    public static final int[] BUDGETS = {128, 192, 266};

    public boolean hasExplicitBudget() {
        return budget > 0;
    }
}
