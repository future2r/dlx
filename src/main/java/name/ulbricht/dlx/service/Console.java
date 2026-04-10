package name.ulbricht.dlx.service;

import java.util.ArrayList;
import java.util.List;

/// Thread-safe console output buffer that notifies listeners about changes.
///
/// Used by [TrapHandler] to receive program output from trap instructions and by
/// the Console view to display it. The listener callbacks are invoked on the
/// calling thread (typically the simulation thread).
public final class Console {

    private final StringBuilder buffer = new StringBuilder();

    private final List<ConsoleListener> listeners = new ArrayList<>();

    /// Creates a new console service instance.
    public Console() {
    }

    /// Appends text to the console output and notifies listeners.
    ///
    /// @param text the text to append; must not be `null`
    public void append(final String text) {
        synchronized (this.buffer) {
            this.buffer.append(text);
        }
        notifyListeners(new Change(text, false));
    }

    /// Clears all console output and notifies listeners.
    public void clear() {
        synchronized (this.buffer) {
            if (this.buffer.isEmpty())
                return;
            this.buffer.setLength(0);
        }
        notifyListeners(new Change("", true));
    }

    /// {@return the full console text}
    public String snapshot() {
        synchronized (this.buffer) {
            return this.buffer.toString();
        }
    }

    /// Registers a listener for console changes.
    ///
    /// @param listener the listener to add
    public synchronized void addListener(final ConsoleListener listener) {
        this.listeners.add(listener);
    }

    /// Unregisters a previously registered listener.
    ///
    /// @param listener the listener to remove
    public synchronized void removeListener(final ConsoleListener listener) {
        this.listeners.remove(listener);
    }

    private void notifyListeners(final Change change) {
        final List<ConsoleListener> currentListeners;
        synchronized (this) {
            if (this.listeners.isEmpty())
                return;
            currentListeners = List.copyOf(this.listeners);
        }
        currentListeners.forEach(listener -> listener.consoleChanged(change));
    }

    /// Listener for incremental changes to the console output.
    @FunctionalInterface
    public interface ConsoleListener {

        /// Called when console output was appended or cleared.
        ///
        /// @param change the change payload
        void consoleChanged(Change change);
    }

    /// Change payload describing a console modification.
    ///
    /// @param appended the appended text, or empty if cleared
    /// @param cleared  `true` if the console was cleared
    public record Change(String appended, boolean cleared) {
    }
}
