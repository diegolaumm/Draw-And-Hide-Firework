package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.draft.Draft;
import com.drawfirework.draft.DraftMetadata;
import com.drawfirework.draft.DraftStorage;
import com.drawfirework.editor.FreehandCanvas;
import com.drawfirework.editor.FreehandResampler;
import com.drawfirework.network.EditorCanvasPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Artist's Workbench & Parchment Canvas Editor Screen.
 * Uploads a single session texture only when the editable raster changes;
 * visual parchment backdrop and wooden frame never alter FreehandCanvas ARGB pixels.
 */
public final class CanvasEditorScreen extends Screen {
    public enum ViewMode { DRAW, CANVAS_32, FIREWORK }

    private static final int[] COLORS = {
            0xFFFF4040, 0xFF4080FF, 0xFFFFDF40, 0xFF40CF70,
            0xFFFF9030, 0xFFB060FF, 0xFFFFFFFF, 0xFF181818
    };
    private static final String[] NAMES = {
            "red", "blue", "yellow", "green",
            "orange", "purple", "white", "black"
    };

    private final UUID token;
    private final FreehandCanvas model;
    private final FreehandInput input;
    private final Object world;
    private final Identifier textureId;
    private DynamicTexture texture;
    private long uploadedRevision = -1;
    private CanvasEditorLayout.BoardRect board;
    private CanvasEditorLayout.ToolbarGrouping toolbar;
    private int left, top, boardWidth, boardHeight;
    private int color = COLORS[0];
    private int lastPaintColor = COLORS[0];
    private String lastColorName = NAMES[0];
    private String tool = NAMES[0];

    private final CanvasPreview preview = new CanvasPreview();
    private FreehandResampler.Mode mode = FreehandResampler.Mode.SOFT;
    private ViewMode viewMode = ViewMode.DRAW;
    private Button undoButton;
    private Button redoButton;
    private Button modeButton;
    private Button viewButton;

    // Draft persistence state
    private UUID currentDraftId;
    private String currentDraftName = "Untitled";
    private long savedRevision = -1;

    public CanvasEditorScreen(UUID token, CanvasData canvas) {
        super(label("title"));
        this.token = token;
        this.model = new FreehandCanvas(canvas);
        this.world = minecraft.level;
        this.input = new FreehandInput(model);
        this.textureId = Identifier.fromNamespaceAndPath("drawfirework", "editor/" + token);
        this.savedRevision = this.model.revision();
    }

    public UUID getCurrentDraftId() {
        return currentDraftId;
    }

    public int getColor() {
        return color;
    }

    public String getTool() {
        return tool;
    }

    public FreehandInput.Tool getActiveInputTool() {
        return input.tool();
    }

    public void triggerSelectBrush() {
        selectBrushTool();
    }

    public void triggerSelectEraser() {
        selectEraserTool();
    }

    public void triggerSelectFill() {
        selectFillTool();
    }

    public void triggerSelectColor(int colorIndex) {
        if (colorIndex >= 0 && colorIndex < COLORS.length) {
            color = COLORS[colorIndex];
            lastPaintColor = COLORS[colorIndex];
            lastColorName = NAMES[colorIndex];
            if (input.tool() != FreehandInput.Tool.FILL) {
                tool = NAMES[colorIndex];
                input.setTool(FreehandInput.Tool.BRUSH);
            }
            input.reset();
        }
    }

    public String getCurrentDraftName() {
        return currentDraftName;
    }

    public void setCurrentDraftName(String name) {
        this.currentDraftName = name;
    }

    public void clearCurrentDraftId() {
        this.currentDraftId = null;
    }

    public FreehandCanvas getModel() {
        return model;
    }

    public boolean isDraftDirty() {
        return model.revision() != savedRevision;
    }

    public void markSaved(UUID id, String name) {
        this.currentDraftId = id;
        this.currentDraftName = name;
        this.savedRevision = model.revision();
    }

