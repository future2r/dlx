package name.ulbricht.dlx.config;

import static java.util.Objects.requireNonNull;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import name.ulbricht.dlx.ui.scene.Theme;

/// User preferences.
public final class UserPreferences {

    /// Property name for the processor speed preference.
    public static final String PROCESSOR_SPEED_PROPERTY = "processorSpeed";

    /// Property name for the memory size preference.
    public static final String MEMORY_SIZE_PROPERTY = "memorySize";

    /// Property name for the theme preference.
    public static final String THEME_PROPERTY = "theme";

    /// Property name for the recent directory preference.
    public static final String RECENT_DIRECTORY_PROPERTY = "recentDirectory";

    /// Property name for the recent files preference.
    public static final String RECENT_FILES_PROPERTY = "recentFiles";

    /// Property name for the log level preference.
    public static final String LOG_LEVEL_PROPERTY = "logLevel";

    /// Default value for the processor speed preference.
    public static final ProcessorSpeed DEFAULT_PROCESSOR_SPEED = ProcessorSpeed.MEDIUM;

    /// Default value for the memory size preference.
    public static final MemorySize DEFAULT_MEMORY_SIZE = MemorySize.SMALL;

    /// Default value for the theme preference.
    public static final Theme DEFAULT_THEME = Theme.AUTO;

    /// Default value for the log level preference.
    public static final System.Logger.Level DEFAULT_LOG_LEVEL = System.Logger.Level.INFO;

    private static final String ROOT_NODE = "name/ulbricht/dlx";
    private static final String RECENT_DIRECTORY_KEY = RECENT_DIRECTORY_PROPERTY;
    private static final String MEMORY_SIZE_KEY = MEMORY_SIZE_PROPERTY;
    private static final String PROCESSOR_SPEED_KEY = PROCESSOR_SPEED_PROPERTY;
    private static final String THEME_KEY = THEME_PROPERTY;
    private static final String LOG_LEVEL_KEY = LOG_LEVEL_PROPERTY;

    private static final String RECENT_FILES_NODE = RECENT_FILES_PROPERTY;
    private static final String RECENT_FILES_COUNT_KEY = "count";
    private static final int MAX_RECENT_FILES = 5;

    private static final String STAGES_NODE = "stages";
    private static final String STAGE_MAXIMIZED_KEY = "maximized";
    private static final String STAGE_X_KEY = "x";
    private static final String STAGE_Y_KEY = "y";
    private static final String STAGE_WIDTH_KEY = "width";
    private static final String STAGE_HEIGHT_KEY = "height";

    private final Preferences preferences;

    private final Map<String, List<Consumer<?>>> listeners = new HashMap<>();

    private final List<Path> recentFiles = new ArrayList<>();

    /// Creates a new user preferences instance.
    public UserPreferences() {
        this.preferences = Preferences.userRoot().node(ROOT_NODE);

        this.recentFiles.addAll(loadRecentFiles());
    }

    /// Adds a preference change listener.
    ///
    /// @param <T>      the type of the preference value
    /// @param name     the name of the preference to listen for, must not be `null`
    /// @param listener the listener to add, must not be `null`
    public synchronized <T> void addPreferenceChangeListener(final String name, final Consumer<T> listener) {
        requireNonNull(name);
        requireNonNull(listener);

        this.listeners.computeIfAbsent(name, _ -> new ArrayList<>()).add(listener);
    }

    /// Removes a preference change listener.
    /// 
    /// @param <T>      the type of the preference value
    /// @param name     the name of the preference, must not be `null`
    /// @param listener the listener to remove, must not be `null`
    public synchronized <T> void removePreferenceChangeListener(final String name, final Consumer<T> listener) {
        requireNonNull(name);
        requireNonNull(listener);

        Optional.ofNullable(this.listeners.get(name)).ifPresent(list -> {
            list.remove(listener);
            if (list.isEmpty())
                this.listeners.remove(name);
        });
    }

