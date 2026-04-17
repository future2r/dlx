package name.ulbricht.dlx.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("User preferences")
final class UserPreferencesTest {

    private UserPreferences prefs;

    @BeforeEach
    void setUp() {
        InMemoryPreferencesFactory.reset();
        this.prefs = new UserPreferences();
    }

    @Test
    @DisplayName("getStageState returns default normal state when nothing saved")
    void stageStateDefaultByDefault() {
        final var result = this.prefs.getStageState("main");
        assertTrue(result.isPresent());
        assertFalse(result.get().maximized());
        assertEquals(StageState.UNDEFINED, result.get().x());
        assertEquals(StageState.UNDEFINED, result.get().y());
        assertEquals(StageState.UNDEFINED, result.get().width());
        assertEquals(StageState.UNDEFINED, result.get().height());
    }

    @Test
    @DisplayName("putStageState then getStageState round-trips correctly")
    void stageStateRoundTrip() {
        final var expected = StageState.ofNormal(100, 200, 800, 600);
        this.prefs.putStageState("main", expected);

        final var restored = this.prefs.getStageState("main");
        assertTrue(restored.isPresent());
        assertEquals(expected, restored.get());
        assertFalse(restored.get().maximized());
    }

    @Test
    @DisplayName("putStageState with maximized stores only the maximized flag")
    void stageStateMaximized() {
        this.prefs.putStageState("main", StageState.ofMaximized());

        final var restored = this.prefs.getStageState("main");
        assertTrue(restored.isPresent());
        assertTrue(restored.get().maximized());
        assertEquals(StageState.UNDEFINED, restored.get().x());
        assertEquals(StageState.UNDEFINED, restored.get().y());
        assertEquals(StageState.UNDEFINED, restored.get().width());
        assertEquals(StageState.UNDEFINED, restored.get().height());
    }

    @Test
    @DisplayName("putStageState with null removes the saved values")
    void stageStateRemoval() {
        this.prefs.putStageState("main", StageState.ofNormal(10, 20, 640, 480));

        this.prefs.putStageState("main", null);

        final var restored = this.prefs.getStageState("main");
        assertTrue(restored.isPresent());
        assertFalse(restored.get().maximized());
        assertEquals(StageState.UNDEFINED, restored.get().x());
        assertEquals(StageState.UNDEFINED, restored.get().y());
        assertEquals(StageState.UNDEFINED, restored.get().width());
        assertEquals(StageState.UNDEFINED, restored.get().height());
    }

    @Test
    @DisplayName("Different stage IDs are independent")
    void stageStateIndependentIds() {
        final var stateA = StageState.ofNormal(0, 0, 500, 400);
        final var stateB = StageState.ofMaximized();

        this.prefs.putStageState("main", stateA);
        this.prefs.putStageState("other", stateB);

        final var restoredA = this.prefs.getStageState("main");
        final var restoredB = this.prefs.getStageState("other");

        assertTrue(restoredA.isPresent());
        assertEquals(stateA, restoredA.get());
        assertFalse(restoredA.get().maximized());

        assertTrue(restoredB.isPresent());
        assertTrue(restoredB.get().maximized());
    }

    @Nested
    @DisplayName("Recent Files")
    final class RecentFiles {

        @Test
        @DisplayName("recent files list is empty by default")
        void emptyByDefault() {
            assertTrue(UserPreferencesTest.this.prefs.getRecentFiles().isEmpty());
        }

        @Test
        @DisplayName("addRecentFile adds file to the list")
        void addRecentFile() {
            final var file = Path.of("/tmp/test.s");
            UserPreferencesTest.this.prefs.addRecentFile(file);

            assertEquals(List.of(file), UserPreferencesTest.this.prefs.getRecentFiles());
        }

        @Test
        @DisplayName("addRecentFile moves existing file to the front")
        void addRecentFileMovesToFront() {
            final var file1 = Path.of("/tmp/a.s");
            final var file2 = Path.of("/tmp/b.s");
            final var file3 = Path.of("/tmp/c.s");

            UserPreferencesTest.this.prefs.addRecentFile(file1);
            UserPreferencesTest.this.prefs.addRecentFile(file2);
            UserPreferencesTest.this.prefs.addRecentFile(file3);

            // Re-add file1 — should move to front
            UserPreferencesTest.this.prefs.addRecentFile(file1);

            assertEquals(List.of(file1, file3, file2), UserPreferencesTest.this.prefs.getRecentFiles());
        }

        @Test
        @DisplayName("addRecentFile limits list to five entries")
        void limitsToFive() {
            for (var i = 0; i < 7; i++)
                UserPreferencesTest.this.prefs.addRecentFile(Path.of("/tmp/file" + i + ".s"));

            assertEquals(5, UserPreferencesTest.this.prefs.getRecentFiles().size());
            assertEquals(Path.of("/tmp/file6.s"), UserPreferencesTest.this.prefs.getRecentFiles().getFirst());
            assertEquals(Path.of("/tmp/file2.s"), UserPreferencesTest.this.prefs.getRecentFiles().getLast());
        }

        @Test
        @DisplayName("removeRecentFile removes the file from the list")
        void removeRecentFile() {
            final var file1 = Path.of("/tmp/a.s");
            final var file2 = Path.of("/tmp/b.s");

            UserPreferencesTest.this.prefs.addRecentFile(file1);
            UserPreferencesTest.this.prefs.addRecentFile(file2);
            UserPreferencesTest.this.prefs.removeRecentFile(file1);

            assertEquals(List.of(file2), UserPreferencesTest.this.prefs.getRecentFiles());
        }

        @Test
        @DisplayName("clearRecentFiles empties the list")
        void clearRecentFiles() {
            UserPreferencesTest.this.prefs.addRecentFile(Path.of("/tmp/a.s"));
            UserPreferencesTest.this.prefs.addRecentFile(Path.of("/tmp/b.s"));
            UserPreferencesTest.this.prefs.clearRecentFiles();

            assertTrue(UserPreferencesTest.this.prefs.getRecentFiles().isEmpty());
        }
    }
}
