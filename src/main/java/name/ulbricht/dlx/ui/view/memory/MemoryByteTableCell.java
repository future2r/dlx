package name.ulbricht.dlx.ui.view.memory;

import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import name.ulbricht.dlx.simulator.Access;

/// A table cell that renders a single memory byte as a two-digit hexadecimal
/// value and applies CSS pseudo-classes to indicate the last access type.
final class MemoryByteTableCell extends TableCell<MemoryRow, Number> {

    private static final PseudoClass READ_PSEUDO_CLASS = PseudoClass.getPseudoClass("read");
    private static final PseudoClass WRITE_PSEUDO_CLASS = PseudoClass.getPseudoClass("write");

    /// The column index (0–15) this cell represents within a row.
    private final int col;

    /// Creates a new memory byte table cell for the given column index.
    ///
    /// @param col the column index (0–15) within a [MemoryRow]
    MemoryByteTableCell(final int col) {
        this.col = col;
        getStyleClass().add("memory-byte-cell");
    }

    @Override
    protected void updateItem(final Number item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            pseudoClassStateChanged(READ_PSEUDO_CLASS, false);
            pseudoClassStateChanged(WRITE_PSEUDO_CLASS, false);
            return;
        }

        // Render the byte value as a two-digit uppercase hex string.
        setText("%02X".formatted(Integer.valueOf(item.intValue())));

        // Apply access highlighting. The MemoryRow carries the access state
        // for each byte; we read it for our column index.
        final var row = getTableRow() != null ? getTableRow().getItem() : null;
        final var access = row != null ? row.getByteAccess(this.col) : null;
        pseudoClassStateChanged(READ_PSEUDO_CLASS, access == Access.READ);
        pseudoClassStateChanged(WRITE_PSEUDO_CLASS, access == Access.WRITE);
    }
}
