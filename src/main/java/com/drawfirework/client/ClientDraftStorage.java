package com.drawfirework.client;

import com.drawfirework.draft.DraftStorage;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Client-side accessor for the draft storage service located at <gameDirectory>/drawfirework/drafts/.
 * Automatically checks and safely migrates drafts from legacy <gameDirectory>/config/drawfirework/drafts/ if present.
 */
public final class ClientDraftStorage {
    private static DraftStorage instance;
    private static volatile String initError = null;
    private static final List<String> migrationConflicts = new ArrayList<>();

    private ClientDraftStorage() {}

    public static synchronized String getInitError() {
        return initError;
    }

    public static synchronized List<String> getMigrationConflicts() {
        return new ArrayList<>(migrationConflicts);
    }

    public static synchronized DraftStorage getInstance() {
        if (instance == null && initError == null) {
            try {
                Path gameDir = Minecraft.getInstance().gameDirectory.toPath();
                Path draftsDir = gameDir.resolve("drawfirework").resolve("drafts");

                // Minor 2: Check and migrate legacy drafts from config/drawfirework/drafts/ if present
                Path legacyDir = gameDir.resolve("config").resolve("drawfirework").resolve("drafts");
                migrateLegacyDrafts(legacyDir, draftsDir);

                instance = new DraftStorage(draftsDir);
            } catch (Exception e) {
                initError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                throw new RuntimeException("Draft storage service failed to initialize: " + initError, e);
            }
        }
        if (initError != null) {
            throw new RuntimeException("Draft storage service unavailable: " + initError);
        }
        return instance;
    }

    public static void migrateLegacyDrafts(Path legacyDir, Path targetDir) {
        if (Files.exists(legacyDir) && Files.isDirectory(legacyDir)) {
            try {
                Files.createDirectories(targetDir);
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(legacyDir, "*.json")) {
                    for (Path entry : stream) {
                        Path destination = targetDir.resolve(entry.getFileName());
                        if (!Files.exists(destination)) {
                            try {
                                Files.copy(entry, destination);
                            } catch (IOException ignored) {
                            }
                        } else {
                            // Minor B: Destination already exists. Check if contents differ!
                            try {
                                if (Files.mismatch(entry, destination) != -1L) {
                                    // Conflict! Contents differ!
                                    // Neither file is overwritten or deleted.
                                    // Preserve a conflict backup so both are safely retained.
                                    String conflictName = entry.getFileName().toString().replace(".json", ".legacy_conflict.json");
                                    Path conflictPath = targetDir.resolve(conflictName);
                                    if (!Files.exists(conflictPath)) {
                                        Files.copy(entry, conflictPath);
                                    }
                                    migrationConflicts.add("UUID conflict for " + entry.getFileName()
                                            + ": preserved at " + entry + " and backup created at " + conflictPath);
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static synchronized void setInstanceForTesting(DraftStorage storage) {
        instance = storage;
        initError = null;
    }
}
