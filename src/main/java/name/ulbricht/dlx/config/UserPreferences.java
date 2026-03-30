package name.ulbricht.dlx.config;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Rectangle2D;
import name.ulbricht.dlx.ui.scene.Theme;
import name.ulbricht.dlx.ui.stage.WindowState;

/// User preferences.
public final class UserPreferences {

    private static final String ROOT_NODE = "name/ulbricht/dlx";
    private static final String MOST_RECENTLY_USED_DIRECTORY_KEY = "mostRecentlyUsedDirectory";
    private static final String MEMORY_SIZE_KEY = "memorySize";
    private static final String PROCESSOR_SPEED_KEY = "processorSpeed";
    private static final String THEME_KEY = "theme";

    private static final String WINDOWS_NODE = "windows";
    private static final String WINDOW_X_KEY = "x";
    private static final String WINDOW_Y_KEY = "y";
    private static final String WINDOW_WIDTH_KEY = "width";
    private static final String WINDOW_HEIGHT_KEY = "height";
    private static final String WINDOW_MAXIMIZED_KEY = "maximized";

    private final Preferences preferences;

    private final ReadOnlyObjectWrapper<Path> mostRecentlyUsedDirectory = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<MemorySize> memorySize = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ProcessorSpeed> processorSpeed = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Theme> theme = new ReadOnlyObjectWrapper<>();

    /// Creates a new user preferences instance.
    public UserPreferences() {
        this.preferences = Preferences.userRoot().node(ROOT_NODE);
        this.preferences.addPreferenceChangeListener(this::preferenceChanged);

        updateMostRecentlyUsedDirectory();
        updateMemorySize();
        updateProcessorSpeed();
        updateTheme();
    }

    private void preferenceChanged(final PreferenceChangeEvent event) {
        switch (event.getKey()) {
            case MOST_RECENTLY_USED_DIRECTORY_KEY -> updateMostRecentlyUsedDirectory();
            case MEMORY_SIZE_KEY -> updateMemorySize();
            case PROCESSOR_SPEED_KEY -> updateProcessorSpeed();
            case THEME_KEY -> updateTheme();
            default -> {
                // Ignore other preferences
            }
        }
    }

    /// {@return a read-only property for the most recently used directory}
    public ReadOnlyObjectProperty<Path> mostRecentlyUsedDirectoryProperty() {
        return this.mostRecentlyUsedDirectory.getReadOnlyProperty();
    }

    private void updateMostRecentlyUsedDirectory() {
        this.mostRecentlyUsedDirectory.set(getDirectory(MOST_RECENTLY_USED_DIRECTORY_KEY));
    }

    /// {@return the most recently used directory, or the user's home directory if
    /// not set or invalid.}
    public Path getMostRecentlyUsedDirectory() {
        return mostRecentlyUsedDirectoryProperty().get();
    }

    /// Set the most recently used directory.
    /// 
    /// @param directory the directory to set, or null to remove the preference
    public void putMostRecentlyUsedDirectory(final Path directory) {
        putPath(MOST_RECENTLY_USED_DIRECTORY_KEY, directory);
    }

    /// {@return a read-only property for the memory size}
    public ReadOnlyObjectProperty<MemorySize> memorySizeProperty() {
        return this.memorySize.getReadOnlyProperty();
    }

    private void updateMemorySize() {
        this.memorySize.set(getEnumValue(MEMORY_SIZE_KEY, MemorySize.class, () -> MemorySize.SMALL));
    }

    /// {@return the memory size, or [MemorySize#SMALL] if not set or invalid}
    public MemorySize getMemorySize() {
        return memorySizeProperty().get();
    }

    /// Set the memory size.
    ///
    /// @param newMemorySize the memory size to set, or null to remove the preference
    public void putMemorySize(final MemorySize newMemorySize) {
        putEnum(MEMORY_SIZE_KEY, newMemorySize);
    }

    /// {@return a read-only property for the processor speed}
    public ReadOnlyObjectProperty<ProcessorSpeed> processorSpeedProperty() {
        return this.processorSpeed.getReadOnlyProperty();
    }

    private void updateProcessorSpeed() {
        this.processorSpeed.set(getEnumValue(PROCESSOR_SPEED_KEY, ProcessorSpeed.class, () -> ProcessorSpeed.MEDIUM));
    }

