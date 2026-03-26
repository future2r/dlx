package name.ulbricht.dlx.ui.view.memory;

import javafx.scene.control.TableCell;

/// A table cell that renders an integer value as an 8-digit uppercase
/// hexadecimal address (e.g. `00000010`).
final class HexAddressTableCell extends TableCell<MemoryRow, Number> {

    /// Creates a new hex address table cell.
    HexAddressTableCell() {
        getStyleClass().add("hex-address-cell");
    }

    @Override
    protected void updateItem(final Number item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
        } else {
            setText("%08X".formatted(Integer.valueOf(item.intValue())));
        }
    }
}
