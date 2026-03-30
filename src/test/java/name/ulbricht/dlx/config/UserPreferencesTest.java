package name.ulbricht.dlx.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javafx.geometry.Rectangle2D;
import name.ulbricht.dlx.ui.stage.WindowState;

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
        assertTrue(this.prefs.getWindowState("main").isEmpty());
    }

    @Test
    @DisplayName("putWindowState then getWindowState round-trips correctly")
    void windowStateRoundTrip() {
        final var bounds = new Rectangle2D(100, 200, 800, 600);
        this.prefs.putWindowState("main", new WindowState(bounds, false));

        final var restored = this.prefs.getWindowState("main");
        assertTrue(restored.isPresent());
        assertEquals(bounds, restored.get().bounds());
        assertFalse(restored.get().maximized());
    }

    @Test
    @DisplayName("putWindowState preserves maximized flag")
    void windowStateMaximized() {
        final var bounds = new Rectangle2D(50, 50, 1024, 768);
        this.prefs.putWindowState("main", new WindowState(bounds, true));

        final var restored = this.prefs.getWindowState("main");
        assertTrue(restored.isPresent());
        assertEquals(bounds, restored.get().bounds());
        assertTrue(restored.get().maximized());
    }

    @Test
    @DisplayName("putWindowState with null removes the sub-node")
    void windowStateRemoval() {
        final var bounds = new Rectangle2D(10, 20, 640, 480);
        this.prefs.putWindowState("main", new WindowState(bounds, false));

        this.prefs.putWindowState("main", null);

        assertTrue(this.prefs.getWindowState("main").isEmpty());
    }

    @Test
    @DisplayName("Different window IDs are independent")
    void windowStateIndependentIds() {
        final var boundsA = new Rectangle2D(0, 0, 500, 400);
        final var boundsB = new Rectangle2D(100, 100, 300, 200);

        this.prefs.putWindowState("main", new WindowState(boundsA, false));
        this.prefs.putWindowState("other", new WindowState(boundsB, true));

        final var restoredA = this.prefs.getWindowState("main");
        final var restoredB = this.prefs.getWindowState("other");

        assertEquals(boundsA, restoredA.get().bounds());
        assertFalse(restoredA.get().maximized());

        assertEquals(boundsB, restoredB.get().bounds());
        assertTrue(restoredB.get().maximized());
    }
}
