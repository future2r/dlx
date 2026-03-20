package name.ulbricht.dlx.ui.control;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.ui.util.FormatUtil;

/// Table cells that displays a decimal value.
public final class DecimalTableCell<S> extends TableCell<S, Integer> {

    /// Creates a new decimal table cell.
    public DecimalTableCell() {
        getStyleClass().add("decimal-table-cell");
    }

    @Override
    protected void updateItem(final Integer item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            this.setText(null);
        } else {
            this.setText(FormatUtil.decimal(item.intValue()));
        }
    }
}
