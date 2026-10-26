package com.drawfirework.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/**
 * Centralized artist's workbench, parchment canvas, wooden frame, and procedural pixel-art icon theme
 * for the Firework Canvas Editor and Draft Manager GUI.
 *
 * Strictly visual-only: never modifies FreehandCanvas ARGB pixel buffers or firework output data.
 */
public final class CanvasEditorTheme {
    private CanvasEditorTheme() {}

    // --- 1. Main GUI & Panel Background Palette ---
    /** Warm dark studio backdrop */
    public static final int STUDIO_BG = 0xEE352F2A;
    /** Modal overlay backdrop for prompts */
    public static final int MODAL_BG = 0xD826211E;
    /** Right toolbar panel linen/wood backing */
    public static final int PANEL_BG = 0xE6473D35;
    /** Right toolbar outer dark border */
    public static final int PANEL_BORDER_DARK = 0xFF28211C;
    /** Right toolbar inner highlight border */
    public static final int PANEL_BORDER_LIGHT = 0xFF6B5B4E;
    /** Subtle section divider line */
    public static final int SECTION_DIVIDER = 0xFF5D4E42;

    // --- 2. Parchment Canvas & Transparency Grid (GUI-only visual backdrop) ---
    /** Primary warm parchment paper tone */
    public static final int PARCHMENT_LIGHT = 0xFFEFE3C6;
    /** Secondary warm parchment checker tone (subtle contrast so white & yellow strokes remain crisp) */
    public static final int PARCHMENT_DARK = 0xFFE2D3B1;
    /** 32x32 Canvas preview parchment checker light */
    public static final int PREVIEW_CHECKER_LIGHT = 0xFFE8D9B5;
    /** 32x32 Canvas preview parchment checker dark */
    public static final int PREVIEW_CHECKER_DARK = 0xFFD9C7A0;
    /** Night sky backdrop for Dust Firework preview */
    public static final int NIGHT_SKY_TOP = 0xFF141824;
    public static final int NIGHT_SKY_BOTTOM = 0xFF1E2436;

    // --- 3. Wooden Easel Frame Palette ---
    /** Outer dark walnut wood trim */
    public static final int WOOD_OUTER_BORDER = 0xFF422C1B;
    /** Main warm oak wood body */
    public static final int WOOD_MAIN = 0xFFB68B5B;
    /** Top/left sunlit wood bevel highlight */
    public static final int WOOD_HIGHLIGHT = 0xFFD6AD7C;
    /** Bottom/right wood shadow bevel */
    public static final int WOOD_SHADOW = 0xFF7D5937;
    /** Inner recess groove between wood frame and canvas */
    public static final int WOOD_INNER_GROOVE = 0xFF593D26;
    /** Drop shadow cast by the framed canvas onto the studio backdrop */
    public static final int FRAME_DROP_SHADOW = 0x6614100D;

    // --- 4. Typography & Status Colors ---
    /** Warm cream title text */
    public static final int TEXT_TITLE = 0xFFF7EBD2;
    /** Muted warm tan subtitle & secondary info */
    public static final int TEXT_SUBTITLE = 0xFFD6C4A8;
    /** Golden accent for active status / dirty marker */
    public static final int TEXT_ACCENT_GOLD = 0xFFFFDF70;
    /** Soft amber for bottom notes/disclaimers */
    public static final int TEXT_NOTE = 0xFFE6C896;
    /** Cyan-mint for 32x32 canvas preview status */
    public static final int TEXT_PREVIEW_CANVAS = 0xFF88E8D8;

