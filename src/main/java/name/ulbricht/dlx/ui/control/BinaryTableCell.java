package name.ulbricht.dlx.ui.control;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.ui.util.FormatUtil;

/// Table cells that displays a binary value.
public class BinaryTableCell<S> extends TableCell<S, Integer> {

    /// Creates a new binary table cell.
    public BinaryTableCell() {
        getStyleClass().add("binary-table-cell");
    }

    @Override
    protected void updateItem(final Integer item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            this.setText(null);
        } else {
            this.setText(FormatUtil.binary(item.intValue()));
        }
    }
}
