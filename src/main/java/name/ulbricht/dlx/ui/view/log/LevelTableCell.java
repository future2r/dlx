package name.ulbricht.dlx.ui.view.log;

import java.util.logging.Level;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.ui.i18n.Messages;

/// A table cell that displays the level of a log entry as a
/// human-readable string.
public final class LevelTableCell extends TableCell<LogEntry, Level> {

    /// Creates a new instance.
    public LevelTableCell() {
    }

    @Override
    protected void updateItem(final Level item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(Messages.getString("log.level." + item.getName().toLowerCase()));
        }
    }
}