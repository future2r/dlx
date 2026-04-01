package name.ulbricht.dlx.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/// Service that stores recent log records and notifies listeners about changes.
public final class LogStore {

    /// Maximum number of records retained in this store.
    private static final int MAX_ENTRIES = 100;

    private final Logger rootLogger = Logger.getLogger("");

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
    public LogStore() {
        this.handler.setLevel(Level.ALL);
        this.rootLogger.addHandler(this.handler);
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
        synchronized (this) {
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
        synchronized (this) {
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