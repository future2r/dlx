package name.ulbricht.dlx.ui.view.log;

import java.util.logging.Level;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;

/// A table row representing a log entry, with visual feedback according
/// to level.
public final class LogTableRow extends TableRow<LogEntry> {

    private static final PseudoClass LOG_ERROR = PseudoClass.getPseudoClass("log-error");
    private static final PseudoClass LOG_WARNING = PseudoClass.getPseudoClass("log-warning");
    private static final PseudoClass LOG_INFO = PseudoClass.getPseudoClass("log-info");
    private static final PseudoClass LOG_DEBUG = PseudoClass.getPseudoClass("log-debug");
    private static final PseudoClass LOG_TRACE = PseudoClass.getPseudoClass("log-trace");

    /// Creates a new log table row instance.
    LogTableRow() {
    }

    @Override
    protected void updateItem(final LogEntry item, final boolean empty) {
        super.updateItem(item, empty);

        final var severity = empty || item == null ? null : item.getLevel();
        pseudoClassStateChanged(LOG_ERROR, severity == Level.SEVERE);
        pseudoClassStateChanged(LOG_WARNING, severity == Level.WARNING);
        pseudoClassStateChanged(LOG_INFO, severity == Level.INFO);
        pseudoClassStateChanged(LOG_DEBUG, severity == Level.FINE);
        pseudoClassStateChanged(LOG_TRACE, severity == Level.FINER);
    }
}
