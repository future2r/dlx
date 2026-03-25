package name.ulbricht.dlx.ui.view.registers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.control.BinaryTableCell;
import name.ulbricht.dlx.ui.scene.control.DecimalTableCell;
import name.ulbricht.dlx.ui.scene.control.HexadecimalTableCell;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for the internals of the processor.
public final class RegistersView implements ViewPart<RegistersViewModel> {

    /// Loads the registers view from the FXML file.
    /// 
    /// @return The configured registers view with the loaded content.
    public static RegistersView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = RegistersView.class.getResource("RegistersView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load RegistersView FXML", ex);
        }

        final var controller = fxmlLoader.<RegistersController>getController();

        // Create and return the view
        return new RegistersView(controller);
    }

    /// {@return a row factory for register table rows}
    public static Callback<TableView<RegisterItem>, TableRow<RegisterItem>> registerTableRowFactory() {
        return _ -> new RegisterTableRow();
    }

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

    private final RegistersController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("registers.title"));

    private RegistersView(final RegistersController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringWrapper titleProperty() {
        return this.title;
    }

    @Override
    public Node getRoot() {
        return this.controller.getRoot();
    }

    /// {@return the view model of the registers view}
    @Override
    public RegistersViewModel getViewModel() {
        return this.controller.getViewModel();
    }
}
