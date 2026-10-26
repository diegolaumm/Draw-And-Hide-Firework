package com.drawfirework.client;

/**
 * Pure Java deterministic layout calculator for DraftManagerScreen.
 * Guarantees zero overlap between draft list and buttons, non-negative dimensions,
 * and adaptive scaling to fit screen dimensions from 140x140 up to 4K displays.
 */
public final class DraftManagerLayout {

    public record LayoutResult(
            int listX,
            int listY,
            int listWidth,
            int listHeight,
            int buttonX,
            int buttonStartY,
            int buttonWidth,
            int buttonHeight,
            int buttonSpacing
    ) {}

    private DraftManagerLayout() {}

    public static LayoutResult compute(int screenWidth, int screenHeight, int buttonCount) {
        int leftMargin = screenWidth < 220 ? 6 : 12;
        int rightMargin = screenWidth < 220 ? 6 : 12;
        int middleGap = screenWidth < 220 ? 6 : 10;
        int topMargin = screenHeight < 200 ? 26 : 32;
        int bottomMargin = screenHeight < 200 ? 6 : 12;

        int availW = Math.max(80, screenWidth - leftMargin - rightMargin - middleGap);
        int buttonWidth;
        int listWidth;

        if (availW < 140) {
            buttonWidth = Math.max(50, Math.min(80, availW * 40 / 100));
            listWidth = Math.max(30, availW - buttonWidth);
        } else {
            buttonWidth = Math.max(60, Math.min(110, availW * 28 / 100));
            listWidth = availW - buttonWidth;
        }

        int listX = leftMargin;
        int buttonX = listX + listWidth + middleGap;

        // Ensure button does not spill past right edge
        if (buttonX + buttonWidth > screenWidth - 2) {
            buttonWidth = Math.max(40, screenWidth - 2 - buttonX);
        }

        int availH = Math.max(50, screenHeight - topMargin - bottomMargin);
        int n = Math.max(1, buttonCount);

        int buttonHeight;
        int buttonSpacing;

        int standardHeightNeeded = n * 20 + (n - 1) * 4;
        if (availH >= standardHeightNeeded) {
            buttonHeight = 20;
            buttonSpacing = Math.min(6, (availH - n * 20) / Math.max(1, n - 1));
        } else {
            buttonSpacing = Math.max(1, (availH - n * 14) / Math.max(1, n - 1));
            buttonHeight = Math.max(12, (availH - (n - 1) * buttonSpacing) / n);
            if (n * buttonHeight + (n - 1) * buttonSpacing > availH) {
                buttonSpacing = 1;
                buttonHeight = Math.max(10, (availH - (n - 1)) / n);
            }
        }

        int listY = topMargin;
        int listHeight = availH;
        int buttonStartY = topMargin;

        return new LayoutResult(
                listX, listY, listWidth, listHeight,
                buttonX, buttonStartY, buttonWidth, buttonHeight, buttonSpacing
        );
    }
}
