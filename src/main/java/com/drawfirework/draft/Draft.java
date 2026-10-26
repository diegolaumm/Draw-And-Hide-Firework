package com.drawfirework.draft;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable full-resolution firework draft containing 256x256 raw ARGB pixels and metadata.
 */
public final class Draft {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final String FORMAT = "argb32-hex";
    public static final int DEFAULT_WIDTH = 256;
    public static final int DEFAULT_HEIGHT = 256;

    private final DraftMetadata metadata;
    private final int[] pixels;

    public Draft(DraftMetadata metadata, int[] pixels) {
        Objects.requireNonNull(metadata, "metadata must not be null");
        Objects.requireNonNull(pixels, "pixels must not be null");
        int area = metadata.width() * metadata.height();
        if (pixels.length != area) {
            throw new IllegalArgumentException("Pixel count " + pixels.length + " does not match dimensions "
                    + metadata.width() + "x" + metadata.height() + " (expected " + area + ")");
        }
        this.metadata = metadata;
        this.pixels = pixels.clone();
    }

    public static Draft createNew(String name, int width, int height, int[] pixels) {
        long now = System.currentTimeMillis();
        DraftMetadata meta = new DraftMetadata(
                UUID.randomUUID(),
                name,
                now,
                now,
                width,
                height,
                CURRENT_SCHEMA_VERSION
        );
        return new Draft(meta, pixels);
    }

    public static Draft createBlank(String name) {
        return createNew(name, DEFAULT_WIDTH, DEFAULT_HEIGHT, new int[DEFAULT_WIDTH * DEFAULT_HEIGHT]);
    }

    public Draft withName(String newName) {
        DraftMetadata meta = new DraftMetadata(
                metadata.id(),
                newName,
                metadata.createdAt(),
                System.currentTimeMillis(),
                metadata.width(),
                metadata.height(),
                metadata.schemaVersion()
        );
        return new Draft(meta, pixels);
    }

    public Draft withPixels(int[] newPixels) {
        DraftMetadata meta = new DraftMetadata(
                metadata.id(),
                metadata.name(),
                metadata.createdAt(),
                System.currentTimeMillis(),
                metadata.width(),
                metadata.height(),
                metadata.schemaVersion()
        );
        return new Draft(meta, newPixels);
    }

    public Draft copyAsNew(String newName) {
        long now = System.currentTimeMillis();
        DraftMetadata meta = new DraftMetadata(
                UUID.randomUUID(),
                newName,
                now,
                now,
                metadata.width(),
                metadata.height(),
                CURRENT_SCHEMA_VERSION
        );
        return new Draft(meta, pixels);
    }

    public DraftMetadata metadata() {
        return metadata;
    }

    public UUID id() {
        return metadata.id();
    }

    public String name() {
        return metadata.name();
    }

    public long createdAt() {
        return metadata.createdAt();
    }

    public long updatedAt() {
        return metadata.updatedAt();
    }

    public int width() {
        return metadata.width();
    }

    public int height() {
        return metadata.height();
    }

    public int schemaVersion() {
        return metadata.schemaVersion();
    }

    public int pixelCount() {
        return pixels.length;
    }

    public int[] pixels() {
        return pixels.clone();
    }

    public int pixel(int x, int y) {
        Objects.checkIndex(x, metadata.width());
        Objects.checkIndex(y, metadata.height());
        return pixels[y * metadata.width() + x];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Draft draft)) return false;
        return metadata.equals(draft.metadata) && Arrays.equals(pixels, draft.pixels);
    }

    @Override
    public int hashCode() {
        int result = metadata.hashCode();
        result = 31 * result + Arrays.hashCode(pixels);
        return result;
    }

    @Override
    public String toString() {
        return "Draft{id=" + metadata.id() + ", name='" + metadata.name() + "', size="
                + metadata.width() + "x" + metadata.height() + "}";
    }
}
