package com.drawfirework.draft;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

public class DraftStorageTest {
    private Path tempDir;
    private DraftStorage storage;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("draft_storage_test_");
        storage = new DraftStorage(tempDir);
    }

    @After
    public void tearDown() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {}
                        });
            }
        }
    }

    @Test
    public void testFull256LosslessRoundtrip() throws Exception {
        int w = 256, h = 256;
        int[] pixels = new int[w * h];

        // Fill with various colors, alpha levels, and patterns
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                if ((x + y) % 5 == 0) {
                    pixels[idx] = 0x00000000; // Fully transparent
                } else if ((x + y) % 5 == 1) {
                    pixels[idx] = 0x80FF4040; // Semi-transparent red
                } else if ((x + y) % 5 == 2) {
                    pixels[idx] = 0xFF4080FF; // Opaque blue
                } else if ((x + y) % 5 == 3) {
                    pixels[idx] = 0x3300FF88; // Low alpha green
                } else {
                    pixels[idx] = 0xFF181818; // Dark gray
                }
            }
        }

        Draft draft = storage.createNewDraft("Test 256 Art", w, h, pixels);
        assertNotNull(draft.id());
        assertEquals("Test 256 Art", draft.name());

        Draft loaded = storage.loadDraft(draft.id());
        assertEquals(draft.id(), loaded.id());
        assertEquals("Test 256 Art", loaded.name());
        assertEquals(w, loaded.width());
        assertEquals(h, loaded.height());
        assertEquals(w * h, loaded.pixelCount());

        int[] loadedPixels = loaded.pixels();
        assertArrayEquals("All 65,536 pixels must match exactly with zero loss", pixels, loadedPixels);
    }

    @Test
    public void testMultipleDraftsManagement() throws Exception {
        Draft d1 = storage.createNewDraft("Draft Alpha", 256, 256, new int[256 * 256]);
        Thread.sleep(10); // Ensure timestamp difference
        Draft d2 = storage.createNewDraft("Draft Beta", 256, 256, new int[256 * 256]);
        Thread.sleep(10);
        Draft d3 = storage.createNewDraft("Draft Gamma", 256, 256, new int[256 * 256]);

        List<DraftMetadata> list = storage.listDrafts();
        assertEquals(3, list.size());
        // Ordered by updatedAt descending: d3, d2, d1
        assertEquals("Draft Gamma", list.get(0).name());
        assertEquals("Draft Beta", list.get(1).name());
        assertEquals("Draft Alpha", list.get(2).name());

        // Delete d2
        assertTrue(storage.deleteDraft(d2.id()));
        assertEquals(2, storage.listDrafts().size());

        assertFalse("Deleted draft must not exist", storage.listDrafts().stream().anyMatch(m -> m.id().equals(d2.id())));
    }

    @Test
    public void testSaveAsDoesNotOverwriteOriginal() throws Exception {
        int[] originalPixels = new int[256 * 256];
        originalPixels[0] = 0xFFFF0000;
        Draft original = storage.createNewDraft("Original Draft", 256, 256, originalPixels);

        // Modify and Save As
        int[] modifiedPixels = originalPixels.clone();
        modifiedPixels[1] = 0xFF00FF00;
        Draft copy = storage.saveDraftAs(new Draft(original.metadata(), modifiedPixels), "Copy Draft");

        assertNotEquals(original.id(), copy.id());
        assertEquals("Copy Draft", copy.name());

        Draft loadedOriginal = storage.loadDraft(original.id());
        Draft loadedCopy = storage.loadDraft(copy.id());

        assertEquals("Original Draft", loadedOriginal.name());
        assertEquals(0, loadedOriginal.pixels()[1]); // Not modified

        assertEquals("Copy Draft", loadedCopy.name());
        assertEquals(0xFF00FF00, loadedCopy.pixels()[1]); // Modified
    }

    @Test
    public void testRenamePreservesPixels() throws Exception {
        int[] pixels = new int[256 * 256];
        pixels[100] = 0xFFABCDEF;
        Draft draft = storage.createNewDraft("Before Rename", 256, 256, pixels);

        Draft renamed = storage.renameDraft(draft.id(), "After Rename");
        assertEquals("After Rename", renamed.name());
        assertEquals(draft.id(), renamed.id());

        Draft loaded = storage.loadDraft(draft.id());
        assertEquals("After Rename", loaded.name());
        assertEquals(0xFFABCDEF, loaded.pixels()[100]);
    }

    @Test
    public void testBackupCreatedOnOverwriteAndRecoverable() throws Exception {
        int[] v1Pixels = new int[256 * 256];
        v1Pixels[0] = 0xFF111111;
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);
        assertFalse("First save has no backup yet", storage.hasBackup(v1.id()));

        // Second save creates backup of v1
        int[] v2Pixels = new int[256 * 256];
        v2Pixels[0] = 0xFF222222;
        Draft v2 = v1.withPixels(v2Pixels);
        storage.saveDraft(v2);

        assertTrue("Second save must produce .bak file", storage.hasBackup(v1.id()));

        // Simulate main file corruption by truncating the main file
        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        Files.writeString(mainFile, "{ \"corrupt\": true, \"truncated");

        // Loading corrupted file throws DraftCorruptedException without crashing
        assertThrows(DraftCorruptedException.class, () -> storage.loadDraft(v1.id()));

        // Main file remains on disk (not deleted!)
        assertTrue(Files.exists(mainFile));

        // Recovery restores v1 from backup
        assertTrue(storage.recoverFromBackup(v1.id()));
        Draft recovered = storage.loadDraft(v1.id());
        assertEquals(0xFF111111, recovered.pixels()[0]);
    }

    @Test
    public void testUnsupportedNewerSchemaVersionSafelyRejected() throws Exception {
        Draft draft = storage.createNewDraft("Version Test", 256, 256, new int[256 * 256]);
        Path file = storage.resolvePath(draft.id(), DraftStorage.FILE_EXTENSION);

        // Alter schemaVersion to 999
        String content = Files.readString(file);
        content = content.replace("\"schemaVersion\": 1", "\"schemaVersion\": 999");
        Files.writeString(file, content);

        DraftVersionException ex = assertThrows(DraftVersionException.class, () -> storage.loadDraft(draft.id()));
        assertEquals(999, ex.getVersion());
        assertTrue(ex.getMessage().contains("Unsupported newer draft schema version"));

        // File remains on disk unmodified
        assertTrue(Files.exists(file));
    }

    @Test
    public void testCorruptedFilesHandledSafelyInList() throws Exception {
        Draft valid = storage.createNewDraft("Valid Draft", 256, 256, new int[256 * 256]);

        // Create a malformed JSON file
        UUID corruptId = UUID.randomUUID();
        Path corruptPath = storage.resolvePath(corruptId, DraftStorage.FILE_EXTENSION);
        Files.writeString(corruptPath, "{ broken json content");

        // listDrafts must succeed and contain only the valid draft
        List<DraftMetadata> list = storage.listDrafts();
        assertEquals(1, list.size());
        assertEquals("Valid Draft", list.get(0).name());

        // Corrupted file is NOT deleted
        assertTrue(Files.exists(corruptPath));
    }

    @Test
    public void testPathTraversalProtection() {
        assertThrows(SecurityException.class, () -> {
            storage.resolvePath(UUID.fromString("00000000-0000-0000-0000-000000000000"), "/../../evil.json");
        });
    }

    @Test
    public void testOversizedFileRejectedBeforeParsing() throws Exception {
        UUID bigId = UUID.randomUUID();
        Path bigFile = storage.resolvePath(bigId, DraftStorage.FILE_EXTENSION);
        // Create 2.5 MB dummy file
        byte[] large = new byte[2_500_000];
        Files.write(bigFile, large);

        DraftCorruptedException ex = assertThrows(DraftCorruptedException.class, () -> storage.loadDraft(bigId));
        assertTrue(ex.getMessage().contains("exceeds maximum limit"));
    }

    @Test
    public void testNameSanitization() {
        assertEquals("Untitled", DraftMetadata.sanitizeName(null));
        assertEquals("Untitled", DraftMetadata.sanitizeName("   "));
        assertEquals("My_Draft", DraftMetadata.sanitizeName("My/Draft"));
        assertEquals("Test_Firework", DraftMetadata.sanitizeName("Test\\Firework"));
        assertEquals("A".repeat(64), DraftMetadata.sanitizeName("A".repeat(100)));
    }
}