    public void loadDraftData(Draft draft) {
        if (!model.canLoadLosslessly(draft.pixels())) {
            throw new IllegalArgumentException("Cannot load draft: draft contains high-resolution details incompatible with a "
                    + model.width() + "x" + model.height() + " canvas");
        }
        model.loadDraft(draft.pixels());
        markSaved(draft.id(), draft.name());
        syncHistoryButtons();
    }

    private static Component label(String key, Object... args) {
        return Component.translatable("editor.drawfirework." + key, args);
    }

    private void syncHistoryButtons() {
        if (undoButton != null) undoButton.active = model.canUndo();
        if (redoButton != null) redoButton.active = model.canRedo();
    }

    private void selectBrushTool() {
        if (color == 0) {
            color = lastPaintColor;
        }
        tool = lastColorName;
        input.setTool(FreehandInput.Tool.BRUSH);
        input.reset();
    }

    private void selectEraserTool() {
        color = 0;
        tool = "eraser";
        input.setTool(FreehandInput.Tool.BRUSH);
        input.reset();
    }

    private void selectFillTool() {
        tool = (color == 0) ? "eraser" : "fill";
        input.setTool(FreehandInput.Tool.FILL);
        input.reset();
    }

    private void toggleResampleMode() {
        mode = (mode == FreehandResampler.Mode.SOFT) ? FreehandResampler.Mode.CRISP : FreehandResampler.Mode.SOFT;
        if (modeButton != null) {
            Component msg = label(mode == FreehandResampler.Mode.SOFT ? "mode_soft" : "mode_crisp");
            modeButton.setMessage(msg);
            modeButton.setTooltip(Tooltip.create(label("tooltip_mode", msg)));
        }
    }

    private void cycleViewMode() {
        viewMode = switch (viewMode) {
            case DRAW -> ViewMode.CANVAS_32;
            case CANVAS_32 -> ViewMode.FIREWORK;
            case FIREWORK -> ViewMode.DRAW;
        };
        if (viewButton != null) {
            Component msg = label(viewLabelKey());
            viewButton.setMessage(msg);
            viewButton.setTooltip(Tooltip.create(label("tooltip_view", msg)));
        }
    }

