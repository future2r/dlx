package name.ulbricht.dlx.ui.scene.control;

import javafx.scene.control.TableCell;
import name.ulbricht.dlx.ui.util.FormatUtil;

/// Table cells that displays a hexadecimal value.
/// 
/// @param <S> the type of the row item
public class HexadecimalTableCell<S> extends TableCell<S, Integer> {

    /// Creates a new hexadecimal table cell.
    public HexadecimalTableCell() {
        getStyleClass().add("hexadecimal-table-cell");
    }

    @Override
    protected void updateItem(final Integer item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            this.setText(null);
        } else {
            this.setText(FormatUtil.hexadecimal(item.intValue()));
        }
    }
}
