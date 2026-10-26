package com.drawfirework.client;

import com.drawfirework.firework.DustPattern;
import java.util.List;

/**
 * Pure Java layout, measurement and coordinate projection for CanvasEditorScreen.
 * Independent of Minecraft GUI/rendering classes so all geometry and boundaries can be strictly unit-tested.
 */
public final class CanvasEditorLayout {
    private CanvasEditorLayout() {}

    public static final int TOP_BAR_HEIGHT = 32;
    public static final int MARGIN_LEFT = 8;
    public static final int MARGIN_BOTTOM = 4;
    public static final int SPACING_BOARD_TO_STATS = 6;
    public static final int SPACING_STATS_TO_DISCLAIMER = 4;
    public static final int DEFAULT_LINE_HEIGHT = 9;
    public static final int DEFAULT_FRAME_THICKNESS = 4;

    public record BoardRect(int left, int top, int width, int height) {}

    public record FrameRect(
            int outerLeft,
            int outerTop,
            int outerRight,
            int outerBottom,
            int innerLeft,
            int innerTop,
            int innerWidth,
            int innerHeight,
            int thickness
    ) {}

    public record ToolbarGrouping(
            int panelX,
            int panelY,
            int panelWidth,
            int panelHeight,
            int innerX,
            int innerWidth,
            int rowHeight,
            int buttonHeight,
            int swatchRow1Y,
            int swatchRow2Y,
            int sliderY,
            int toolsRowY,
            int historyRowY,
            int viewRowY,
            int draftsRowY,
            int actionsRowY
    ) {}

    public record TextPlacement(
            int statsX,
            int statsY,
            int disclaimerX,
            int disclaimerY,
            int maxTextWidth,
            int maxAllowedDisclaimerLines
    ) {}

    public static int panelWidth(int screenWidth) {
        return Math.min(128, Math.max(84, screenWidth / 3));
    }

    public static int panelStart(int screenWidth) {
        return screenWidth - panelWidth(screenWidth) - 8;
    }

    public static int availableBoardWidth(int screenWidth) {
        return Math.max(1, screenWidth - panelWidth(screenWidth) - 28);
    }

    public static int maxTextWidth(int screenWidth) {
        int pStart = panelStart(screenWidth);
        // Guarantee at least 4px clearance before the right control panel; strictly non-negative
        return Math.max(0, pStart - 4 - MARGIN_LEFT);
    }

    /**
     * Computes the adaptive wooden frame thickness around the board so that the outer frame
     * never crosses left of MARGIN_LEFT/2 or right into panelStart - 2.
     */
    public static int frameThickness(int screenWidth, int screenHeight, BoardRect board) {
        int maxLeft = Math.max(1, board.left() - 2);
        int maxTop = Math.max(1, board.top() - 4);
        int maxRight = Math.max(1, panelStart(screenWidth) - 2 - (board.left() + board.width()));
        int allowed = Math.min(DEFAULT_FRAME_THICKNESS, Math.min(maxLeft, Math.min(maxTop, maxRight)));
        return Math.max(1, allowed);
    }

    /**
     * Computes the wooden frame geometry around the board.
     * The inner interactive area is strictly identical to board [left, top, width, height].
     */
    public static FrameRect computeFrame(int screenWidth, int screenHeight, BoardRect board) {
        int t = frameThickness(screenWidth, screenHeight, board);
        return new FrameRect(
                board.left() - t,
                board.top() - t,
                board.left() + board.width() + t,
                board.top() + board.height() + t,
                board.left(),
                board.top(),
                board.width(),
                board.height(),
                t
        );
    }

