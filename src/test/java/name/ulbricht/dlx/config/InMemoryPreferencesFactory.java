package name.ulbricht.dlx.config;

import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

/// A [PreferencesFactory] that returns in-memory preference trees. Activated
/// via the system property `-Djava.util.prefs.PreferencesFactory`.
public final class InMemoryPreferencesFactory implements PreferencesFactory {

    private static Preferences userRoot = new InMemoryPreferences(null, "");
    private static Preferences systemRoot = new InMemoryPreferences(null, "");

    /// Creates a new in-memory preferences factory.
    public InMemoryPreferencesFactory() {
    }

    @Override
    public Preferences userRoot() {
        return userRoot;
    }

    @Override
    public Preferences systemRoot() {
        return systemRoot;
    }

    /// Resets both root nodes to fresh, empty instances. Call from test setup to
    /// ensure isolation between tests.
    public static void reset() {
        userRoot = new InMemoryPreferences(null, "");
        systemRoot = new InMemoryPreferences(null, "");
    }
}
