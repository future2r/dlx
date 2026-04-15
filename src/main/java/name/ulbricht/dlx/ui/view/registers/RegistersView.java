package name.ulbricht.dlx.ui.view.registers;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
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
import name.ulbricht.dlx.ui.view.Views;

/// View for the internals of the processor.
public final class RegistersView implements View<Parent, RegistersViewModel> {

    /// Loads the registers view from the FXML file.
    ///
    /// @param activeProcessor the observable value providing the currently active
    ///                        processor
    /// @return The configured registers view with the loaded content.
    public static RegistersView load(final ObservableValue<CPU> activeProcessor) {
        return new RegistersView(Views.loadController(RegistersView.class,
                controllerClass -> controllerClass == RegistersController.class
                        ? new RegistersController(activeProcessor)
                        : null));
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
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final Subscription titleSubscription;

    private RegistersView(final RegistersController controller) {
        this.controller = requireNonNull(controller);
        this.title.set(createTitle(null));

        // Update the title when the processor changes to show the register count.
        this.titleSubscription = this.controller.getViewModel().processorProperty().subscribe(this::updateTitle);
    }

    private void updateTitle(final CPU processor) {
        this.title.set(createTitle(processor));
    }

    private static String createTitle(final CPU processor) {
        return processor != null
                ? Messages.getString("registers.title.pattern")
                        .formatted(Integer.valueOf(processor.getRegisters().size()))
                : Messages.getString("registers.title");
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