    private <T> void notifyPreferenceChangeListeners(final String name, final T newValue) {
        final List<Consumer<?>> currentListeners;
        synchronized (this.listeners) {
            if (this.listeners.isEmpty())
                return;
            currentListeners = this.listeners.get(name);
            if (currentListeners == null || currentListeners.isEmpty())
                return;
        }

        currentListeners.forEach(listener -> {
            @SuppressWarnings("unchecked")
            final var typedListener = (Consumer<T>) listener;
            typedListener.accept(newValue);
        });
    }

    /// {@return the most recently used directory, or the user's home directory if
    /// not set or invalid.}
    public Path getRecentDirectory() {
        return getDirectory(RECENT_DIRECTORY_KEY);
    }

    /// Set the most recently used directory.
    ///
    /// @param directory the directory to set, or null to remove the preference
    public void putMostRecentlyUsedDirectory(final Path directory) {
        putPath(RECENT_DIRECTORY_KEY, directory);
        notifyPreferenceChangeListeners(RECENT_DIRECTORY_PROPERTY, getRecentDirectory());
    }

    /// {@return the memory size, or [MemorySize#SMALL] if not set or invalid}
    public MemorySize getMemorySize() {
        return getEnumValue(MEMORY_SIZE_KEY, MemorySize.class, () -> DEFAULT_MEMORY_SIZE);
    }

    /// Set the memory size.
    ///
    /// @param newMemorySize the memory size to set, or null to remove the preference
    public void putMemorySize(final MemorySize newMemorySize) {
        putEnum(MEMORY_SIZE_KEY, newMemorySize);
        notifyPreferenceChangeListeners(MEMORY_SIZE_PROPERTY, getMemorySize());
    }

    /// {@return the processor speed, or [ProcessorSpeed#MEDIUM] if not set
    /// or invalid}
    public ProcessorSpeed getProcessorSpeed() {
        return getEnumValue(PROCESSOR_SPEED_KEY, ProcessorSpeed.class, () -> DEFAULT_PROCESSOR_SPEED);
    }

    /// Set the processor speed.
    ///
    /// @param newProcessorSpeed the processor speed to set, or null to remove
    ///                          the preference
    public void putProcessorSpeed(final ProcessorSpeed newProcessorSpeed) {
        putEnum(PROCESSOR_SPEED_KEY, newProcessorSpeed);
        notifyPreferenceChangeListeners(PROCESSOR_SPEED_PROPERTY, getProcessorSpeed());
    }

    /// {@return the theme, or [Theme#LIGHT] if not set or invalid.}
    public Theme getTheme() {
        return getEnumValue(THEME_KEY, Theme.class, () -> DEFAULT_THEME);
    }

    /// Set the theme.
    ///
    /// @param newTheme the theme to set, or null to remove the preference
    public void putTheme(final Theme newTheme) {
        putEnum(THEME_KEY, newTheme);
        notifyPreferenceChangeListeners(THEME_PROPERTY, getTheme());
    }

    /// {@return the log level, or [System.Logger.Level#INFO] if not set
    /// or invalid.}
    public System.Logger.Level getLogLevel() {
        return getEnumValue(LOG_LEVEL_KEY, System.Logger.Level.class, () -> DEFAULT_LOG_LEVEL);
    }

    /// Set the log level.
    ///
    /// @param newLogLevel the log level to set, or null to remove the preference
    public void putLogLevel(final System.Logger.Level newLogLevel) {
        putEnum(LOG_LEVEL_KEY, newLogLevel);
        notifyPreferenceChangeListeners(LOG_LEVEL_PROPERTY, getLogLevel());
    }

    /// {@return an unmodifiable view of the recently opened files}
    public List<Path> getRecentFiles() {
        return Collections.unmodifiableList(this.recentFiles);
    }

    /// Adds a file to the recent files list. If the file is already in the list, it
    /// is moved to the beginning. The list is trimmed to at most 5 entries.
    ///
    /// @param file the file to add, must not be `null`
    public void addRecentFile(final Path file) {
        requireNonNull(file);
        this.recentFiles.remove(file);
        this.recentFiles.addFirst(file);
        while (this.recentFiles.size() > MAX_RECENT_FILES)
            this.recentFiles.removeLast();
        persistRecentFiles();
        notifyPreferenceChangeListeners(RECENT_FILES_PROPERTY, getRecentFiles());
    }

