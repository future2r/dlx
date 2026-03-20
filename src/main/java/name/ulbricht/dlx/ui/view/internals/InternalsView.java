package name.ulbricht.dlx.ui.view.internals;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.control.BinaryTableCell;
import name.ulbricht.dlx.ui.control.DecimalTableCell;
import name.ulbricht.dlx.ui.control.HexadecimalTableCell;

/// View for the internals of the processor. Since the view is created via
/// `<fx:include>` in the main view, it does not need a view class. However, we
/// still need a class to provide some view utilities.
public final class InternalsView {

    /// {@return a cell factory for decimal table cells}
    public static Callback<TableColumn<?, Integer>, TableCell<?, Integer>> decimalCellFactory() {
        return _ -> new DecimalTableCell<>();
    }

    /// {@return a cell factory for hexadecimal table cells}
    public static Callback<TableColumn<?, Integer>, TableCell<?, Integer>> hexadecimalCellFactory() {
        return _ -> new HexadecimalTableCell<>();
    }

    /// {@return a cell factory for binary table cells}
    public static Callback<TableColumn<?, Integer>, TableCell<?, Integer>> binaryCellFactory() {
        return _ -> new BinaryTableCell<>();
    }

    /// Private constructor to prevent instantiation.
    private InternalsView() {
    }
}
