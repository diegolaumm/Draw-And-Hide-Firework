package com.drawfirework.draft;

import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Robust, safe, thread-confined pure Java filesystem storage for firework drafts.
 * Features:
 * - Atomic replacement using temp files (.tmp)
 * - Safe backup preservation (.bak) before overwriting existing drafts
 * - Corruption detection without crashing or deleting corrupted files
 * - Schema versioning with safe rejection of newer versions
 * - Path traversal prevention
 * - Bounded file size (2 MB) and draft count limits (200)
 */
public final class DraftStorage {
    public static final int MAX_FILE_BYTES = 2_000_000;
    public static final int MAX_DRAFTS = 200;
    public static final int CURRENT_SCHEMA_VERSION = 1;
    public static final String FORMAT = "argb32-hex";
    public static final String FILE_EXTENSION = ".json";
    public static final String TEMP_EXTENSION = ".json.tmp";
    public static final String BACKUP_EXTENSION = ".json.bak";

    private static final HexFormat HEX = HexFormat.of();

    private final Path baseDir;
    private volatile String lastListError = null;

    public DraftStorage(Path baseDir) {
        Objects.requireNonNull(baseDir, "baseDir must not be null");
        this.baseDir = baseDir.toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize draft storage directory: " + this.baseDir, e);
        }
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public String getLastListError() {
        return lastListError;
    }

