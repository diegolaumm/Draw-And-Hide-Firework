package com.drawfirework.draft;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.Assert.*;

public class DraftSafetyTest {
    private Path tempDir;
    private DraftStorage storage;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("draft_safety_test_");
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
    public void testNewerSchemaFileCannotBeOverwritten() throws Exception {
        Draft draft = storage.createNewDraft("Future Draft", 256, 256, new int[256 * 256]);
        Path file = storage.resolvePath(draft.id(), DraftStorage.FILE_EXTENSION);

        // Simulate a newer version format by changing schemaVersion to 2
        String json = Files.readString(file);
        Files.writeString(file, json.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2"));

        // Attempting to save over it must be rejected with DraftVersionException
        Draft newAttempt = draft.withName("Overwritten Name");
        DraftVersionException ex = assertThrows(DraftVersionException.class, () -> storage.saveDraft(newAttempt));
        assertEquals(2, ex.getVersion());

        // Verify disk content still has version 2 and original name
        String after = Files.readString(file);
        assertTrue(after.contains("\"schemaVersion\": 2"));
        assertTrue(after.contains("Future Draft"));
        assertFalse(after.contains("Overwritten Name"));
    }

    @Test
    public void testCorruptMainFileWithValidHeaderWrongPixelLengthDoesNotOverwriteBackup() throws Exception {
        // Setup valid v1 and v2 so that .bak exists with v1
        int[] v1Pixels = new int[256 * 256];
        Arrays.fill(v1Pixels, 0xFF111111);
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);

        int[] v2Pixels = new int[256 * 256];
        Arrays.fill(v2Pixels, 0xFF222222);
        Draft v2 = v1.withPixels(v2Pixels);
        storage.saveDraft(v2);

        Path bakFile = storage.resolvePath(v1.id(), DraftStorage.BACKUP_EXTENSION);
        byte[] originalBakBytes = Files.readAllBytes(bakFile);

        // Corrupt main file: valid JSON header, but pixels length is truncated
        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        String mainContent = Files.readString(mainFile);
        // Replace pixels with a truncated hex string (e.g. only 100 characters instead of 524288)
        String corruptContent = mainContent.replaceAll("\"pixels\":\\s*\"[0-9a-fA-F]+\"", "\"pixels\": \"aabbccdd\"");
        Files.writeString(mainFile, corruptContent);

        // Attempting to save v3 must be refused because main file is corrupt and valid .bak exists
        int[] v3Pixels = new int[256 * 256];
        Arrays.fill(v3Pixels, 0xFF333333);
        Draft v3 = v1.withPixels(v3Pixels);
        DraftStorageException ex = assertThrows(DraftStorageException.class, () -> storage.saveDraft(v3));
        assertTrue(ex.getMessage().contains("valid backup (.bak) exists"));

        // Verify .bak bytes are 100% UNCHANGED
        byte[] currentBakBytes = Files.readAllBytes(bakFile);
        assertArrayEquals("Existing valid .bak must NOT be overwritten by corrupt main file", originalBakBytes, currentBakBytes);
    }

    @Test
    public void testCorruptMainFileWithIllegalHexDoesNotOverwriteBackup() throws Exception {
        // Setup valid v1 and v2
        int[] v1Pixels = new int[256 * 256];
        Arrays.fill(v1Pixels, 0xFFAAAAAA);
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);

        int[] v2Pixels = new int[256 * 256];
        Arrays.fill(v2Pixels, 0xFFBBBBBB);
        Draft v2 = v1.withPixels(v2Pixels);
        storage.saveDraft(v2);

        Path bakFile = storage.resolvePath(v1.id(), DraftStorage.BACKUP_EXTENSION);
        byte[] originalBakBytes = Files.readAllBytes(bakFile);

        // Corrupt main file: valid JSON header and correct length, but contains non-hex characters (e.g. 'Z')
        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        String mainContent = Files.readString(mainFile);
        String corruptContent = mainContent.replace("ffbbbbbb", "ZZZZZZZZ");
        assertTrue("Corruption string replacement must take effect", corruptContent.contains("ZZZZZZZZ"));
        Files.writeString(mainFile, corruptContent);

        // Attempting to save v3 must be refused because main file contains invalid hex
        Draft v3 = v1.withPixels(new int[256 * 256]);
        DraftStorageException ex = assertThrows(DraftStorageException.class, () -> storage.saveDraft(v3));
        assertTrue(ex.getMessage().contains("valid backup (.bak) exists"));