    /**
     * Computes the compact grouped artist toolbar layout on the right side of the screen.
     * Consolidates the previous 10 text-heavy rows into 8 clean rows with visual section gaps.
     */
    public static ToolbarGrouping computeToolbar(int screenWidth, int screenHeight) {
        int pWidth = panelWidth(screenWidth);
        int pStart = panelStart(screenWidth);
        int totalRows = 8;

        int rowH;
        int btnH;
        int gapAfterColor;
        int gapAfterTools;
        int gapAfterView;

        if (screenHeight >= 250) {
            rowH = 22;
            btnH = 19;
            gapAfterColor = 4;
            gapAfterTools = 4;
            gapAfterView = 4;
        } else if (screenHeight >= 210) {
            rowH = 20;
            btnH = 18;
            gapAfterColor = 2;
            gapAfterTools = 2;
            gapAfterView = 2;
        } else {
            rowH = Math.max(14, (screenHeight - 24) / totalRows);
            btnH = Math.max(12, rowH - 2);
            gapAfterColor = 0;
            gapAfterTools = 0;
            gapAfterView = 0;
        }

        int contentHeight = totalRows * rowH + gapAfterColor + gapAfterTools + gapAfterView;
        int startY = screenHeight >= 250
                ? 28
                : Math.max(8, (screenHeight - contentHeight) / 2);

        int y0 = startY;                        // Swatch Row 1 (4 colors)
        int y1 = y0 + rowH;                     // Swatch Row 2 (4 colors)
        int y2 = y1 + rowH;                     // Brush Size Slider
        int y3 = y2 + rowH + gapAfterColor;     // Tools: Brush, Eraser, Fill
        int y4 = y3 + rowH;                     // History: Undo, Redo
        int y5 = y4 + rowH + gapAfterTools;     // View & Mode: Soft/Crisp, Draw/32x32/Dust
        int y6 = y5 + rowH + gapAfterView;      // Drafts: Drafts Folder, Save Draft
        int y7 = y6 + rowH;                     // Output & Exit: Close, To Firework

        int panelPad = 3;
        int panelX = Math.max(0, pStart - panelPad);
        int panelY = Math.max(2, y0 - panelPad - 1);
        int panelW = Math.min(screenWidth - panelX - 2, pWidth + panelPad * 2 - 2);
        int panelH = Math.min(screenHeight - panelY - 2, (y7 + btnH) - y0 + panelPad * 2 + 2);

        return new ToolbarGrouping(
                panelX, panelY, Math.max(1, panelW), Math.max(1, panelH),
                pStart, Math.max(1, pWidth - 2),
                rowH, btnH,
                y0, y1, y2, y3, y4, y5, y6, y7
        );
    }

    /**
     * Computes the board rectangle dynamically based on the vertical space required for text.
     * @param screenWidth window width
     * @param screenHeight window height
     * @param modelWidth editable raster width (e.g. 128 or 256)
     * @param modelHeight editable raster height (e.g. 128 or 256)
     * @param textHeightNeeded measured height in pixels needed by the disclaimer / session note
     */
    public static BoardRect computeBoard(int screenWidth, int screenHeight, int modelWidth, int modelHeight,
                                         int textHeightNeeded) {
        int availW = availableBoardWidth(screenWidth);
        int clampedTextHeight = Math.max(DEFAULT_LINE_HEIGHT, Math.min(screenHeight / 3, textHeightNeeded));
        int overheadBelowBoard = SPACING_BOARD_TO_STATS + DEFAULT_LINE_HEIGHT + SPACING_STATS_TO_DISCLAIMER
                + clampedTextHeight + MARGIN_BOTTOM;
        int maxBoardH = Math.max(1, screenHeight - TOP_BAR_HEIGHT - overheadBelowBoard);
        double scale = Math.min(1.5, Math.min(availW / (double) modelWidth, maxBoardH / (double) modelHeight));
        int bw = Math.max(1, (int) Math.round(modelWidth * scale));
        int bh = Math.max(1, (int) Math.round(modelHeight * scale));
        int left = 10 + (availW - bw) / 2;
        int top = TOP_BAR_HEIGHT;
        return new BoardRect(left, top, bw, bh);
    }

