package name.ulbricht.dlx.ui.view.log;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/// UI model for a single log entry displayed in the Log view.
public final class LogEntry {

    private final LogRecord logRecord;
    private final ReadOnlyObjectWrapper<LocalDateTime> time = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<Level> level = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper source = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper();

    LogEntry(final LogRecord logRecord) {
        this.logRecord = requireNonNull(logRecord);

        this.time.set(LocalDateTime.ofInstant(logRecord.getInstant(), ZoneId.systemDefault()));
        this.level.set(logRecord.getLevel());
        this.source.set(logRecord.getSourceClassName());
        this.message.set(logRecord.getMessage());
    }

    /// {@return the underlying log record}
    public LogRecord getLogRecord() {
        return this.logRecord;
    }

    /// {@return the property for the log entry time}
    public ReadOnlyObjectProperty<LocalDateTime> timeProperty() {
        return this.time.getReadOnlyProperty();
    }

    /// {@return the log entry time}
    public LocalDateTime getTime() {
        return this.time.get();
    }

    /// {@return the property for the log entry level}
    public ReadOnlyObjectProperty<Level> levelProperty() {
        return this.level.getReadOnlyProperty();
    }

    /// {@return the log entry level}
    public Level getLevel() {
        return this.level.get();
    }

    /// {@return the property for the log entry source}
    public ReadOnlyStringProperty sourceProperty() {
        return this.source.getReadOnlyProperty();
    }

    /// {@return the log entry source}
    public String getSource() {
        return this.source.get();
    }

    /// {@return the property for the log entry message}
    public ReadOnlyStringProperty messageProperty() {
        return this.message.getReadOnlyProperty();
    }

    /// {@return the log entry message}
    public String getMessage() {
        return this.message.get();
    }
}
