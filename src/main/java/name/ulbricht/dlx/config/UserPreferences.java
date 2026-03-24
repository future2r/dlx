package name.ulbricht.dlx.config;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.prefs.Preferences;

/// User preferences.
public final class UserPreferences {

    private static UserPreferences instance;

    /// Get the singleton instance.
    ///
    /// @return the singleton instance
    public static UserPreferences getInstance() {
        if (instance == null)
            instance = new UserPreferences();
        return instance;
    }

    private static final String ROOT_NODE = "name.ulbricht.dlx";
    private static final String MOST_RECENTLY_USED_DIRECTORY_KEY = "mostRecentlyUsedDirectory";

    private final Preferences preferences;

    private UserPreferences() {
        this.preferences = Preferences.userRoot().node(ROOT_NODE);
    }

    /// {@return the most recently used directory, or the user's home directory if
    /// not set or invalid.}
    public Path getMostRecentlyUsedDirectory() {
        return getDirectory(MOST_RECENTLY_USED_DIRECTORY_KEY);
    }

    /// Set the most recently used directory.
    /// 
    /// @param directory the directory to set, or null to remove the preference
    public void putMostRecentlyUsedDirectory(final Path directory) {
        putPath(MOST_RECENTLY_USED_DIRECTORY_KEY, directory);
    }

    /// Returns the directory stored under the given key, or {@code null} if absent or invalid.
    ///
    /// @param  key the preference key
    /// @return the stored directory path, or {@code null}
    public Path getDirectory(final String key) {
        final var value = this.preferences.get(key, null);
        if (value != null) {
            try {
                final var path = Path.of(value);
                if ((Files.isDirectory(path)))
                    return path;
            } catch (final InvalidPathException _) {
                // Ignore invalid path
            }
        }
        return Path.of(System.getProperty("user.home"));
    }

    private void putPath(final String key, final Path path) {
        if (path != null)
            this.preferences.put(key, path.toString());
        else
            this.preferences.remove(key);
    }
}
