package com.drawfirework.client;

import com.drawfirework.draft.DraftMetadata;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Scrollable list widget for browsing and selecting firework drafts.
 */
public final class DraftListWidget extends ObjectSelectionList<DraftListWidget.DraftEntry> {
    private final DraftManagerScreen screen;

    public DraftListWidget(Minecraft mc, int width, int height, int y, int itemHeight, DraftManagerScreen screen) {
        super(mc, width, height, y, itemHeight);
        this.screen = Objects.requireNonNull(screen, "screen must not be null");
    }

    public void setDraftEntries(Collection<DraftMetadata> drafts, UUID currentDraftId) {
        clearEntries();
        for (DraftMetadata meta : drafts) {
            boolean isCurrent = currentDraftId != null && currentDraftId.equals(meta.id());
            addEntry(new DraftEntry(meta, isCurrent, screen));
        }
    }

    public static final class DraftEntry extends ObjectSelectionList.Entry<DraftEntry> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault());

        private final DraftMetadata metadata;
        private final boolean isCurrent;
        private final DraftManagerScreen screen;

        public DraftEntry(DraftMetadata metadata, boolean isCurrent, DraftManagerScreen screen) {
            this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
            this.isCurrent = isCurrent;
            this.screen = Objects.requireNonNull(screen, "screen must not be null");
        }

        public DraftMetadata getMetadata() {
            return metadata;
        }

        @Override
        public Component getNarration() {
            return Component.literal(metadata.name());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            Font font = screen.getFont();
            int x = getX() + 4;
            int y = getY() + 4;

            // Line 1: Draft Name (highlighted if hovering)
            int nameColor = isHovering ? CanvasEditorTheme.TEXT_ACCENT_GOLD : CanvasEditorTheme.TEXT_TITLE;
            String nameText = metadata.name();
            graphics.text(font, nameText, x, y, nameColor);

            if (isCurrent) {
                int nameWidth = font.width(nameText);
                Component tag = Component.translatable("editor.drawfirework.draft_tag_current");
                graphics.text(font, tag, x + nameWidth + 8, y, CanvasEditorTheme.TEXT_ACCENT_GOLD);
            }

            // Line 2: Updated Timestamp and Dimensions
            String dateText = FORMATTER.format(Instant.ofEpochMilli(metadata.updatedAt()));
            String infoText = dateText + "  (" + metadata.width() + "x" + metadata.height() + ")";
            graphics.text(font, infoText, x, y + 13, CanvasEditorTheme.TEXT_SUBTITLE);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            screen.onEntrySelected(this);
            if (doubleClick) {
                screen.loadSelectedDraft();
                return true;
            }
            return true;
        }
    }
}