        // Verify .bak bytes remain 100% intact
        assertArrayEquals(originalBakBytes, Files.readAllBytes(bakFile));
    }

    @Test
    public void testCorruptMainFileWithUuidMismatchDoesNotOverwriteBackup() throws Exception {
        // Setup valid v1 and v2
        int[] v1Pixels = new int[256 * 256];
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);
        Draft v2 = v1.withName("Version 2");
        storage.saveDraft(v2);

        Path bakFile = storage.resolvePath(v1.id(), DraftStorage.BACKUP_EXTENSION);
        byte[] originalBakBytes = Files.readAllBytes(bakFile);

        // Corrupt main file: JSON id does not match filename UUID
        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        String mainContent = Files.readString(mainFile);
        String mismatchedContent = mainContent.replace(v1.id().toString(), UUID.randomUUID().toString());
        Files.writeString(mainFile, mismatchedContent);

        // Attempting to save must be refused
        Draft v3 = v1.withName("Version 3");
        DraftStorageException ex = assertThrows(DraftStorageException.class, () -> storage.saveDraft(v3));
        assertTrue(ex.getMessage().contains("valid backup (.bak) exists"));

        // Verify .bak bytes remain 100% intact
        assertArrayEquals(originalBakBytes, Files.readAllBytes(bakFile));
    }

    @Test
    public void testValidMainFileBacksUpAndOverwritesNormally() throws Exception {
        int[] v1Pixels = new int[256 * 256];
        v1Pixels[0] = 0xFF111111;
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);

        int[] v2Pixels = new int[256 * 256];
        v2Pixels[0] = 0xFF222222;
        Draft v2 = v1.withPixels(v2Pixels);
        storage.saveDraft(v2);

        // Check backup contains v1
        Path bakFile = storage.resolvePath(v1.id(), DraftStorage.BACKUP_EXTENSION);
        assertTrue(Files.exists(bakFile));
        Draft loadedBak = storage.loadDraft(v1.id()); // loaded main
        assertEquals(0xFF222222, loadedBak.pixels()[0]);

        // Overwrite again with v3
        int[] v3Pixels = new int[256 * 256];
        v3Pixels[0] = 0xFF333333;
        Draft v3 = v1.withPixels(v3Pixels);
        storage.saveDraft(v3);

        // Backup now contains v2
        assertTrue(Files.readString(bakFile).contains("222222"));
        // Main file contains v3
        Draft loadedMain = storage.loadDraft(v1.id());
        assertEquals(0xFF333333, loadedMain.pixels()[0]);
    }

    @Test
    public void testRecoveryNeverOverwritesNewerSchemaEvenWithForce() throws Exception {
        // Create draft and backup
        Draft v1 = storage.createNewDraft("Old Backup V1", 256, 256, new int[256 * 256]);
        Draft v2 = v1.withName("Old Backup V2");
        storage.saveDraft(v2);

        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        Path bakFile = storage.resolvePath(v1.id(), DraftStorage.BACKUP_EXTENSION);

        // Alter mainFile to schemaVersion 2
        String mainContent = Files.readString(mainFile);
        Files.writeString(mainFile, mainContent.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2"));

        byte[] mainBytesBefore = Files.readAllBytes(mainFile);
        byte[] bakBytesBefore = Files.readAllBytes(bakFile);

        // Standard recoverFromBackup must throw DraftVersionException
        DraftVersionException ex1 = assertThrows(DraftVersionException.class, () -> storage.recoverFromBackup(v1.id(), false));
        assertEquals(2, ex1.getVersion());

        // Force recoverFromBackup must ALSO throw DraftVersionException (force cannot bypass newer schema)
        DraftVersionException ex2 = assertThrows(DraftVersionException.class, () -> storage.recoverFromBackup(v1.id(), true));
        assertEquals(2, ex2.getVersion());

        // Verify both files remain 100% byte-for-byte unchanged
        assertArrayEquals("Main file with newer schema must NOT be modified", mainBytesBefore, Files.readAllBytes(mainFile));
        assertArrayEquals("Backup file must NOT be modified", bakBytesBefore, Files.readAllBytes(bakFile));
    }

    @Test
    public void testLegitimateCorruptMainFileRecoverableFromBackup() throws Exception {
        int[] v1Pixels = new int[256 * 256];
        v1Pixels[0] = 0xFF445566;
        Draft v1 = storage.createNewDraft("Version 1", 256, 256, v1Pixels);

        int[] v2Pixels = new int[256 * 256];
        v2Pixels[0] = 0xFF778899;
        Draft v2 = v1.withPixels(v2Pixels);
        storage.saveDraft(v2);

        // Corrupt main file (e.g. truncated)
        Path mainFile = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        Files.writeString(mainFile, "{ truncated garbage");

        // Recovery succeeds and restores v1 from backup
        assertTrue(storage.recoverFromBackup(v1.id()));
        Draft recovered = storage.loadDraft(v1.id());
        assertEquals(0xFF445566, recovered.pixels()[0]);
    }

    @Test
    public void testRecoveryRefusedWhenTargetIsValidAndIntact() throws Exception {
        Draft v1 = storage.createNewDraft("Target Valid", 256, 256, new int[256 * 256]);
        Draft v2 = v1.withName("Target Valid V2");
        storage.saveDraft(v2);

        assertTrue(storage.hasBackup(v1.id()));

        // Calling recoverFromBackup without force must fail because target file is valid and intact
        DraftStorageException ex = assertThrows(DraftStorageException.class, () -> storage.recoverFromBackup(v1.id(), false));
        assertTrue(ex.getMessage().contains("already valid and intact"));

        // Calling with force = true succeeds on valid version 1 file
        assertTrue(storage.recoverFromBackup(v1.id(), true));
        Draft recovered = storage.loadDraft(v1.id());
        assertEquals("Target Valid", recovered.name());
    }

    @Test
    public void testTempFilesAreCleanedUp() throws Exception {
        for (int i = 0; i < 5; i++) {
            storage.createNewDraft("Draft " + i, 256, 256, new int[256 * 256]);
        }

        try (var stream = Files.list(tempDir)) {
            long tmpCount = stream.filter(p -> p.getFileName().toString().endsWith(".tmp")).count();
            assertEquals("No dangling .tmp files should remain after successful operations", 0, tmpCount);
        }
    }
}