    /// {@return the processor speed, or [ProcessorSpeed#MEDIUM] if not set
    /// or invalid}
    public ProcessorSpeed getProcessorSpeed() {
        return processorSpeedProperty().get();
    }

    /// Set the processor speed.
    ///
    /// @param newProcessorSpeed the processor speed to set, or null to remove
    ///                          the preference
    public void putProcessorSpeed(final ProcessorSpeed newProcessorSpeed) {
        putEnum(PROCESSOR_SPEED_KEY, newProcessorSpeed);
    }

    /// {@return a read-only property for the theme}
    public ReadOnlyObjectProperty<Theme> themeProperty() {
        return this.theme.getReadOnlyProperty();
    }

    private void updateTheme() {
        this.theme.set(getEnumValue(THEME_KEY, Theme.class, () -> Theme.LIGHT));
    }

    /// {@return the theme, or [Theme#LIGHT] if not set or invalid.}
    public Theme getTheme() {
        return themeProperty().get();
    }

    /// Set the theme.
    ///
    /// @param newTheme the theme to set, or null to remove the preference
    public void putTheme(final Theme newTheme) {
        putEnum(THEME_KEY, newTheme);
    }

    /// Returns the saved window state for the given window identifier.
    ///
    /// @param windowId identifies the window, must not be `null`
    /// @return the saved window state, or empty if not previously saved
    public Optional<WindowState> getWindowState(final String windowId) {
        requireNonNull(windowId);

        final var node = this.preferences.node(WINDOWS_NODE).node(windowId);
        final var x = node.getDouble(WINDOW_X_KEY, Double.NaN);
        final var y = node.getDouble(WINDOW_Y_KEY, Double.NaN);
        final var width = node.getDouble(WINDOW_WIDTH_KEY, Double.NaN);
        final var height = node.getDouble(WINDOW_HEIGHT_KEY, Double.NaN);

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(width) || Double.isNaN(height))
            return Optional.empty();

        final var maximized = node.getBoolean(WINDOW_MAXIMIZED_KEY, false);
        return Optional.of(new WindowState(new Rectangle2D(x, y, width, height), maximized));
    }

    /// Save the window state for the given window identifier, or remove the sub
    /// node if `windowState` is `null`.
    ///
    /// @param windowId    identifies the window, must not be `null`
    /// @param windowState the window state to save, or `null` to remove
    public void putWindowState(final String windowId, final WindowState windowState) {
        requireNonNull(windowId);

        final var node = this.preferences.node(WINDOWS_NODE).node(windowId);
        if (windowState != null) {
            final var bounds = windowState.bounds();
            node.putDouble(WINDOW_X_KEY, bounds.getMinX());
            node.putDouble(WINDOW_Y_KEY, bounds.getMinY());
            node.putDouble(WINDOW_WIDTH_KEY, bounds.getWidth());
            node.putDouble(WINDOW_HEIGHT_KEY, bounds.getHeight());
            node.putBoolean(WINDOW_MAXIMIZED_KEY, windowState.maximized());
        } else {
            node.remove(WINDOW_X_KEY);
            node.remove(WINDOW_Y_KEY);
            node.remove(WINDOW_WIDTH_KEY);
            node.remove(WINDOW_HEIGHT_KEY);
            node.remove(WINDOW_MAXIMIZED_KEY);
        }
    }

    private Path getDirectory(final String key) {
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

    private <E extends Enum<E>> E getEnumValue(final String key,
            final Class<E> enumClass, final Supplier<E> defaultValueSupplier) {
        requireNonNull(key);
        requireNonNull(enumClass);
        requireNonNull(defaultValueSupplier);

        return Optional.ofNullable(this.preferences.get(key, "")) //
                .filter(Predicate.not(String::isBlank)) //
                .map(value -> {
                    try {
                        return Enum.valueOf(enumClass, value);
                    } catch (final IllegalArgumentException _) {
                        return null;
                    }
                }) //
                .orElseGet(defaultValueSupplier);
    }

    private void putEnum(final String key, final Enum<?> enumValue) {
        requireNonNull(key);

        Optional.ofNullable(enumValue) //
                .map(Enum::name) //
                .ifPresentOrElse(s -> this.preferences.put(key, s), () -> this.preferences.remove(key));
    }
}
