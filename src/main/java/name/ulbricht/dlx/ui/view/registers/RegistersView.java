package name.ulbricht.dlx.ui.view.registers;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.Subscription;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.control.BinaryTableCell;
import name.ulbricht.dlx.ui.scene.control.DecimalTableCell;
import name.ulbricht.dlx.ui.scene.control.HexadecimalTableCell;
import name.ulbricht.dlx.ui.view.View;

/// View for the internals of the processor.
public final class RegistersView implements View<Parent, RegistersViewModel> {

    /// Loads the registers view from the FXML file.
    /// 
    /// @param activeProcessor the observable value providing the currently active
    ///                        processor
    /// @return The configured registers view with the loaded content.
    public static RegistersView load(final ObservableValue<CPU> activeProcessor) {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = RegistersView.class.getResource("RegistersView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources, null, controllerClass -> {
            if (controllerClass == RegistersController.class)
                return new RegistersController(activeProcessor);
            return null;
        });

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
    private final Subscription titleSubscription;

    private RegistersView(final RegistersController controller) {
        this.controller = requireNonNull(controller);

        // Update the title when the processor changes to show the register count.
        this.titleSubscription = this.controller.getViewModel().processorProperty().subscribe(this::updateTitle);
    }

    private void updateTitle(final CPU processor) {
        if (processor != null) {
            this.title.set(Messages.getString("registers.title.pattern")
                    .formatted(Integer.valueOf(processor.getRegisters().size())));
        } else {
            this.title.set(Messages.getString("registers.title"));
        }
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public void dispose() {
        this.titleSubscription.unsubscribe();
        this.controller.dispose();
    }
}