    /**
     * Computes text positioning and constraints.
     */
    public static TextPlacement computeTextPlacement(int screenWidth, int screenHeight, BoardRect board,
                                                     int statsTextWidth, int lineHeight) {
        int pStart = panelStart(screenWidth);
        int maxW = maxTextWidth(screenWidth);
        int lh = Math.max(1, lineHeight);

        int textY1 = board.top() + board.height() + SPACING_BOARD_TO_STATS;
        int textY2 = textY1 + lh + SPACING_STATS_TO_DISCLAIMER;

        // Position statsX: center over board if possible, strictly clamped between MARGIN_LEFT and pStart - 4 - statsTextWidth
        int targetStatsX = board.left() + (board.width() - statsTextWidth) / 2;
        int minX = MARGIN_LEFT;
        int maxX = Math.max(minX, pStart - 4 - statsTextWidth);
        int statsX = Math.max(minX, Math.min(maxX, targetStatsX));

        int availableHeight = Math.max(0, screenHeight - MARGIN_BOTTOM - textY2);
        int maxLines = availableHeight / lh;

        return new TextPlacement(statsX, textY1, MARGIN_LEFT, textY2, maxW, maxLines);
    }

    @FunctionalInterface
    public interface TextWidthMeter<T> {
        int width(T text);
    }

    /**
     * Evaluates candidates in order and returns the first candidate whose width <= maxTextWidth.
     * If maxTextWidth <= 0 or no candidate fits, returns null (safe omission).
     */
    public static <T> T chooseStatsText(int maxTextWidth, List<T> candidates, TextWidthMeter<T> meter) {
        if (maxTextWidth <= 0 || candidates == null) {
            return null;
        }
        for (T candidate : candidates) {
            if (candidate == null) continue;
            int w = meter.width(candidate);
            if (w > 0 && w <= maxTextWidth) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Backward-compatible convenience overload for 3 candidates (full, shortText, compact).
     * If none fit within maxTextWidth or maxTextWidth <= 0, returns null (safe omission).
     */
    public static String chooseStatsText(int maxTextWidth, String full, String shortText, String compact, TextWidthMeter<String> meter) {
        return chooseStatsText(maxTextWidth, List.of(full, shortText, compact), meter);
    }

    public record ResolvedText<T>(T item, int x, int y, int width, boolean visible) {}

    /**
     * Resolves candidate selection, position clamping, and safe visibility state.
     * When visible:
     * - x >= MARGIN_LEFT
     * - x + width <= panelStart(screenWidth) - 4
     * - width <= maxTextWidth(screenWidth)
     * When invisible (width <= 0 or candidates do not fit):
     * - visible is false, width is 0, item is null.
     */
    public static <T> ResolvedText<T> resolveStatsText(int screenWidth, int screenHeight, BoardRect board, int lineHeight,
                                                       List<T> candidates, TextWidthMeter<T> meter) {
        int maxW = maxTextWidth(screenWidth);
        T chosen = chooseStatsText(maxW, candidates, meter);
        if (chosen == null) {
            TextPlacement placement = computeTextPlacement(screenWidth, screenHeight, board, 0, lineHeight);
            return new ResolvedText<>(null, placement.statsX(), placement.statsY(), 0, false);
        }
        int w = meter.width(chosen);
        TextPlacement placement = computeTextPlacement(screenWidth, screenHeight, board, w, lineHeight);
        return new ResolvedText<>(chosen, placement.statsX(), placement.statsY(), w, true);
    }

    // Particle projection and visibility methods:
    public static float projectionScale(int boardWidth, int boardHeight) {
        return Math.max(boardWidth, boardHeight) / DustPattern.CANVAS_SPAN;
    }

    public static int projectX(int boardLeft, int boardWidth, float x, float scale) {
        return Math.round(boardLeft + boardWidth / 2f + x * scale);
    }

    public static int projectY(int boardTop, int boardHeight, float y, float scale) {
        return Math.round(boardTop + boardHeight / 2f - y * scale);
    }

    public static int particleDotRadius(float scale, float sizeScale) {
        return Math.max(1, Math.round(sizeScale * scale * 0.055f));
    }

    public static boolean isParticleVisible(int boardLeft, int boardTop, int boardWidth, int boardHeight,
                                           int screenX, int screenY, float localOffset, float scale, int r) {
        int margin = Math.round(localOffset * scale) + r + 2;
        return screenX >= boardLeft - margin && screenX <= boardLeft + boardWidth + margin
                && screenY >= boardTop - margin && screenY <= boardTop + boardHeight + margin;
    }
}
