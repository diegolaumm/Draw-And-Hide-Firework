package com.drawfirework.draft;

import java.util.Objects;
import java.util.UUID;

/**
 * Immutable metadata header for a high-resolution firework draft.
 * User-facing name is decoupled from filesystem storage (UUID is used for filenames).
 */
public record DraftMetadata(
        UUID id,
        String name,
        long createdAt,
        long updatedAt,
        int width,
        int height,
        int schemaVersion
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final int MAX_NAME_LENGTH = 64;

    public DraftMetadata {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(name, "name must not be null");
        name = sanitizeName(name);
        if (width < 1 || width > 256 || height < 1 || height > 256) {
            throw new IllegalArgumentException("Draft dimensions must be 1..256 (got " + width + "x" + height + ")");
        }
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("Invalid schemaVersion: " + schemaVersion);
        }
    }

    public static String sanitizeName(String input) {
        if (input == null || input.isBlank()) {
            return "Untitled";
        }
        String trimmed = input.trim();
        // Replace file path separators and control characters for safe display
        trimmed = trimmed.replace('/', '_').replace('\\', '_').replace(':', '_');
        if (trimmed.length() > MAX_NAME_LENGTH) {
            trimmed = trimmed.substring(0, MAX_NAME_LENGTH);
        }
        return trimmed.isEmpty() ? "Untitled" : trimmed;
    }
}