    @Override
    protected void init() {
        input.reset();
        int maxW = CanvasEditorLayout.maxTextWidth(width);
        int disclaimerLines = maxW > 0 ? font.split(label("preview_dust_disclaimer"), maxW).size() : 0;
        int textHeightNeeded = Math.max(disclaimerLines, 2) * font.lineHeight;

        board = CanvasEditorLayout.computeBoard(width, height, model.width(), model.height(), textHeightNeeded);
        boardWidth = board.width();
        boardHeight = board.height();
        left = board.left();
        top = board.top();

        if (texture == null) {
            texture = new DynamicTexture("drawfirework editor", model.width(), model.height(), true);
            minecraft.getTextureManager().register(textureId, texture);
            uploadedRevision = -1;
        }

        toolbar = CanvasEditorLayout.computeToolbar(width, height);
        int start = toolbar.innerX();
        int innerW = toolbar.innerWidth();
        int btnH = toolbar.buttonHeight();

        // 1. Artist Color Palette: 8 swatches in 2 compact rows of 4
        int swatchGap = 2;
        int swatchW = Math.max(12, (innerW - swatchGap * 3) / 4);
        for (int i = 0; i < COLORS.length; i++) {
            final int index = i;
            int col = i % 4;
            int sx = start + col * (swatchW + swatchGap);
            if (col == 3) {
                sx = start + innerW - swatchW;
            }
            int sy = (i < 4) ? toolbar.swatchRow1Y() : toolbar.swatchRow2Y();
            Component colorName = label(NAMES[i]);
            SwatchButton swatch = new SwatchButton(
                    sx, sy, swatchW, btnH,
                    colorName,
                    COLORS[i],
                    () -> lastPaintColor == COLORS[index] && color != 0,
                    b -> {
                        color = COLORS[index];
                        lastPaintColor = COLORS[index];
                        lastColorName = NAMES[index];
                        if (input.tool() != FreehandInput.Tool.FILL) {
                            tool = NAMES[index];
                            input.setTool(FreehandInput.Tool.BRUSH);
                        }
                        input.reset();
                    }
            );
            swatch.setTooltip(Tooltip.create(label("tooltip_color", colorName)));
            swatch.setTooltipDelay(Duration.ofMillis(150));
            addRenderableWidget(swatch);
        }

        // 2. Brush Size Slider
        AbstractSliderButton brushSlider = new AbstractSliderButton(
                start, toolbar.sliderY(), innerW, btnH,
                label("brush", model.diameter()), (model.diameter() - 1) / 63.0) {
            @Override
            protected void updateMessage() {
                setMessage(label("brush", model.diameter()));
            }

            @Override
            protected void applyValue() {
                input.reset();
                model.setDiameter(1 + (int) Math.round(value * 63));
            }
        };
        brushSlider.setTooltip(Tooltip.create(label("tooltip_brush_size")));
        brushSlider.setTooltipDelay(Duration.ofMillis(200));
        addRenderableWidget(brushSlider);

        // 3. Tools Row: Brush, Eraser, Fill (3 icon buttons)
        int thirdGap = 2;
        int thirdW = Math.max(16, (innerW - thirdGap * 2) / 3);
        int tX0 = start;
        int tX1 = start + thirdW + thirdGap;
        int tX2 = start + innerW - thirdW;

        ThemedToolButton brushBtn = new ThemedToolButton(
                tX0, toolbar.toolsRowY(), thirdW, btnH,
                label("brush_tool"),
                () -> CanvasEditorTheme.IconType.BRUSH,
                () -> null,
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> input.tool() == FreehandInput.Tool.BRUSH && color != 0,
                b -> selectBrushTool()
        );
        brushBtn.setTooltip(Tooltip.create(label("tooltip_brush")));
        brushBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(brushBtn);

        ThemedToolButton eraserBtn = new ThemedToolButton(
                tX1, toolbar.toolsRowY(), thirdW, btnH,
                label("eraser"),
                () -> CanvasEditorTheme.IconType.ERASER,
                () -> null,
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> input.tool() == FreehandInput.Tool.BRUSH && color == 0,
                b -> selectEraserTool()
        );
        eraserBtn.setTooltip(Tooltip.create(label("tooltip_eraser")));
        eraserBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(eraserBtn);

        ThemedToolButton fillBtn = new ThemedToolButton(
                tX2, toolbar.toolsRowY(), thirdW, btnH,
                label("fill"),
                () -> CanvasEditorTheme.IconType.FILL,
                () -> null,
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> input.tool() == FreehandInput.Tool.FILL,
                b -> selectFillTool()
        );
        fillBtn.setTooltip(Tooltip.create(label("tooltip_fill")));
        fillBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(fillBtn);

        // 4. History Row: Undo & Redo (2 icon buttons)
        int halfGap = 2;
        int halfW = Math.max(20, (innerW - halfGap) / 2);
        int hX0 = start;
        int hX1 = start + innerW - halfW;

        undoButton = new ThemedToolButton(
                hX0, toolbar.historyRowY(), halfW, btnH,
                label("undo"),
                () -> CanvasEditorTheme.IconType.UNDO,
                () -> null,
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> false,
                b -> {
                    input.reset();
                    model.undo();
                    syncHistoryButtons();
                }
        );
        undoButton.setTooltip(Tooltip.create(label("tooltip_undo")));
        undoButton.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(undoButton);

        redoButton = new ThemedToolButton(
                hX1, toolbar.historyRowY(), halfW, btnH,
                label("redo"),
                () -> CanvasEditorTheme.IconType.REDO,
                () -> null,
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> false,
                b -> {
                    input.reset();
                    model.redo();
                    syncHistoryButtons();
                }
        );
        redoButton.setTooltip(Tooltip.create(label("tooltip_redo")));
        redoButton.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(redoButton);

        // 5. Conversion Mode & View Preview Row (Icon + compact state text)
        Component initialModeLabel = label(mode == FreehandResampler.Mode.SOFT ? "mode_soft" : "mode_crisp");
        modeButton = new ThemedToolButton(
                hX0, toolbar.viewRowY(), halfW, btnH,
                initialModeLabel,
                () -> mode == FreehandResampler.Mode.SOFT
                        ? CanvasEditorTheme.IconType.MODE_SOFT
                        : CanvasEditorTheme.IconType.MODE_CRISP,
                () -> modeButton.getMessage(),
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> mode == FreehandResampler.Mode.CRISP,
                b -> toggleResampleMode()
        );
        modeButton.setTooltip(Tooltip.create(label("tooltip_mode", initialModeLabel)));
        modeButton.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(modeButton);

        Component initialViewLabel = label(viewLabelKey());
        viewButton = new ThemedToolButton(
                hX1, toolbar.viewRowY(), halfW, btnH,
                initialViewLabel,
                () -> switch (viewMode) {
                    case DRAW -> CanvasEditorTheme.IconType.VIEW_DRAW;
                    case CANVAS_32 -> CanvasEditorTheme.IconType.VIEW_CANVAS;
                    case FIREWORK -> CanvasEditorTheme.IconType.VIEW_DUST;
                },
                () -> viewButton.getMessage(),
                CanvasEditorTheme.ButtonCategory.TOOL_NEUTRAL,
                () -> viewMode != ViewMode.DRAW,
                b -> cycleViewMode()
        );
        viewButton.setTooltip(Tooltip.create(label("tooltip_view", initialViewLabel)));
        viewButton.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(viewButton);

        // 6. Drafts Row: Drafts Manager (Folder) & Quick Save Draft (Disk)
        ThemedToolButton draftsBtn = new ThemedToolButton(
                hX0, toolbar.draftsRowY(), halfW, btnH,
                label("drafts"),
                () -> CanvasEditorTheme.IconType.DRAFTS_FOLDER,
                () -> label("drafts_short"),
                CanvasEditorTheme.ButtonCategory.DRAFT_STORAGE,
                () -> false,
                b -> {
                    input.reset();
                    try {
                        DraftStorage storage = ClientDraftStorage.getInstance();
                        minecraft.gui.setScreen(new DraftManagerScreen(this, storage));
                    } catch (Exception e) {
                        if (minecraft != null && minecraft.player != null) {
                            minecraft.player.sendSystemMessage(label("draft_error", e.getMessage()));
                        }
                    }
                }
        );
        draftsBtn.setTooltip(Tooltip.create(label("tooltip_drafts")));
        draftsBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(draftsBtn);

        ThemedToolButton saveDraftBtn = new ThemedToolButton(
                hX1, toolbar.draftsRowY(), halfW, btnH,
                label("save_draft"),
                () -> CanvasEditorTheme.IconType.SAVE_DRAFT,
                () -> label("save_draft_short"),
                CanvasEditorTheme.ButtonCategory.DRAFT_STORAGE,
                this::isDraftDirty,
                b -> {
                    input.reset();
                    quickSaveDraft();
                }
        );
        saveDraftBtn.setTooltip(Tooltip.create(label("tooltip_save_draft")));
        saveDraftBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(saveDraftBtn);

        // 7. Output & Exit Row: Close (compact icon button) & To Firework (prominent golden button)
        int closeW = Math.max(20, Math.min(26, innerW / 4));
        int fireworkW = Math.max(24, innerW - closeW - halfGap);

        ThemedToolButton cancelBtn = new ThemedToolButton(
                start, toolbar.actionsRowY(), closeW, btnH,
                label("cancel"),
                () -> CanvasEditorTheme.IconType.CLOSE,
                () -> null,
                CanvasEditorTheme.ButtonCategory.DANGER_OR_EXIT,
                () -> false,
                b -> onClose()
        );
        cancelBtn.setTooltip(Tooltip.create(label("tooltip_cancel")));
        cancelBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(cancelBtn);

        ThemedToolButton saveFireworkBtn = new ThemedToolButton(
                start + closeW + halfGap, toolbar.actionsRowY(), fireworkW, btnH,
                label("save_firework"),
                () -> CanvasEditorTheme.IconType.TO_FIREWORK,
                () -> label("save_firework_short"),
                CanvasEditorTheme.ButtonCategory.FIREWORK_OUTPUT,
                () -> false,
                b -> save()
        );
        saveFireworkBtn.setTooltip(Tooltip.create(label("tooltip_save_firework")));
        saveFireworkBtn.setTooltipDelay(Duration.ofMillis(150));
        addRenderableWidget(saveFireworkBtn);

        syncHistoryButtons();
    }

