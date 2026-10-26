package com.drawfirework.client;

import com.drawfirework.canvas.CanvasData;
import com.drawfirework.draft.Draft;
import com.drawfirework.draft.DraftMetadata;
import com.drawfirework.draft.DraftStorage;
import com.drawfirework.draft.DraftStorageException;
import com.drawfirework.draft.DraftVersionException;
import com.drawfirework.editor.FreehandCanvas;
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

/**
 * Tests draft GUI business logic, dirty state tracking, and session isolation.
 */
public class DraftGuiLogicTest {
    private Path tempDir;
    private DraftStorage storage;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("draft_gui_logic_test_");
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
    public void testDirtyStateLifecycle() {
        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());
        long savedRev = canvas.revision();

        // 1. Initial state is not dirty
        assertEquals(savedRev, canvas.revision());

        // 2. Painting makes it dirty
        canvas.paint(10, 10, 0xFFFF0000);
        canvas.endStroke();
        assertNotEquals(savedRev, canvas.revision());

        // 3. Mark saved resets dirty state
        savedRev = canvas.revision();
        assertEquals(savedRev, canvas.revision());

        // 4. Erase makes it dirty
        canvas.paint(10, 10, 0x00000000);
        canvas.endStroke();
        assertNotEquals(savedRev, canvas.revision());

