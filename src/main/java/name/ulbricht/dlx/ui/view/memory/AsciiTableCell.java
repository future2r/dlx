package name.ulbricht.dlx.ui.view.memory;

import javafx.scene.control.TableCell;

/// A table cell that renders the 16-character ASCII representation of a
/// memory row.
final class AsciiTableCell extends TableCell<MemoryRow, String> {

    /// Creates a new ASCII table cell.
    AsciiTableCell() {
        getStyleClass().add("ascii-cell");
    }

    @Override
    protected void updateItem(final String item, final boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item);
    }
}
