package name.ulbricht.dlx.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import name.ulbricht.dlx.config.UserPreferences;

/// Logging service that also stores recent log records and notifies listeners
/// about changes. This implementation uses the 'java.util.logging' framework.
/// The application itselfs however uses the 'System.Logger' API. This way, the
/// application stys independent of the underlying logging framework.
public final class Logging {

    /// Maximum number of records retained in this store.
    private static final int MAX_ENTRIES = 100;

    private static final Logger rootLogger = Logger.getLogger("name.ulbricht.dlx");

    /// Initializes the logging system.
    public static void initLogging() {
        // Load logging configuration from the bundled properties file
        try (var is = Logging.class.getResourceAsStream("/name/ulbricht/dlx/logging.properties")) {
            if (is != null)
                LogManager.getLogManager().readConfiguration(is);
        } catch (final Exception _) {
            // Fall back to JVM defaults
        }

        // Apply the log level from the user preferences
        applyLogLevel(Services.userPreferences().getLogLevel());

        // Register for future changes of the log level
        Services.userPreferences().addPreferenceChangeListener(UserPreferences.LOG_LEVEL_PROPERTY,
                Logging::applyLogLevel);
    }

    /// Applies the current log level preference to the 'java.util.logging' root
    /// logger.
    /// 
    /// @param level The log level to apply.
    private static void applyLogLevel(final System.Logger.Level level) {
        final var utilLevel = convertLevel(level);
        rootLogger.setLevel(utilLevel);
        for (final var handler : rootLogger.getHandlers())
            handler.setLevel(utilLevel);
    }

    /// Converts a [System.Logger.Level] to a [java.util.logging.Level].
    /// 
    /// @param level The [System.Logger.Level] to convert.
    /// @return The corresponding [java.util.logging.Level].
    private static Level convertLevel(final System.Logger.Level level) {
        return switch (level) {
            case OFF -> Level.OFF;
            case ERROR -> Level.SEVERE;
            case WARNING -> Level.WARNING;
            case INFO -> Level.INFO;
            case DEBUG -> Level.FINE;
            case TRACE -> Level.FINER;
            case ALL -> Level.ALL;
        };
    }

    private final List<LogRecord> records = new ArrayList<>();

    private final List<LogListener> listeners = new ArrayList<>();

    private final Handler handler = new Handler() {

        @Override
        public void publish(final LogRecord record) {
            if (record != null && isLoggable(record))
                append(record);
        }

        @Override
        public void flush() {
            // No buffered output.
        }

        @Override
        public void close() {
            // Lifecycle is managed by the service instance.
        }
    };

    /// Creates a new log store and installs a root logging handler that forwards
    /// records into this store.
    public Logging() {
        this.handler.setLevel(Level.ALL);
        rootLogger.addHandler(this.handler);
    }

    /// Returns an immutable snapshot of the currently retained records.
    ///
    /// @return snapshot of current records
    public synchronized List<LogRecord> snapshot() {
        return List.copyOf(this.records);
    }

    /// Clears all retained records.
    public void clear() {
        final List<LogRecord> removed;
        synchronized (this.records) {
            if (this.records.isEmpty())
                return;

            removed = List.copyOf(this.records);
            this.records.clear();
        }

        notifyListeners(new Change(removed, List.of()));
    }

    /// Registers a listener for record list changes.
    ///
    /// @param listener listener to add
    public synchronized void addListener(final LogListener listener) {
        this.listeners.add(listener);
    }

    /// Unregisters a previously registered listener.
    ///
    /// @param listener listener to remove
    public synchronized void removeListener(final LogListener listener) {
        this.listeners.remove(listener);
    }

    private void append(final LogRecord record) {
        final List<LogRecord> removed;
        synchronized (this.records) {
            this.records.add(record);
            if (this.records.size() > MAX_ENTRIES) {
                removed = List.of(this.records.removeFirst());
            } else {
                removed = List.of();
            }
        }

        notifyListeners(new Change(removed, List.of(record)));
    }

    private void notifyListeners(final Change change) {
        final List<LogListener> currentListeners;
        synchronized (this) {
            if (this.listeners.isEmpty())
                return;
            currentListeners = List.copyOf(this.listeners);
        }

        currentListeners.forEach(listener -> listener.logsChanged(change));
    }

    /// Listener for incremental changes of the log store.
    @FunctionalInterface
    public interface LogListener {

        /// Called when log records were added and/or removed.
        ///
        /// @param change change payload
        void logsChanged(Change change);
    }

    /// Change payload describing removed and added log records.
    ///
    /// @param removed records removed from the store
    /// @param added   records added to the store
    public record Change(List<LogRecord> removed, List<LogRecord> added) {
    }
}