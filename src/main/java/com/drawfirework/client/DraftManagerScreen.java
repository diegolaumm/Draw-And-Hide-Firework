package com.drawfirework.client;

import com.drawfirework.draft.Draft;
import com.drawfirework.draft.DraftMetadata;
import com.drawfirework.draft.DraftStorage;
import com.drawfirework.draft.DraftStorageException;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * High-resolution draft management screen: list, load, new, save, save-as, rename, and delete.
 */
public final class DraftManagerScreen extends Screen {
    private final CanvasEditorScreen parentEditor;
    private final DraftStorage storage;

    private DraftListWidget listWidget;
    private Button loadButton;
    private Button renameButton;
    private Button deleteButton;

    public DraftManagerScreen(CanvasEditorScreen parentEditor, DraftStorage storage) {
        super(Component.translatable("editor.drawfirework.draft_manager_title"));
        this.parentEditor = Objects.requireNonNull(parentEditor, "parentEditor must not be null");
        this.storage = Objects.requireNonNull(storage, "storage must not be null");
    }

    public Font getFont() {
        return font;
    }

    @Override
    protected void init() {
        DraftManagerLayout.LayoutResult layout = DraftManagerLayout.compute(width, height, 7);

        listWidget = new DraftListWidget(minecraft, layout.listWidth(), layout.listHeight(), 32, 32, this);
        listWidget.setX(layout.listX());
        listWidget.setY(layout.listY());
        addRenderableWidget(listWidget);

        int startX = layout.buttonX();
        int btnW = layout.buttonWidth();
        int btnH = layout.buttonHeight();
        int spacing = layout.buttonSpacing();
        int startY = layout.buttonStartY();

        // 1. Load Draft
        loadButton = Button.builder(Component.translatable("editor.drawfirework.draft_load"), b -> loadSelectedDraft())
                .bounds(startX, startY, btnW, btnH).build();
        addRenderableWidget(loadButton);

        // 2. New Draft
        startY += btnH + spacing;
        addRenderableWidget(Button.builder(Component.translatable("editor.drawfirework.draft_new"), b -> promptNewDraft())
                .bounds(startX, startY, btnW, btnH).build());

        // 3. Save Draft
        startY += btnH + spacing;
        addRenderableWidget(Button.builder(Component.translatable("editor.drawfirework.save_draft"), b -> saveCurrentDraft())
                .bounds(startX, startY, btnW, btnH).build());

        // 4. Save As...
        startY += btnH + spacing;
        addRenderableWidget(Button.builder(Component.translatable("editor.drawfirework.draft_save_as"), b -> promptSaveAs())
                .bounds(startX, startY, btnW, btnH).build());

        // 5. Rename...
        startY += btnH + spacing;
        renameButton = Button.builder(Component.translatable("editor.drawfirework.draft_rename"), b -> promptRename())
                .bounds(startX, startY, btnW, btnH).build();
        addRenderableWidget(renameButton);

        // 6. Delete
        startY += btnH + spacing;
        deleteButton = Button.builder(Component.translatable("editor.drawfirework.draft_delete"), b -> confirmDelete())
                .bounds(startX, startY, btnW, btnH).build();
        addRenderableWidget(deleteButton);

        // 7. Back to Editor
        startY += btnH + spacing;
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> onClose())
                .bounds(startX, startY, btnW, btnH).build());

        refreshList();
        updateButtonStates();
    }

    public void refreshList() {
        if (listWidget == null) return;
        List<DraftMetadata> drafts = storage.listDrafts();
        UUID currentId = parentEditor.getCurrentDraftId();
        listWidget.setDraftEntries(drafts, currentId);
        updateButtonStates();
    }

    public void onEntrySelected(DraftListWidget.DraftEntry entry) {
        if (listWidget != null) {
            listWidget.setSelected(entry);
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = listWidget != null && listWidget.getSelected() != null;
        if (loadButton != null) loadButton.active = hasSelection;
        if (renameButton != null) renameButton.active = hasSelection;
        if (deleteButton != null) deleteButton.active = hasSelection;
    }

    public void loadSelectedDraft() {
        if (listWidget == null) return;
        DraftListWidget.DraftEntry selected = listWidget.getSelected();
        if (selected == null) return;

        UUID targetId = selected.getMetadata().id();

        if (parentEditor.isDraftDirty()) {
            if (minecraft != null) {
                minecraft.gui.setScreen(new UnsavedChangesScreen(
                        this,
                        () -> parentEditor.saveAndThen(DraftManagerScreen.this, () -> executeLoad(targetId), null),
                        () -> executeLoad(targetId)
                ));
            }
        } else {
            executeLoad(targetId);
        }
    }

    private void executeLoad(UUID id) {
        try {
            Draft draft = storage.loadDraft(id);
            if (!parentEditor.getModel().canLoadLosslessly(draft.pixels())) {
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.translatable(
                            "editor.drawfirework.draft_lossy_load_error",
                            draft.name(),
                            parentEditor.getModel().outputWidth() + "x" + parentEditor.getModel().outputHeight()));
                }
                return;
            }
            parentEditor.loadDraftData(draft);
            if (minecraft != null) {
                minecraft.gui.setScreen(parentEditor);
                if (minecraft.player != null) {
                    minecraft.player.sendSystemMessage(Component.translatable(
                            "editor.drawfirework.draft_loaded", draft.name()));
                }
            }
        } catch (Exception e) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.sendSystemMessage(Component.translatable(
                        "editor.drawfirework.draft_error", e.getMessage()));
            }
            refreshList();
        }
    }

    private void promptNewDraft() {
        if (parentEditor.isDraftDirty()) {
            if (minecraft != null) {
                minecraft.gui.setScreen(new UnsavedChangesScreen(
                        this,
                        () -> parentEditor.saveAndThen(DraftManagerScreen.this, this::openNewDraftPrompt, null),
                        this::openNewDraftPrompt
                ));
            }
        } else {
            openNewDraftPrompt();
        }
    }

    private void openNewDraftPrompt() {
        if (minecraft == null) return;
        minecraft.gui.setScreen(new DraftNamePromptScreen(
                this,
                Component.translatable("editor.drawfirework.draft_new_title"),
                Component.translatable("editor.drawfirework.draft_create"),
                "Untitled",
                name -> {
                    try {
                        Draft blank = storage.createNewDraft(name, 256, 256, new int[256 * 256]);
                        parentEditor.loadDraftData(blank);
                        minecraft.gui.setScreen(parentEditor);
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(Component.translatable(
                                    "editor.drawfirework.draft_created", blank.name()));
                        }
                    } catch (DraftStorageException e) {
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(Component.translatable(
                                    "editor.drawfirework.draft_error", e.getMessage()));
                        }
                        minecraft.gui.setScreen(DraftManagerScreen.this);
                    }
                }
        ));
    }

    private void saveCurrentDraft() {
        parentEditor.saveAndThen(DraftManagerScreen.this, () -> {
            if (minecraft != null) {
                minecraft.gui.setScreen(DraftManagerScreen.this);
            }
            refreshList();
        }, () -> {
            if (minecraft != null) {
                minecraft.gui.setScreen(DraftManagerScreen.this);
            }
        });
    }

    private void promptSaveAs() {
        if (minecraft == null) return;
        String baseName = parentEditor.getCurrentDraftName();
        String initial = baseName.equals("Untitled") ? "Untitled" : "Copy of " + baseName;

        minecraft.gui.setScreen(new DraftNamePromptScreen(
                this,
                Component.translatable("editor.drawfirework.draft_save_as_title"),
                Component.translatable("editor.drawfirework.save_draft"),
                initial,
                newName -> {
                    try {
                        int[] pixels = parentEditor.getModel().toDraft256Pixels();
                        Draft newDraft = storage.createNewDraft(newName, 256, 256, pixels);
                        parentEditor.markSaved(newDraft.id(), newDraft.name());
                        minecraft.gui.setScreen(DraftManagerScreen.this);
                        refreshList();
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(Component.translatable(
                                    "editor.drawfirework.draft_saved", newDraft.name()));
                        }
                    } catch (DraftStorageException e) {
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(Component.translatable(
                                    "editor.drawfirework.draft_error", e.getMessage()));
                        }
                        minecraft.gui.setScreen(DraftManagerScreen.this);
                    }
                }
        ));
    }

    private void promptRename() {
        if (listWidget == null) return;
        DraftListWidget.DraftEntry selected = listWidget.getSelected();
        if (selected == null || minecraft == null) return;

        DraftMetadata meta = selected.getMetadata();
        minecraft.gui.setScreen(new DraftNamePromptScreen(
                this,
                Component.translatable("editor.drawfirework.draft_rename_title"),
                Component.translatable("editor.drawfirework.draft_rename_confirm"),
                meta.name(),
                newName -> {
                    try {
                        Draft renamed = storage.renameDraft(meta.id(), newName);
                        if (meta.id().equals(parentEditor.getCurrentDraftId())) {
                            parentEditor.setCurrentDraftName(renamed.name());
                        }
                        minecraft.gui.setScreen(DraftManagerScreen.this);
                        refreshList();
                    } catch (DraftStorageException e) {
                        if (minecraft.player != null) {
                            minecraft.player.sendSystemMessage(Component.translatable(
                                    "editor.drawfirework.draft_error", e.getMessage()));
                        }
                        minecraft.gui.setScreen(DraftManagerScreen.this);
                    }
                }
        ));
    }

    private void confirmDelete() {
        if (listWidget == null) return;
        DraftListWidget.DraftEntry selected = listWidget.getSelected();
        if (selected == null || minecraft == null) return;

        DraftMetadata meta = selected.getMetadata();
        minecraft.gui.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) {
                        storage.deleteDraft(meta.id());
                        if (meta.id().equals(parentEditor.getCurrentDraftId())) {
                            parentEditor.clearCurrentDraftId();
                        }
                        refreshList();
                    }
                    minecraft.gui.setScreen(DraftManagerScreen.this);
                },
                Component.translatable("editor.drawfirework.draft_delete_title"),
                Component.translatable("editor.drawfirework.draft_delete_confirm", meta.name())
        ));
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.gui.setScreen(parentEditor);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, CanvasEditorTheme.STUDIO_BG);
        graphics.centeredText(font, title, width / 2, 8, CanvasEditorTheme.TEXT_TITLE);
        if (storage.getLastListError() != null) {
            graphics.centeredText(font, Component.translatable("editor.drawfirework.draft_error", storage.getLastListError()),
                    width / 2, height - 12, 0xFFFF5555);
        } else if (!ClientDraftStorage.getMigrationConflicts().isEmpty()) {
            graphics.centeredText(font, Component.translatable("editor.drawfirework.draft_conflict_notice"),
                    width / 2, height - 12, 0xFFFFAA00);
        }
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