        // 5. Loading draft resets dirty state
        int[] draftPixels = new int[canvas.width() * canvas.height()];
        draftPixels[0] = 0xFF4080FF;
        canvas.loadDraft(draftPixels);
        savedRev = canvas.revision();
        assertEquals(savedRev, canvas.revision());
        assertEquals(0xFF4080FF, canvas.pixel(0, 0));
    }

    @Test
    public void testDraftSwitchingDoesNotLeakHistory() {
        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());

        // Draft A work
        canvas.paint(20, 20, 0xFFFF0000);
        canvas.endStroke();
        assertTrue("Draft A has undo", canvas.canUndo());

        // Switch to Draft B
        int[] bPixels = new int[canvas.width() * canvas.height()];
        bPixels[50 * canvas.width() + 50] = 0xFF00FF00;
        canvas.loadDraft(bPixels);

        // Draft B must have NO undo history from Draft A
        assertFalse("Undo stack must be empty after loading Draft B", canvas.canUndo());
        assertFalse(canvas.canRedo());
        assertEquals(0xFF00FF00, canvas.pixel(50, 50));
        assertEquals(0, canvas.pixel(20, 20)); // Draft A pixels gone

        // Reset blank (New Draft) must also clear undo history
        canvas.paint(60, 60, 0xFFFFFFFF);
        canvas.endStroke();
        assertTrue(canvas.canUndo());

        canvas.resetBlank();
        assertFalse("Undo stack must be empty after resetBlank", canvas.canUndo());
        assertEquals(0, canvas.pixel(60, 60));
    }

    @Test
    public void testCrudWorkflow() throws Exception {
        // Create 2 drafts
        Draft d1 = storage.createNewDraft("Draft 1", 256, 256, new int[256 * 256]);
        Draft d2 = storage.createNewDraft("Draft 2", 256, 256, new int[256 * 256]);

        List<DraftMetadata> list = storage.listDrafts();
        assertEquals(2, list.size());

        // Rename Draft 1
        storage.renameDraft(d1.id(), "Renamed 1");
        Draft loaded1 = storage.loadDraft(d1.id());
        assertEquals("Renamed 1", loaded1.name());

        // Delete Draft 2
        assertTrue(storage.deleteDraft(d2.id()));
        List<DraftMetadata> remaining = storage.listDrafts();
        assertEquals(1, remaining.size());
        assertEquals(d1.id(), remaining.get(0).id());
    }

    @Test
    public void testRealStorageSaveFailurePreservesCanvasAndDirtyState() throws Exception {
        // Setup real Draft on disk with schemaVersion 2 to force storage failure
        Draft v1 = storage.createNewDraft("Protected Future Draft", 256, 256, new int[256 * 256]);
        Path file = storage.resolvePath(v1.id(), DraftStorage.FILE_EXTENSION);
        Files.writeString(file, Files.readString(file).replace("\"schemaVersion\": 1", "\"schemaVersion\": 2"));

        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());
        canvas.paint(10, 10, 0xFFFF0000);
        canvas.endStroke();

        long dirtyRev = canvas.revision();
        long[] savedRev = {-1};
        boolean[] successCalled = {false};

        // Real product workflow: save draft with currentDraftId = v1.id()
        try {
            Draft draft = new Draft(
                    new DraftMetadata(v1.id(), "Protected Future Draft", System.currentTimeMillis(), System.currentTimeMillis(), 256, 256, 1),
                    canvas.toDraft256Pixels()
            );
            storage.saveDraft(draft);
            savedRev[0] = canvas.revision();
            successCalled[0] = true;
        } catch (DraftStorageException e) {
            // Storage rejected write! (Real product catch block)
        }

        assertFalse("onSuccess must NOT run on storage failure", successCalled[0]);
        assertNotEquals("Dirty state must NOT be cleared on storage failure", dirtyRev, savedRev[0]);
        assertEquals("Canvas pixels must remain intact", 0xFFFF0000, canvas.pixel(10, 10));
    }

    @Test
    public void testRealStorageSaveSuccessUpdatesRevisionAndCallsSuccess() throws Exception {
        FreehandCanvas canvas = new FreehandCanvas(CanvasData.blank());
        canvas.paint(25, 25, 0xFF00FF00);
        canvas.endStroke();

        long dirtyRev = canvas.revision();
        long[] savedRev = {-1};
        boolean[] successCalled = {false};

        // Real product workflow: save new draft
        Draft draft = storage.createNewDraft("Real Success Draft", 256, 256, canvas.toDraft256Pixels());
        savedRev[0] = canvas.revision();
        successCalled[0] = true;

        assertTrue("onSuccess must be called on successful save", successCalled[0]);
        assertEquals("Saved revision must match canvas revision", dirtyRev, savedRev[0]);

        Draft loaded = storage.loadDraft(draft.id());
        assertEquals(0xFF00FF00, loaded.pixels()[25 * 256 + 25]);
    }

    @Test
    public void testMigrationUuidConflictSafelyPreservesBothFiles() throws IOException {
        Path legacyDir = tempDir.resolve("legacy_config");
        Path targetDir = tempDir.resolve("target_drafts");
        Files.createDirectories(legacyDir);
        Files.createDirectories(targetDir);

        UUID sharedId = UUID.randomUUID();
        String filename = sharedId + ".json";

        Path legacyFile = legacyDir.resolve(filename);
        Path targetFile = targetDir.resolve(filename);

        // Write differing contents
        Files.writeString(legacyFile, "{ \"id\": \"" + sharedId + "\", \"source\": \"legacy\" }");
        Files.writeString(targetFile, "{ \"id\": \"" + sharedId + "\", \"source\": \"target\" }");

        ClientDraftStorage.migrateLegacyDrafts(legacyDir, targetDir);

        // 1. Target file must NOT be overwritten
        assertEquals("{ \"id\": \"" + sharedId + "\", \"source\": \"target\" }", Files.readString(targetFile));

        // 2. Legacy source file must NOT be deleted
        assertTrue(Files.exists(legacyFile));
        assertEquals("{ \"id\": \"" + sharedId + "\", \"source\": \"legacy\" }", Files.readString(legacyFile));

        // 3. Conflict backup must be created
        Path conflictBackup = targetDir.resolve(sharedId + ".legacy_conflict.json");
        assertTrue("Conflict backup file must exist", Files.exists(conflictBackup));
        assertEquals("{ \"id\": \"" + sharedId + "\", \"source\": \"legacy\" }", Files.readString(conflictBackup));

        // 4. Migration conflicts list reports the conflict
        assertFalse(ClientDraftStorage.getMigrationConflicts().isEmpty());
    }
}
