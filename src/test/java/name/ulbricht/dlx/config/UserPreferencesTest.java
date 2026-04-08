package name.ulbricht.dlx.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    @DisplayName("getWindowState returns empty when nothing saved")
    void windowStateEmptyByDefault() {
        assertNull(this.prefs.getWindowState("main"));
    }

    @Test
    @DisplayName("putWindowState then getWindowState round-trips correctly")
    void windowStateRoundTrip() {
        final var expected = new WindowState(false, 100, 200, 800, 600);
        this.prefs.putWindowState("main", expected);

        final var restored = this.prefs.getWindowState("main");
        assertNotNull(restored);
        assertEquals(expected, restored);
        assertFalse(restored.maximized());
    }

    @Test
    @DisplayName("putWindowState with maximized stores only the maximized flag")
    void windowStateMaximized() {
        this.prefs.putWindowState("main", new WindowState(true, Double.NaN, Double.NaN, Double.NaN, Double.NaN));

        final var restored = this.prefs.getWindowState("main");
        assertNotNull(restored);
        assertTrue(restored.maximized());
        assertTrue(Double.isNaN(restored.x()));
        assertTrue(Double.isNaN(restored.y()));
        assertTrue(Double.isNaN(restored.width()));
        assertTrue(Double.isNaN(restored.height()));
    }

    @Test
    @DisplayName("putWindowState with null removes the sub-node")
    void windowStateRemoval() {
        this.prefs.putWindowState("main", new WindowState(false, 10, 20, 640, 480));

        this.prefs.putWindowState("main", null);

        assertNull(this.prefs.getWindowState("main"));
    }

    @Test
    @DisplayName("Different window IDs are independent")
    void windowStateIndependentIds() {
        final var stateA = new WindowState(false, 0, 0, 500, 400);
        final var stateB = new WindowState(true, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

        this.prefs.putWindowState("main", stateA);
        this.prefs.putWindowState("other", stateB);

        final var restoredA = this.prefs.getWindowState("main");
        final var restoredB = this.prefs.getWindowState("other");

        assertEquals(stateA, restoredA);
        assertFalse(restoredA.maximized());

        assertTrue(restoredB.maximized());
    }

    @Nested
    @DisplayName("Recent Files")
    class RecentFiles {

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
