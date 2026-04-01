package name.ulbricht.dlx.ui.view.log;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.concurrent.Executor;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.service.LogStore;

/// View model for the Log view.
public final class LogViewModel {

    private final Executor uiExecutor;
    private final LogStore logStore;

    private final ObservableList<LogEntry> modifiableEntries = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<LogEntry> entries = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableEntries));

    private final BooleanProperty autoScroll = new SimpleBooleanProperty(true);

    private final LogStore.LogListener storeListener = this::storeChanged;

    /// Creates a new log view model instance.
    /// 
    /// @param uiExecutor the executor to use for UI updates, must not be `null`
    /// @param logStore   the log store to observe for log entries
    public LogViewModel(
            @NamedArg("uiExecutor") final Executor uiExecutor,
            @NamedArg("logStore") final LogStore logStore) {
        this.uiExecutor = requireNonNull(uiExecutor);
        this.logStore = requireNonNull(logStore);

        this.logStore.addListener(this.storeListener);
        applySnapshot(this.logStore);
    }

    /// {@return the read-only log entries property}
    public ReadOnlyListProperty<LogEntry> entriesProperty() {
        return this.entries.getReadOnlyProperty();
    }

    /// {@return the current log entries}
    public ObservableList<LogEntry> getEntries() {
        return entriesProperty().get();
    }

    /// Clears all current log entries.
    public void clear() {
        this.logStore.clear();
    }

    /// {@return auto-scroll state property}
    public BooleanProperty autoScrollProperty() {
        return this.autoScroll;
    }

    /// {@return whether auto-scroll is enabled}
    public boolean isAutoScroll() {
        return this.autoScroll.get();
    }

    /// Sets whether auto-scroll is enabled.
    ///
    /// @param autoScroll true to follow latest entries
    public void setAutoScroll(final boolean autoScroll) {
        this.autoScroll.set(autoScroll);
    }

    private void applySnapshot(final LogStore store) {
        final var snapshot = store.snapshot().stream().map(LogEntry::new).toList();
        this.uiExecutor.execute(() -> this.modifiableEntries.setAll(snapshot));
    }

    private void storeChanged(final LogStore.Change change) {
        this.uiExecutor.execute(() -> {
            if (!change.removed().isEmpty()) {
                // Remove entries whose LogRecords are in the removed set
                final var removedRecords = new HashSet<>(change.removed());
                this.modifiableEntries.removeIf(entry -> removedRecords.contains(entry.getLogRecord()));
            }

            if (!change.added().isEmpty())
                this.modifiableEntries.addAll(change.added().stream().map(LogEntry::new).toList());
        });
    }
}