    /// Removes a file from the recent files list.
    ///
    /// @param file the file to remove, must not be `null`
    public void removeRecentFile(final Path file) {
        requireNonNull(file);
        if (this.recentFiles.remove(file)) {
            persistRecentFiles();
            notifyPreferenceChangeListeners(RECENT_FILES_PROPERTY, getRecentFiles());
        }
    }

    /// Removes all files from the recent files list.
    public void clearRecentFiles() {
        if (!this.recentFiles.isEmpty()) {
            this.recentFiles.clear();
            persistRecentFiles();
            notifyPreferenceChangeListeners(RECENT_FILES_PROPERTY, getRecentFiles());
        }
    }

    /// Returns the saved stage state for the given stage identifier.
    ///
    /// @param stageId identifies the stage, must not be `null`
    /// @return the saved stage state, or an empty optional if not previously saved
    public Optional<StageState> getStageState(final String stageId) {
        requireNonNull(stageId);

        final var node = this.preferences.node(STAGES_NODE).node(stageId);
        final var maximized = node.getBoolean(STAGE_MAXIMIZED_KEY, false);

        if (maximized)
            return Optional.of(StageState.ofMaximized());

        final var x = node.getInt(STAGE_X_KEY, StageState.UNDEFINED);
        final var y = node.getInt(STAGE_Y_KEY, StageState.UNDEFINED);
        final var width = node.getInt(STAGE_WIDTH_KEY, StageState.UNDEFINED);
        final var height = node.getInt(STAGE_HEIGHT_KEY, StageState.UNDEFINED);

        return Optional.of(StageState.ofNormal(x, y, width, height));
    }

    /// Save the stage state for the given stage identifier, or remove the sub node
    /// if `stageState` is `null`.
    ///
    /// @param stageId    identifies the stage, must not be `null`
    /// @param stageState the stage state to save, or `null` to remove
    public void putStageState(final String stageId, final StageState stageState) {
        requireNonNull(stageId);

        final var node = this.preferences.node(STAGES_NODE).node(stageId);
        if (stageState != null) {
            if (stageState.maximized()) {
                node.putBoolean(STAGE_MAXIMIZED_KEY, true);
                node.remove(STAGE_X_KEY);
                node.remove(STAGE_Y_KEY);
                node.remove(STAGE_WIDTH_KEY);
                node.remove(STAGE_HEIGHT_KEY);
            } else {
                node.putBoolean(STAGE_MAXIMIZED_KEY, false);
                node.putInt(STAGE_X_KEY, stageState.x());
                node.putInt(STAGE_Y_KEY, stageState.y());
                node.putInt(STAGE_WIDTH_KEY, stageState.width());
                node.putInt(STAGE_HEIGHT_KEY, stageState.height());
            }
        } else {
            node.remove(STAGE_MAXIMIZED_KEY);
            node.remove(STAGE_X_KEY);
            node.remove(STAGE_Y_KEY);
            node.remove(STAGE_WIDTH_KEY);
            node.remove(STAGE_HEIGHT_KEY);
        }
    }

    private List<Path> loadRecentFiles() {
        final var node = this.preferences.node(RECENT_FILES_NODE);
        final var count = node.getInt(RECENT_FILES_COUNT_KEY, 0);
        final var files = new ArrayList<Path>();
        for (var i = 0; i < count; i++) {
            final var value = node.get(String.valueOf(i), null);
            if (value != null) {
                try {
                    files.add(Path.of(value));
                } catch (final InvalidPathException _) {
                    // skip invalid entries
                }
            }
        }
        return files;
    }

    private void persistRecentFiles() {
        final var node = this.preferences.node(RECENT_FILES_NODE);
        final var size = this.recentFiles.size();
        node.putInt(RECENT_FILES_COUNT_KEY, size);
        for (var i = 0; i < size; i++)
            node.put(String.valueOf(i), this.recentFiles.get(i).toString());
        for (var i = size; i < MAX_RECENT_FILES; i++)
            node.remove(String.valueOf(i));
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