    public void saveAndThen(Screen callingScreen, Runnable onSuccess, Runnable onCancel) {
        if (currentDraftId != null) {
            try {
                DraftStorage storage = ClientDraftStorage.getInstance();
                Draft draft = new Draft(
                        new DraftMetadata(currentDraftId, currentDraftName, System.currentTimeMillis(), System.currentTimeMillis(), 256, 256, 1),
                        model.toDraft256Pixels()
                );
                storage.saveDraft(draft);
                markSaved(currentDraftId, currentDraftName);
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.sendSystemMessage(label("draft_saved", currentDraftName));
                }
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } catch (Exception e) {
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.sendSystemMessage(label("draft_error", e.getMessage()));
                }
                if (minecraft != null && callingScreen != null) {
                    minecraft.gui.setScreen(callingScreen);
                }
            }
        } else {
            if (minecraft != null) {
                minecraft.gui.setScreen(new DraftNamePromptScreen(
                        callingScreen != null ? callingScreen : this,
                        label("draft_save_as_title"),
                        label("save_draft"),
                        currentDraftName.equals("Untitled") ? "Untitled" : currentDraftName,
                        name -> {
                            try {
                                DraftStorage storage = ClientDraftStorage.getInstance();
                                Draft draft = storage.createNewDraft(name, 256, 256, model.toDraft256Pixels());
                                markSaved(draft.id(), draft.name());
                                if (minecraft.player != null) {
                                    minecraft.player.sendSystemMessage(label("draft_saved", draft.name()));
                                }
                                if (onSuccess != null) {
                                    onSuccess.run();
                                }
                            } catch (Exception e) {
                                if (minecraft.player != null) {
                                    minecraft.player.sendSystemMessage(label("draft_error", e.getMessage()));
                                }
                                if (callingScreen != null) {
                                    minecraft.gui.setScreen(callingScreen);
                                }
                            }
                        },
                        onCancel
                ));
            }
        }
    }

    public void quickSaveDraft() {
        saveAndThen(this, () -> {
            if (minecraft != null) {
                minecraft.gui.setScreen(this);
            }
        }, null);
    }

    private String viewLabelKey() {
        return switch (viewMode) {
            case DRAW -> "view_draw";
            case CANVAS_32 -> "view_canvas";
            case FIREWORK -> "view_dust";
        };
    }

    private void drawCanvasPreview(GuiGraphicsExtractor g) {
        var output = preview.get(model, mode);
        CanvasData canvas = output.canvas();
        g.fill(left, top, left + boardWidth, top + boardHeight, CanvasEditorTheme.PREVIEW_CHECKER_DARK);
        int cw = canvas.width(), ch = canvas.height();
        for (int cy = 0; cy < ch; cy++) {
            int y0 = top + cy * boardHeight / ch;
            int y1 = top + (cy + 1) * boardHeight / ch;
            for (int cx = 0; cx < cw; cx++) {
                int x0 = left + cx * boardWidth / cw;
                int x1 = left + (cx + 1) * boardWidth / cw;
                int p = canvas.pixel(cx, cy);
                int bg = ((cx + cy) & 1) == 0
                        ? CanvasEditorTheme.PREVIEW_CHECKER_LIGHT
                        : CanvasEditorTheme.PREVIEW_CHECKER_DARK;
                g.fill(x0, y0, x1, y1, bg);
                if ((p >>> 24) > 0) {
                    g.fill(x0, y0, x1, y1, p);
                }
            }
        }
    }

    private void drawParticlePreview(GuiGraphicsExtractor g) {
        var output = preview.get(model, mode);
        g.fillGradient(left, top, left + boardWidth, top + boardHeight,
                CanvasEditorTheme.NIGHT_SKY_TOP, CanvasEditorTheme.NIGHT_SKY_BOTTOM);
        float scale = CanvasEditorLayout.projectionScale(boardWidth, boardHeight);
        var preset = DustStyle.BLOOM_130;
        int copies = preset.particlesPerPoint();
        var points = output.pattern().points();
        for (int i = 0; i < points.size(); i++) {
            var pt = points.get(i);
            int rgb = 0xFF000000 | pt.rgb();
            int r = CanvasEditorLayout.particleDotRadius(scale, pt.sizeScale());
            for (int k = 0; k < copies; k++) {
                var s = preset.sample(i, k, pt.x(), pt.y());
                int screenX = CanvasEditorLayout.projectX(left, boardWidth, s.x(), scale);
                int screenY = CanvasEditorLayout.projectY(top, boardHeight, s.y(), scale);
                if (CanvasEditorLayout.isParticleVisible(left, top, boardWidth, boardHeight, screenX, screenY, preset.localOffset(), scale, r)) {
                    g.fill(screenX - r, screenY - r, screenX + r + 1, screenY + r + 1, rgb);
                }
            }
        }
    }

    private void save() {
        if (minecraft.player == null || minecraft.level != world) { closeWithoutPrompt(); return; }
        if (!ClientPlayNetworking.canSend(EditorCanvasPayload.SAVE)) {
            minecraft.player.sendSystemMessage(label("unavailable")); return;
        }
        ClientPlayNetworking.send(new EditorCanvasPayload(token, preview.get(model, mode).canvas()));
        minecraft.player.sendSystemMessage(label("submitted"));
        closeWithoutPrompt();
    }

    @Override
    public void onClose() {
        if (isDraftDirty()) {
            if (minecraft != null) {
                minecraft.gui.setScreen(new UnsavedChangesScreen(
                        this,
                        () -> saveAndThen(this, this::closeWithoutPrompt, null),
                        this::closeWithoutPrompt
                ));
            }
        } else {
            closeWithoutPrompt();
        }
    }

    private void closeWithoutPrompt() {
        super.onClose();
    }

    @Override
    public void tick() {
        if (minecraft.level != world || minecraft.player == null) closeWithoutPrompt();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void removed() {
        input.reset();
        if (texture != null) {
            minecraft.getTextureManager().release(textureId);
            texture = null;
        }
        super.removed();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        syncHistoryButtons();

        // 1. Warm artist studio backdrop
        g.fill(0, 0, width, height, CanvasEditorTheme.STUDIO_BG);

        // 2. Right-hand artist toolbar panel & subtle group dividers
        if (toolbar != null) {
            CanvasEditorTheme.drawToolbarPanel(g, toolbar.panelX(), toolbar.panelY(), toolbar.panelWidth(), toolbar.panelHeight());
            if (toolbar.toolsRowY() > toolbar.sliderY() + toolbar.buttonHeight() + 1) {
                int divY = toolbar.toolsRowY() - 2;
                g.fill(toolbar.innerX() + 2, divY, toolbar.innerX() + toolbar.innerWidth() - 2, divY + 1, CanvasEditorTheme.SECTION_DIVIDER);
            }
            if (toolbar.viewRowY() > toolbar.historyRowY() + toolbar.buttonHeight() + 1) {
                int divY = toolbar.viewRowY() - 2;
                g.fill(toolbar.innerX() + 2, divY, toolbar.innerX() + toolbar.innerWidth() - 2, divY + 1, CanvasEditorTheme.SECTION_DIVIDER);
            }
            if (toolbar.draftsRowY() > toolbar.viewRowY() + toolbar.buttonHeight() + 1) {
                int divY = toolbar.draftsRowY() - 2;
                g.fill(toolbar.innerX() + 2, divY, toolbar.innerX() + toolbar.innerWidth() - 2, divY + 1, CanvasEditorTheme.SECTION_DIVIDER);
            }
        }

        // 3. Header title & draft dirty state
        int centerHeaderX = Math.max(width / 3, (CanvasEditorLayout.panelStart(width) + CanvasEditorLayout.MARGIN_LEFT) / 2);
        String titleStr = title.getString() + " — " + currentDraftName + (isDraftDirty() ? " *" : "");
        int titleColor = isDraftDirty() ? CanvasEditorTheme.TEXT_ACCENT_GOLD : CanvasEditorTheme.TEXT_TITLE;
        g.centeredText(font, titleStr, centerHeaderX, 7, titleColor);
        g.centeredText(font, label("size", model.outputWidth(), model.outputHeight()), centerHeaderX, 18, CanvasEditorTheme.TEXT_SUBTITLE);

        // 4. Wooden Easel Frame (strictly outside [left..left+boardWidth, top..top+boardHeight])
        var b = board != null ? board : CanvasEditorLayout.computeBoard(width, height, model.width(), model.height(), 18);
        int frameThickness = CanvasEditorLayout.frameThickness(width, height, b);
        CanvasEditorTheme.drawWoodenFrame(g, left, top, boardWidth, boardHeight, frameThickness);

        // 5. Canvas / Preview content
        List<Component> candidates;
        int statusColor;
        if (viewMode == ViewMode.DRAW) {
            CanvasEditorTheme.drawParchmentBackdrop(g, left, top, boardWidth, boardHeight);
            if (texture != null) {
                if (uploadedRevision != model.revision()) {
                    var image = texture.getPixels();
                    for (int y = 0; y < model.height(); y++) {
                        for (int x = 0; x < model.width(); x++) {
                            image.setPixel(x, y, model.pixel(x, y));
                        }
                    }
                    texture.upload();
                    uploadedRevision = model.revision();
                }
                g.blit(textureId, left, top, left + boardWidth, top + boardHeight, 0f, 1f, 0f, 1f);
            }
            candidates = List.of(label("tool", label(tool)), label(tool));
            statusColor = CanvasEditorTheme.TEXT_TITLE;
        } else if (viewMode == ViewMode.CANVAS_32) {
            drawCanvasPreview(g);
            var out = preview.get(model, mode);
            var modeName = label(mode == FreehandResampler.Mode.SOFT ? "mode_soft_name" : "mode_crisp_name");
            candidates = List.of(
                    label("preview_canvas", out.canvas().width(), out.canvas().height(), modeName),
                    label("preview_canvas_short", out.canvas().width(), out.canvas().height(), modeName),
                    Component.literal(out.canvas().width() + "x" + out.canvas().height())
            );
            statusColor = CanvasEditorTheme.TEXT_PREVIEW_CANVAS;
        } else {
            drawParticlePreview(g);
            var out = preview.get(model, mode);
            int copies = DustStyle.BLOOM_130.particlesPerPoint();
            int sampleCount = out.pattern().points().size();
            int totalParticles = sampleCount * copies;
            candidates = List.of(
                    label("preview_dust", totalParticles, sampleCount),
                    label("preview_dust_short", totalParticles, sampleCount),
                    label("preview_dust_compact", totalParticles, sampleCount),
                    label("preview_dust_minimal", totalParticles),
                    Component.literal(String.valueOf(totalParticles))
            );
            statusColor = CanvasEditorTheme.TEXT_ACCENT_GOLD;
        }

        // 6. Bottom Status & Disclaimer text
        var resolvedStats = CanvasEditorLayout.resolveStatsText(width, height, b, font.lineHeight, candidates, font::width);
        if (resolvedStats.visible()) {
            g.text(font, resolvedStats.item(), resolvedStats.x(), resolvedStats.y(), statusColor);
        }

        var placement = CanvasEditorLayout.computeTextPlacement(width, height, b, resolvedStats.width(), font.lineHeight);
        if (placement.maxTextWidth() > 0 && placement.maxAllowedDisclaimerLines() > 0) {
            Component disclaimerComp = (viewMode == ViewMode.FIREWORK)
                    ? label("preview_dust_disclaimer")
                    : label("session_only");
            var lines = font.split(disclaimerComp, placement.maxTextWidth());
            int linesToDraw = Math.min(lines.size(), placement.maxAllowedDisclaimerLines());
            for (int i = 0; i < linesToDraw; i++) {
                g.text(font, lines.get(i), placement.disclaimerX(), placement.disclaimerY() + i * font.lineHeight, CanvasEditorTheme.TEXT_NOTE);
            }
        }

        super.extractRenderState(g, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (viewMode == ViewMode.DRAW && input.click(event, left, top, boardWidth, boardHeight, color)) {
            syncHistoryButtons();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (viewMode == ViewMode.DRAW && input.drag(event, left, top, boardWidth, boardHeight, color)) {
            syncHistoryButtons();
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (input.release(event)) {
            syncHistoryButtons();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.hasControlDown() && event.key() == InputConstants.KEY_S) {
            input.reset();
            quickSaveDraft();
            return true;
        }
        if (event.hasControlDown() && event.key() == InputConstants.KEY_Z) {
            input.reset();
            if (event.hasShiftDown()) {
                model.redo();
            } else {
                model.undo();
            }
            syncHistoryButtons();
            return true;
        }
        if (event.hasControlDown() && event.key() == InputConstants.KEY_Y) {
            input.reset();
            model.redo();
            syncHistoryButtons();
            return true;
        }
        if (!event.hasControlDown() && !event.hasAltDown()) {
            if (event.key() == InputConstants.KEY_B) {
                selectBrushTool();
                return true;
            }
            if (event.key() == InputConstants.KEY_E) {
                selectEraserTool();
                return true;
            }
            if (event.key() == InputConstants.KEY_F) {
                selectFillTool();
                return true;
            }
            if (event.key() == InputConstants.KEY_M) {
                toggleResampleMode();
                return true;
            }
            if (event.key() == InputConstants.KEY_P) {
                cycleViewMode();
                return true;
            }
        }
        return super.keyPressed(event);
    }

    /**
     * Custom color swatch button with wooden border and golden selection highlight.
     */
    private static final class SwatchButton extends Button {
        private final int swatchColor;
        private final BooleanSupplier selectedSupplier;

        SwatchButton(int x, int y, int width, int height, Component message, int swatchColor,
                     BooleanSupplier selectedSupplier, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.swatchColor = swatchColor;
            this.selectedSupplier = selectedSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            CanvasEditorTheme.drawColorSwatch(
                    g, getX(), getY(), getWidth(), getHeight(),
                    swatchColor, isHoveredOrFocused(), selectedSupplier.getAsBoolean()
            );
        }
    }

    /**
     * Custom artist tool/action button supporting procedural pixel-art icons, category colors,
     * active selection highlight, disabled state, and optional short label.
     */
    private final class ThemedToolButton extends Button {
        private final Supplier<CanvasEditorTheme.IconType> iconSupplier;
        private final Supplier<Component> shortLabelSupplier;
        private final CanvasEditorTheme.ButtonCategory category;
        private final BooleanSupplier selectedSupplier;

        ThemedToolButton(int x, int y, int width, int height, Component narrationMessage,
                         Supplier<CanvasEditorTheme.IconType> iconSupplier,
                         Supplier<Component> shortLabelSupplier,
                         CanvasEditorTheme.ButtonCategory category,
                         BooleanSupplier selectedSupplier,
                         OnPress onPress) {
            super(x, y, width, height, narrationMessage, onPress, DEFAULT_NARRATION);
            this.iconSupplier = iconSupplier;
            this.shortLabelSupplier = shortLabelSupplier;
            this.category = category;
            this.selectedSupplier = selectedSupplier;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
            boolean selected = selectedSupplier.getAsBoolean();
            CanvasEditorTheme.drawStyledButton(
                    g, getX(), getY(), getWidth(), getHeight(),
                    category, this.active, isHoveredOrFocused(), selected
            );
            Component shortLabel = shortLabelSupplier != null ? shortLabelSupplier.get() : null;
            CanvasEditorTheme.drawIconAndOptionalLabel(
                    g, font, getX(), getY(), getWidth(), getHeight(),
                    iconSupplier.get(), shortLabel, this.active, selected
            );
        }
    }
}