    // --- 5. Button Category & State Colors ---
    public enum ButtonCategory {
        /** Brush, Eraser, Fill, Mode, View, History */
        TOOL_NEUTRAL(0xFF6B5847, 0xFF826C58, 0xFF4A3C30, 0xFF382E26),
        /** Drafts folder & Save Draft (soft teal-slate) */
        DRAFT_STORAGE(0xFF3B666B, 0xFF4B7F85, 0xFF264549, 0xFF2B383A),
        /** To Firework output action (warm festive amber-gold) */
        FIREWORK_OUTPUT(0xFF9E6B28, 0xFFBD8234, 0xFF694516, 0xFF423628),
        /** Cancel / Close / Delete / Danger (muted terracotta crimson) */
        DANGER_OR_EXIT(0xFF7A3B36, 0xFF964943, 0xFF4F2320, 0xFF3A2B29);

        public final int normalBg;
        public final int hoverBg;
        public final int borderDark;
        public final int disabledBg;

        ButtonCategory(int normalBg, int hoverBg, int borderDark, int disabledBg) {
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.borderDark = borderDark;
            this.disabledBg = disabledBg;
        }
    }

    /** Bright gold border for currently selected tool or color swatch */
    public static final int SELECTED_BORDER_OUTER = 0xFFFFDF59;
    public static final int SELECTED_BORDER_INNER = 0xFFFFF5C0;
    public static final int HOVER_BORDER = 0xFFE8D5B5;
    public static final int ICON_ACTIVE = 0xFFFDF6E6;
    public static final int ICON_SELECTED = 0xFFFFEA88;
    public static final int ICON_DISABLED = 0xFF7D7268;

    // --- 6. Tool & Action Icons ---
    public enum IconType {
        BRUSH,
        ERASER,
        FILL,
        UNDO,
        REDO,
        MODE_SOFT,
        MODE_CRISP,
        VIEW_DRAW,
        VIEW_CANVAS,
        VIEW_DUST,
        DRAFTS_FOLDER,
        SAVE_DRAFT,
        TO_FIREWORK,
        CLOSE
    }

    /**
     * Draws the wooden easel frame around the canvas board without overlapping a single pixel of
     * the interactive board [left, top, left + width, top + height].
     */
    public static void drawWoodenFrame(GuiGraphicsExtractor g, int left, int top, int width, int height, int frameThickness) {
        if (frameThickness <= 0 || width <= 0 || height <= 0) {
            return;
        }
        int f = frameThickness;
        int outerL = left - f;
        int outerT = top - f;
        int outerR = left + width + f;
        int outerB = top + height + f;

        // Subtle drop shadow behind bottom and right edges
        g.fill(outerL + 2, outerB, outerR + 2, outerB + 2, FRAME_DROP_SHADOW);
        g.fill(outerR, outerT + 2, outerR + 2, outerB, FRAME_DROP_SHADOW);

        // Top, bottom, left, right wood bars (never touching [left..left+width, top..top+height])
        g.fill(outerL, outerT, outerR, top, WOOD_MAIN);
        g.fill(outerL, top + height, outerR, outerB, WOOD_MAIN);
        g.fill(outerL, top, left, top + height, WOOD_MAIN);
        g.fill(left + width, top, outerR, top + height, WOOD_MAIN);

        // Outer 1px dark walnut border
        g.fill(outerL, outerT, outerR, outerT + 1, WOOD_OUTER_BORDER);
        g.fill(outerL, outerB - 1, outerR, outerB, WOOD_OUTER_BORDER);
        g.fill(outerL, outerT, outerL + 1, outerB, WOOD_OUTER_BORDER);
        g.fill(outerR - 1, outerT, outerR, outerB, WOOD_OUTER_BORDER);

        if (f >= 3) {
            // Sunlit bevel highlight inside outer border (top & left)
            g.fill(outerL + 1, outerT + 1, outerR - 1, outerT + 2, WOOD_HIGHLIGHT);
            g.fill(outerL + 1, outerT + 1, outerL + 2, outerB - 1, WOOD_HIGHLIGHT);

            // Shaded bevel inside outer border (bottom & right)
            g.fill(outerL + 1, outerB - 2, outerR - 1, outerB - 1, WOOD_SHADOW);
            g.fill(outerR - 2, outerT + 1, outerR - 1, outerB - 1, WOOD_SHADOW);
        }

        // Inner 1px recess groove right around the canvas edge
        g.fill(left - 1, top - 1, left + width + 1, top, WOOD_INNER_GROOVE);
        g.fill(left - 1, top + height, left + width + 1, top + height + 1, WOOD_INNER_GROOVE);
        g.fill(left - 1, top, left, top + height, WOOD_INNER_GROOVE);
        g.fill(left + width, top, left + width + 1, top + height, WOOD_INNER_GROOVE);

        // Corner brass/wood Joinery pins when frame is thick enough
        if (f >= 4) {
            int pin = 0xFFD3A45F;
            g.fill(outerL + 2, outerT + 2, outerL + 3, outerT + 3, pin);
            g.fill(outerR - 3, outerT + 2, outerR - 2, outerT + 3, pin);
            g.fill(outerL + 2, outerB - 3, outerL + 3, outerB - 2, pin);
            g.fill(outerR - 3, outerB - 3, outerR - 2, outerB - 2, pin);
        }
    }

