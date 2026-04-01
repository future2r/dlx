package name.ulbricht.dlx.ui.view.log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javafx.scene.control.TableCell;

/// A table cell that displays the time of a log entry as a
/// human-readable string.
public final class TimeTableCell extends TableCell<LogEntry, LocalDateTime> {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);

    /// Creates a new instance.
    public TimeTableCell() {
    }

    @Override
    protected void updateItem(final LocalDateTime item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.format(this.timeFormatter));
        }
    }
}