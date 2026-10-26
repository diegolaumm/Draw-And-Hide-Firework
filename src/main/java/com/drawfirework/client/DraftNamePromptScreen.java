package com.drawfirework.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Modal prompt dialog for entering or editing a firework draft name.
 */
public final class DraftNamePromptScreen extends Screen {
    private final Screen parent;
    private final String initialName;
    private final Component confirmButtonText;
    private final Consumer<String> onConfirm;
    private final Runnable onCancel;

    private EditBox nameInput;
    private Button confirmButton;

    public DraftNamePromptScreen(Screen parent, Component title, Component confirmButtonText,
                                 String initialName, Consumer<String> onConfirm) {
        this(parent, title, confirmButtonText, initialName, onConfirm, null);
    }

    public DraftNamePromptScreen(Screen parent, Component title, Component confirmButtonText,
                                 String initialName, Consumer<String> onConfirm, Runnable onCancel) {
        super(title);
        this.parent = Objects.requireNonNull(parent, "parent must not be null");
        this.confirmButtonText = confirmButtonText != null ? confirmButtonText : CommonComponents.GUI_OK;
        this.initialName = initialName != null ? initialName : "";
        this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm must not be null");
        this.onCancel = onCancel;
    }

    @Override
    protected void init() {
        int boxW = 200;
        int boxH = 20;
        int startX = (width - boxW) / 2;
        int startY = height / 2 - 20;

        nameInput = new EditBox(font, startX, startY, boxW, boxH, Component.translatable("editor.drawfirework.draft_name_hint"));
        nameInput.setMaxLength(64);
        nameInput.setValue(initialName);
        nameInput.setResponder(text -> {
            if (confirmButton != null) {
                confirmButton.active = !text.trim().isEmpty();
            }
        });
        addRenderableWidget(nameInput);
        setInitialFocus(nameInput);

        int btnW = (boxW - 4) / 2;
        int btnY = startY + 30;

        confirmButton = Button.builder(confirmButtonText, button -> confirm())
                .bounds(startX, btnY, btnW, 20)
                .build();
        confirmButton.active = !nameInput.getValue().trim().isEmpty();
        addRenderableWidget(confirmButton);

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> cancel())
                .bounds(startX + btnW + 4, btnY, btnW, 20)
                .build());
    }

    private void confirm() {
        String name = nameInput.getValue().trim();
        if (!name.isEmpty()) {
            onConfirm.accept(name);
        }
    }

    private void cancel() {
        if (onCancel != null) {
            onCancel.run();
        }
        if (minecraft != null) {
            minecraft.gui.setScreen(parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER) {
            if (confirmButton != null && confirmButton.active) {
                confirm();
                return true;
            }
        }
        if (event.key() == InputConstants.KEY_ESCAPE) {
            cancel();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        // Semi-transparent warm studio overlay
        graphics.fill(0, 0, width, height, CanvasEditorTheme.MODAL_BG);
        graphics.centeredText(font, title, width / 2, height / 2 - 45, CanvasEditorTheme.TEXT_TITLE);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