    /**
     * Lists all valid drafts sorted by updatedAt descending (most recently modified first).
     * Corrupted files are safely skipped without aborting the listing of valid drafts.
     */
    public List<DraftMetadata> listDrafts() {
        lastListError = null;
        List<DraftMetadata> list = new ArrayList<>();
        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) {
            return list;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*.json")) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                if (fileName.endsWith(TEMP_EXTENSION) || fileName.endsWith(BACKUP_EXTENSION)) {
                    continue;
                }
                try {
                    DraftMetadata meta = readMetadata(entry);
                    if (meta != null) {
                        list.add(meta);
                    }
                } catch (Exception ignored) {
                    // Safe error containment: corrupted files are kept intact on disk but not listed
                }
            }
        } catch (IOException e) {
            lastListError = e.getMessage();
        }

        list.sort(Comparator.comparingLong(DraftMetadata::updatedAt).reversed());
        return list;
    }

    /**
     * Loads a draft by UUID.
     * @throws DraftCorruptedException if the file is malformed, truncated, or corrupted.
     * @throws DraftVersionException if the file has a newer unsupported schema version.
     * @throws DraftStorageException on I/O error or missing file.
     */
    public Draft loadDraft(UUID id) throws DraftStorageException {
        Objects.requireNonNull(id, "Draft ID must not be null");
        Path path = resolvePath(id, FILE_EXTENSION);
        if (!Files.exists(path)) {
            throw new DraftStorageException("Draft file does not exist: " + id);
        }
        return readFullDraft(path, id);
    }

    /**
     * Saves or updates a draft with atomic replacement and backup protection.
     */
    public void saveDraft(Draft draft) throws DraftStorageException {
        Objects.requireNonNull(draft, "Draft must not be null");
        UUID id = draft.id();
        Path targetPath = resolvePath(id, FILE_EXTENSION);
        Path bakPath = resolvePath(id, BACKUP_EXTENSION);

        // Check draft count limit for newly created drafts
        if (!Files.exists(targetPath)) {
            int currentCount = countDrafts();
            if (currentCount >= MAX_DRAFTS) {
                throw new DraftStorageException("Draft storage limit reached: maximum " + MAX_DRAFTS + " drafts");
            }
        }

        // Step 1: Pre-overwrite validation and safe backup protection
        if (Files.exists(targetPath)) {
            boolean targetValid = false;
            try {
                readFullDraft(targetPath, id);
                targetValid = true;
            } catch (DraftVersionException e) {
                throw e; // Refuse to overwrite newer schema
            } catch (Exception ignored) {
                targetValid = false;
            }

            if (targetValid) {
                try {
                    Files.copy(targetPath, bakPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new DraftStorageException("Failed to create draft backup before replacement: " + e.getMessage(), e);
                }
            } else {
                // Target is corrupted!
                // Major 2: Check if an intact backup already exists
                if (Files.exists(bakPath)) {
                    boolean bakValid = false;
                    try {
                        readFullDraft(bakPath, id);
                        bakValid = true;
                    } catch (Exception ignored) {
                    }
                    if (bakValid) {
                        throw new DraftStorageException("Master draft file is corrupted, but a valid backup (.bak) exists. "
                                + "Overwrite refused to prevent loss of backup. Please recover from backup or save as a new draft.");
                    }
                }
                // Target is corrupt; NEVER copy corrupt target to bakPath.
            }
        }

        // Step 2: Write to safe, unique temporary file
        Path tempPath;
        try {
            tempPath = Files.createTempFile(baseDir, id.toString() + "_", ".tmp");
        } catch (IOException e) {
            throw new DraftStorageException("Failed to create temporary file for draft save: " + e.getMessage(), e);
        }

        try {
            writeDraftToPath(tempPath, draft);

            // Step 3: Atomic replace from temp to target
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            // Attempt to restore target from backup if target was lost during move failure
            if (!Files.exists(targetPath) && Files.exists(bakPath)) {
                try {
                    Files.copy(bakPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ignored) {
                }
            }
            throw new DraftStorageException("Failed to atomically replace draft file for " + id + ": " + e.getMessage(), e);
        } finally {
            safeDelete(tempPath);
        }
    }

    /**
     * Creates a new draft from scratch and saves it.
     */
    public Draft createNewDraft(String name, int width, int height, int[] pixels) throws DraftStorageException {
        Draft draft = Draft.createNew(name, width, height, pixels);
        saveDraft(draft);
        return draft;
    }

    /**
     * Saves the draft under a new name with a fresh UUID.
     */
    public Draft saveDraftAs(Draft source, String newName) throws DraftStorageException {
        Draft newDraft = source.copyAsNew(newName);
        saveDraft(newDraft);
        return newDraft;
    }

    /**
     * Renames an existing draft without altering its pixel content.
     */
    public Draft renameDraft(UUID id, String newName) throws DraftStorageException {
        Draft existing = loadDraft(id);
        Draft renamed = existing.withName(newName);
        saveDraft(renamed);
        return renamed;
    }

    /**
     * Deletes a draft, including its backup and temp files.
     */
    public boolean deleteDraft(UUID id) {
        Objects.requireNonNull(id, "Draft ID must not be null");
        Path targetPath = resolvePath(id, FILE_EXTENSION);
        Path bakPath = resolvePath(id, BACKUP_EXTENSION);

        boolean deleted = safeDelete(targetPath);
        safeDelete(bakPath);
        return deleted;
    }

    /**
     * Attempts to recover a draft from its backup (.bak) file if the main file is damaged or lost.
     */
    public boolean recoverFromBackup(UUID id) throws DraftStorageException {
        return recoverFromBackup(id, false);
    }

    /**
     * Attempts to recover a draft from its backup (.bak) file.
     * If the target is already valid and intact, recovery is refused unless force is true.
     */
    public boolean recoverFromBackup(UUID id, boolean force) throws DraftStorageException {
        Objects.requireNonNull(id, "Draft ID must not be null");
        Path targetPath = resolvePath(id, FILE_EXTENSION);
        Path bakPath = resolvePath(id, BACKUP_EXTENSION);

        if (!Files.exists(bakPath)) {
            return false;
        }

        // Validate that the backup file is itself well-formed
        Draft backupDraft = readFullDraft(bakPath, id);
        if (backupDraft == null) {
            return false;
        }

        // Check if target is already valid or has newer schema
        if (Files.exists(targetPath)) {
            boolean targetValid = false;
            try {
                readFullDraft(targetPath, id);
                targetValid = true;
            } catch (DraftVersionException e) {
                // Major 3: Target has newer unsupported schema!
                // Must NEVER be overwritten by recovery, even with force=true!
                throw new DraftVersionException(e.getVersion(),
                        "Cannot recover draft: existing target file has newer schema version " + e.getVersion()
                        + " (current supported version is " + CURRENT_SCHEMA_VERSION + "). Recovery refused to protect newer draft format.");
            } catch (Exception ignored) {
                targetValid = false;
            }

            if (targetValid && !force) {
                throw new DraftStorageException("Target draft is already valid and intact. Recovery refused to prevent overwriting valid draft with older backup.");
            }
        }

        Path tempPath;
        try {
            tempPath = Files.createTempFile(baseDir, id.toString() + "_rec_", ".tmp");
        } catch (IOException e) {
            throw new DraftStorageException("Failed to create temporary file for recovery: " + e.getMessage(), e);
        }

        try {
            Files.copy(bakPath, tempPath, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            throw new DraftStorageException("Failed to restore backup to target: " + e.getMessage(), e);
        } finally {
            safeDelete(tempPath);
        }
    }

    public boolean hasBackup(UUID id) {
        return Files.exists(resolvePath(id, BACKUP_EXTENSION));
    }

    public int countDrafts() {
        int count = 0;
        if (!Files.exists(baseDir) || !Files.isDirectory(baseDir)) return 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*.json")) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (!name.endsWith(TEMP_EXTENSION) && !name.endsWith(BACKUP_EXTENSION)) {
                    count++;
                }
            }
        } catch (IOException ignored) {
        }
        return count;
    }

    public Path resolvePath(UUID id, String extension) {
        Objects.requireNonNull(id, "Draft ID must not be null");
        Objects.requireNonNull(extension, "Extension must not be null");
        String filename = id.toString() + extension;
        Path resolved = baseDir.resolve(filename).normalize();
        if (!resolved.getParent().equals(baseDir)) {
            throw new SecurityException("Path traversal attempt detected: " + id);
        }
        checkPathSecurity(resolved);
        return resolved;
    }

    private void checkPathSecurity(Path path) {
        if (Files.isSymbolicLink(path)) {
            throw new SecurityException("Symbolic links are not permitted: " + path);
        }
        if (Files.exists(path)) {
            try {
                Path realPath = path.toRealPath();
                Path realBase = baseDir.toRealPath();
                if (!realPath.startsWith(realBase)) {
                    throw new SecurityException("Path escapes base directory: " + path);
                }
            } catch (IOException e) {
                throw new SecurityException("Cannot determine real path: " + path, e);
            }
        }
    }

    // --- Internal I/O and Parsing ---

    private void writeDraftToPath(Path path, Draft draft) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
             JsonWriter writer = new JsonWriter(bw)) {

            writer.setIndent("  ");
            writer.beginObject();

            writer.name("schemaVersion").value(draft.schemaVersion());
            writer.name("id").value(draft.id().toString());
            writer.name("name").value(draft.name());
            writer.name("createdAt").value(draft.createdAt());
            writer.name("updatedAt").value(draft.updatedAt());
            writer.name("width").value(draft.width());
            writer.name("height").value(draft.height());
            writer.name("format").value(FORMAT);

            // Encode 256x256 ARGB pixels into hex
            int[] px = draft.pixels();
            StringBuilder hex = new StringBuilder(px.length * 8);
            for (int p : px) {
                hex.append(HEX.toHexDigits(p));
            }
            writer.name("pixels").value(hex.toString());

            writer.endObject();
            writer.flush();
        }
    }

    public DraftMetadata readMetadata(Path path) throws IOException, DraftStorageException {
        checkFileSize(path);
        try (BufferedReader br = Files.newBufferedReader(path);
             JsonReader reader = new JsonReader(br)) {

            reader.setStrictness(Strictness.STRICT);
            reader.beginObject();

            int schemaVersion = -1;
            UUID id = null;
            String name = null;
            long createdAt = -1;
            long updatedAt = -1;
            int width = -1;
            int height = -1;
            String format = null;

            var seen = new HashSet<String>();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (!seen.add(key)) {
                    throw new DraftCorruptedException("Duplicate JSON key: " + key);
                }
                switch (key) {
                    case "schemaVersion" -> schemaVersion = reader.nextInt();
                    case "id" -> {
                        try {
                            id = UUID.fromString(reader.nextString());
                        } catch (IllegalArgumentException e) {
                            throw new DraftCorruptedException("Invalid UUID in draft metadata: " + e.getMessage());
                        }
                    }
                    case "name" -> name = reader.nextString();
                    case "createdAt" -> createdAt = reader.nextLong();
                    case "updatedAt" -> updatedAt = reader.nextLong();
                    case "width" -> width = reader.nextInt();
                    case "height" -> height = reader.nextInt();
                    case "format" -> format = reader.nextString();
                    case "pixels" -> reader.skipValue(); // Fast skip when querying metadata
                    default -> reader.skipValue();
                }
            }
            reader.endObject();

            validateHeader(schemaVersion, id, name, createdAt, updatedAt, width, height, format);
            return new DraftMetadata(id, name, createdAt, updatedAt, width, height, schemaVersion);
        }
    }

    private Draft readFullDraft(Path path, UUID expectedId) throws DraftStorageException {
        checkFileSize(path);
        try (BufferedReader br = Files.newBufferedReader(path);
             JsonReader reader = new JsonReader(br)) {

            reader.setStrictness(Strictness.STRICT);
            reader.beginObject();

            int schemaVersion = -1;
            UUID id = null;
            String name = null;
            long createdAt = -1;
            long updatedAt = -1;
            int width = -1;
            int height = -1;
            String format = null;
            String hexPixels = null;

            var seen = new HashSet<String>();
            while (reader.hasNext()) {
                String key = reader.nextName();
                if (!seen.add(key)) {
                    throw new DraftCorruptedException("Duplicate JSON key: " + key);
                }
                switch (key) {
                    case "schemaVersion" -> schemaVersion = reader.nextInt();
                    case "id" -> {
                        try {
                            id = UUID.fromString(reader.nextString());
                        } catch (IllegalArgumentException e) {
                            throw new DraftCorruptedException("Invalid UUID in draft: " + e.getMessage());
                        }
                    }
                    case "name" -> name = reader.nextString();
                    case "createdAt" -> createdAt = reader.nextLong();
                    case "updatedAt" -> updatedAt = reader.nextLong();
                    case "width" -> width = reader.nextInt();
                    case "height" -> height = reader.nextInt();
                    case "format" -> format = reader.nextString();
                    case "pixels" -> hexPixels = reader.nextString();
                    default -> reader.skipValue();
                }
            }
            reader.endObject();

            validateHeader(schemaVersion, id, name, createdAt, updatedAt, width, height, format);

            if (expectedId != null && !expectedId.equals(id)) {
                throw new DraftCorruptedException("Draft UUID mismatch: expected " + expectedId + " but found " + id);
            }

            if (hexPixels == null) {
                throw new DraftCorruptedException("Missing pixels field in draft");
            }

            int expectedPixels = width * height;
            if (hexPixels.length() != expectedPixels * 8) {
                throw new DraftCorruptedException("Encoded pixel length mismatch: expected "
                        + (expectedPixels * 8) + " chars but got " + hexPixels.length());
            }

            int[] pixels = new int[expectedPixels];
            try {
                for (int i = 0; i < expectedPixels; i++) {
                    pixels[i] = HEX.fromHexDigits(hexPixels, i * 8, i * 8 + 8);
                }
            } catch (IllegalArgumentException e) {
                throw new DraftCorruptedException("Invalid hex characters in pixels data: " + e.getMessage(), e);
            }

            DraftMetadata meta = new DraftMetadata(id, name, createdAt, updatedAt, width, height, schemaVersion);
            return new Draft(meta, pixels);

        } catch (DraftStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new DraftCorruptedException("Failed to parse draft JSON from " + path.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private void validateHeader(int schemaVersion, UUID id, String name, long createdAt, long updatedAt,
                                int width, int height, String format) throws DraftStorageException {
        if (schemaVersion < 1) {
            throw new DraftCorruptedException("Invalid schemaVersion: " + schemaVersion);
        }
        if (schemaVersion > CURRENT_SCHEMA_VERSION) {
            throw new DraftVersionException(schemaVersion, "Unsupported newer draft schema version: "
                    + schemaVersion + " (current supported version is " + CURRENT_SCHEMA_VERSION + ")");
        }
        if (id == null) {
            throw new DraftCorruptedException("Missing draft ID");
        }
        if (name == null) {
            throw new DraftCorruptedException("Missing draft name");
        }
        if (width < 1 || width > 256 || height < 1 || height > 256) {
            throw new DraftCorruptedException("Invalid draft dimensions: " + width + "x" + height);
        }
        if (!FORMAT.equals(format)) {
            throw new DraftCorruptedException("Unsupported draft format: " + format + " (expected " + FORMAT + ")");
        }
        if (createdAt < 0 || updatedAt < 0) {
            throw new DraftCorruptedException("Invalid draft timestamps");
        }
    }

    private void checkFileSize(Path path) throws DraftCorruptedException {
        try {
            long size = Files.size(path);
            if (size > MAX_FILE_BYTES) {
                throw new DraftCorruptedException("Draft file size (" + size + " bytes) exceeds maximum limit of " + MAX_FILE_BYTES + " bytes");
            }
        } catch (IOException e) {
            throw new DraftCorruptedException("Cannot determine file size for " + path + ": " + e.getMessage(), e);
        }
    }

    private static boolean safeDelete(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException ignored) {
            return false;
        }
    }
}
