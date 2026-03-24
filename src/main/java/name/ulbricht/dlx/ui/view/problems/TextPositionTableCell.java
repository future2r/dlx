package name.ulbricht.dlx.ui.view.problems;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.util.TextPosition;

/// A table cell that displays the text position of the problem as a
/// human-readable string.
public class TextPositionTableCell extends TableCell<ProblemItem, TextPosition> {

    /// Creates a new instance.
    public TextPositionTableCell() {
    }

    @Override
    protected void updateItem(final TextPosition item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(Messages.getString("problems.textPosition.pattern").formatted(Integer.valueOf(item.displayLine()),
                    Integer.valueOf(item.displayColumn())));
        }
    }
}