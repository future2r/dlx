package name.ulbricht.dlx.ui.view.problems;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.ui.i18n.Messages;

/// A table cell that displays the source of the problem as a
/// human-readable string.
public final class SourceTableCell extends TableCell<ProblemItem, Diagnostic.Stage> {

    /// Creates a new instance.
    public SourceTableCell() {
    }

    @Override
    protected void updateItem(final Diagnostic.Stage item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(Messages.getString("problems.source." + item.name().toLowerCase()));
        }
    }
}