    /**
     * Draws the visual-only warm parchment background with a subtle weave/checker pattern
     * so transparent areas are clearly distinguishable from pure white or pale yellow paint.
     */
    public static void drawParchmentBackdrop(GuiGraphicsExtractor g, int left, int top, int width, int height) {
        // Fill base warm parchment
        g.fill(left, top, left + width, top + height, PARCHMENT_LIGHT);

        // Draw subtle warm checker cells (cell size adapts to board size, ~8..12px)
        int cell = Math.max(6, Math.min(12, Math.min(width, height) / 16));
        for (int y = top; y < top + height; y += cell) {
            int yEnd = Math.min(top + height, y + cell);
            int row = (y - top) / cell;
            for (int x = left; x < left + width; x += cell) {
                int col = (x - left) / cell;
                if (((row + col) & 1) == 1) {
                    int xEnd = Math.min(left + width, x + cell);
                    g.fill(x, y, xEnd, yEnd, PARCHMENT_DARK);
                }
            }
        }
    }

    /**
     * Draws the right-hand artist toolbar panel background.
     */
    public static void drawToolbarPanel(GuiGraphicsExtractor g, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) return;
        g.fill(x, y, x + width, y + height, PANEL_BG);
        // Outer dark border
        g.fill(x, y, x + width, y + 1, PANEL_BORDER_DARK);
        g.fill(x, y + height - 1, x + width, y + height, PANEL_BORDER_DARK);
        g.fill(x, y, x + 1, y + height, PANEL_BORDER_DARK);
        g.fill(x + width - 1, y, x + width, y + height, PANEL_BORDER_DARK);
        // Inner top/left highlight
        if (width > 4 && height > 4) {
            g.fill(x + 1, y + 1, x + width - 1, y + 2, PANEL_BORDER_LIGHT);
            g.fill(x + 1, y + 1, x + 2, y + height - 1, PANEL_BORDER_LIGHT);
        }
    }

    /**
     * Draws a styled button shell with category colors, hover highlight, active selection border, and disabled dimming.
     */
    public static void drawStyledButton(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                        ButtonCategory category, boolean active, boolean hovered, boolean selected) {
        int bg = !active ? category.disabledBg : (hovered ? category.hoverBg : category.normalBg);
        g.fill(x, y, x + w, y + h, bg);

        if (selected && active) {
            // Double golden highlight border for selected tool/mode
            g.fill(x, y, x + w, y + 1, SELECTED_BORDER_OUTER);
            g.fill(x, y + h - 1, x + w, y + h, SELECTED_BORDER_OUTER);
            g.fill(x, y, x + 1, y + h, SELECTED_BORDER_OUTER);
            g.fill(x + w - 1, y, x + w, y + h, SELECTED_BORDER_OUTER);
            if (w >= 6 && h >= 6) {
                g.fill(x + 1, y + 1, x + w - 1, y + 2, SELECTED_BORDER_INNER);
            }
        } else if (hovered && active) {
            g.fill(x, y, x + w, y + 1, HOVER_BORDER);
            g.fill(x, y + h - 1, x + w, y + h, HOVER_BORDER);
            g.fill(x, y, x + 1, y + h, HOVER_BORDER);
            g.fill(x + w - 1, y, x + w, y + h, HOVER_BORDER);
        } else {
            int b = category.borderDark;
            g.fill(x, y, x + w, y + 1, b);
            g.fill(x, y + h - 1, x + w, y + h, b);
            g.fill(x, y, x + 1, y + h, b);
            g.fill(x + w - 1, y, x + w, y + h, b);
            if (active && w >= 4 && h >= 4) {
                // Subtle top highlight
                g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x33FFFFFF);
            }
        }
    }

    /**
     * Draws a color palette swatch cell with bevels and an unmistakable selection frame.
     */
    public static void drawColorSwatch(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                       int swatchArgb, boolean hovered, boolean selected) {
        // Outer frame
        int outer = selected ? SELECTED_BORDER_OUTER : (hovered ? HOVER_BORDER : WOOD_OUTER_BORDER);
        g.fill(x, y, x + w, y + h, outer);

        int inset = selected ? 2 : 1;
        if (w > inset * 2 && h > inset * 2) {
            g.fill(x + inset, y + inset, x + w - inset, y + h - inset, swatchArgb);
            // Subtle inner sheen on top edge
            g.fill(x + inset, y + inset, x + w - inset, y + inset + 1, 0x44FFFFFF);
            g.fill(x + inset, y + h - inset - 1, x + w - inset, y + h - inset, 0x44000000);
        }

        if (selected && w >= 8 && h >= 8) {
            // Corner indicator pip
            int pipColor = (swatchArgb == 0xFFFFFFFF || swatchArgb == 0xFFFFDF40) ? 0xFF28211C : 0xFFFFFFFF;
            g.fill(x + w - 4, y + 2, x + w - 2, y + 4, pipColor);
        }
    }

    /**
     * Draws a crisp 9x9 procedural pixel-art icon centered at (cx, cy).
     */
    public static void drawIcon(GuiGraphicsExtractor g, IconType icon, int cx, int cy, boolean active, boolean selected) {
        int c = !active ? ICON_DISABLED : (selected ? ICON_SELECTED : ICON_ACTIVE);
        int x0 = cx - 4;
        int y0 = cy - 4;

        switch (icon) {
            case BRUSH -> {
                // Diagonal artist brush: handle bottom-right to ferrule & tip top-left
                int tip = active ? 0xFFFF6B5B : ICON_DISABLED;
                g.fill(x0 + 1, y0 + 1, x0 + 4, y0 + 3, tip);
                g.fill(x0 + 2, y0 + 3, x0 + 4, y0 + 4, tip);
                g.fill(x0 + 3, y0 + 3, x0 + 5, y0 + 5, 0xFFD6C4A8); // ferrule
                g.fill(x0 + 4, y0 + 4, x0 + 6, y0 + 6, c);
                g.fill(x0 + 5, y0 + 5, x0 + 7, y0 + 7, c);
                g.fill(x0 + 6, y0 + 6, x0 + 8, y0 + 8, c);
            }
            case ERASER -> {
                // Two-tone block eraser
                int pink = active ? 0xFFF29B9B : ICON_DISABLED;
                int sleeve = active ? 0xFFD9D2C5 : ICON_DISABLED;
                g.fill(x0 + 1, y0 + 4, x0 + 5, y0 + 8, pink);
                g.fill(x0 + 3, y0 + 2, x0 + 8, y0 + 6, sleeve);
                g.fill(x0 + 1, y0 + 4, x0 + 5, y0 + 5, c);
            }
            case FILL -> {
                // Paint bucket with pouring droplet
                g.fill(x0 + 2, y0 + 2, x0 + 7, y0 + 3, c); // handle arch
                g.fill(x0 + 1, y0 + 3, x0 + 7, y0 + 7, c); // bucket body
                int drop = active ? 0xFF55C8FF : ICON_DISABLED;
                g.fill(x0 + 2, y0 + 4, x0 + 6, y0 + 6, drop); // liquid inside
                g.fill(x0 + 7, y0 + 5, x0 + 8, y0 + 8, drop); // pouring drop
            }
            case UNDO -> {
                // Curved left arrow
                g.fill(x0 + 1, y0 + 3, x0 + 6, y0 + 4, c);
                g.fill(x0 + 2, y0 + 2, x0 + 4, y0 + 3, c);
                g.fill(x0 + 2, y0 + 4, x0 + 4, y0 + 5, c);
                g.fill(x0 + 6, y0 + 3, x0 + 8, y0 + 7, c);
                g.fill(x0 + 4, y0 + 6, x0 + 7, y0 + 7, c);
            }
            case REDO -> {
                // Curved right arrow
                g.fill(x0 + 3, y0 + 3, x0 + 8, y0 + 4, c);
                g.fill(x0 + 5, y0 + 2, x0 + 7, y0 + 3, c);
                g.fill(x0 + 5, y0 + 4, x0 + 7, y0 + 5, c);
                g.fill(x0 + 1, y0 + 3, x0 + 3, y0 + 7, c);
                g.fill(x0 + 2, y0 + 6, x0 + 5, y0 + 7, c);
            }
            case MODE_SOFT -> {
                // Soft concentric water-drop / smooth blend icon
                int mid = active ? 0xFFA8D8EA : ICON_DISABLED;
                g.fill(x0 + 2, y0 + 2, x0 + 7, y0 + 7, mid);
                g.fill(x0 + 3, y0 + 3, x0 + 6, y0 + 6, c);
            }
            case MODE_CRISP -> {
                // Crisp sharp diamond / pixel edge icon
                g.fill(x0 + 4, y0 + 1, x0 + 5, y0 + 8, c);
                g.fill(x0 + 1, y0 + 4, x0 + 8, y0 + 5, c);
                g.fill(x0 + 2, y0 + 2, x0 + 7, y0 + 7, c);
                g.fill(x0 + 3, y0 + 3, x0 + 6, y0 + 6, PANEL_BG);
            }
            case VIEW_DRAW -> {
                // Canvas easel frame icon
                g.fill(x0 + 1, y0 + 1, x0 + 8, y0 + 7, c);
                g.fill(x0 + 2, y0 + 2, x0 + 7, y0 + 6, PARCHMENT_LIGHT);
                g.fill(x0 + 2, y0 + 7, x0 + 3, y0 + 8, c);
                g.fill(x0 + 6, y0 + 7, x0 + 7, y0 + 8, c);
            }
            case VIEW_CANVAS -> {
                // 2x2 pixel grid icon
                g.fill(x0 + 1, y0 + 1, x0 + 4, y0 + 4, c);
                g.fill(x0 + 5, y0 + 1, x0 + 8, y0 + 4, active ? 0xFF88E8D8 : ICON_DISABLED);
                g.fill(x0 + 1, y0 + 5, x0 + 4, y0 + 8, active ? 0xFF88E8D8 : ICON_DISABLED);
                g.fill(x0 + 5, y0 + 5, x0 + 8, y0 + 8, c);
            }
            case VIEW_DUST -> {
                // Sparkle / particle burst eye icon
                int spark = active ? 0xFFFFDF59 : ICON_DISABLED;
                g.fill(x0 + 4, y0 + 1, x0 + 5, y0 + 8, spark);
                g.fill(x0 + 1, y0 + 4, x0 + 8, y0 + 5, spark);
                g.fill(x0 + 2, y0 + 2, x0 + 3, y0 + 3, c);
                g.fill(x0 + 6, y0 + 2, x0 + 7, y0 + 3, c);
                g.fill(x0 + 2, y0 + 6, x0 + 3, y0 + 7, c);
                g.fill(x0 + 6, y0 + 6, x0 + 7, y0 + 7, c);
            }
            case DRAFTS_FOLDER -> {
                // Open folder tab icon
                int folder = active ? 0xFFE6C275 : ICON_DISABLED;
                g.fill(x0 + 1, y0 + 2, x0 + 4, y0 + 3, folder); // tab
                g.fill(x0 + 1, y0 + 3, x0 + 8, y0 + 8, folder); // body
                g.fill(x0 + 2, y0 + 4, x0 + 7, y0 + 5, 0xFFF7EBD2); // paper sheet peeking out
            }
            case SAVE_DRAFT -> {
                // Floppy disk / Save Draft icon
                g.fill(x0 + 1, y0 + 1, x0 + 8, y0 + 8, c);
                g.fill(x0 + 3, y0 + 1, x0 + 6, y0 + 4, PANEL_BG); // metal shutter
                int label = active ? 0xFF88E8D8 : ICON_DISABLED;
                g.fill(x0 + 2, y0 + 5, x0 + 7, y0 + 7, label); // bottom label
            }
            case TO_FIREWORK -> {
                // Firework rocket & star burst icon
                int star = active ? 0xFFFFDF40 : ICON_DISABLED;
                int body = active ? 0xFFFF6050 : ICON_DISABLED;
                g.fill(x0 + 4, y0 + 0, x0 + 5, y0 + 2, body); // cone tip
                g.fill(x0 + 3, y0 + 2, x0 + 6, y0 + 6, c);    // rocket tube
                g.fill(x0 + 3, y0 + 3, x0 + 6, y0 + 4, body); // red stripe
                g.fill(x0 + 4, y0 + 6, x0 + 5, y0 + 8, star); // spark tail
                g.fill(x0 + 1, y0 + 1, x0 + 2, y0 + 2, star); // left star
                g.fill(x0 + 7, y0 + 1, x0 + 8, y0 + 2, star); // right star
            }
            case CLOSE -> {
                // Crisp X / Exit icon
                g.fill(x0 + 1, y0 + 1, x0 + 3, y0 + 3, c);
                g.fill(x0 + 6, y0 + 1, x0 + 8, y0 + 3, c);
                g.fill(x0 + 3, y0 + 3, x0 + 6, y0 + 6, c);
                g.fill(x0 + 1, y0 + 6, x0 + 3, y0 + 8, c);
                g.fill(x0 + 6, y0 + 6, x0 + 8, y0 + 8, c);
            }
        }
    }

    /**
     * Helper to render a themed button with an icon and optional short text when width permits.
     */
    public static void drawIconAndOptionalLabel(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h,
                                                IconType icon, Component shortLabel, boolean active, boolean selected) {
        int textColor = !active ? ICON_DISABLED : (selected ? ICON_SELECTED : ICON_ACTIVE);
        if (shortLabel == null || shortLabel.getString().isEmpty()) {
            drawIcon(g, icon, x + w / 2, y + h / 2, active, selected);
            return;
        }
        int labelW = font.width(shortLabel);
        int totalNeeded = 11 + labelW + 4;
        if (w >= totalNeeded) {
            int startX = x + (w - (11 + labelW)) / 2;
            drawIcon(g, icon, startX + 4, y + h / 2, active, selected);
            g.text(font, shortLabel, startX + 11, y + (h - font.lineHeight) / 2 + 1, textColor, true);
        } else {
            // Compact window: fall back cleanly to centered icon
            drawIcon(g, icon, x + w / 2, y + h / 2, active, selected);
        }
    }
}
