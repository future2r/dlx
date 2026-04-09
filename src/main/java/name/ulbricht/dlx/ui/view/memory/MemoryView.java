package name.ulbricht.dlx.ui.view.memory;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import javafx.util.Subscription;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.util.FormatUtil;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for displaying the memory state of the DLX simulator as a hex viewer.
public final class MemoryView implements View<Parent, MemoryViewModel> {

    /// Loads the memory view from the FXML file.
    ///
    /// @param activeProcessor the observable value providing the currently active
    ///                        processor
    /// @return The configured memory view with the loaded content.
    public static MemoryView load(final ObservableValue<CPU> activeProcessor) {
        return new MemoryView(Views.loadController(MemoryView.class,
                controllerClass -> controllerClass == MemoryController.class
                        ? new MemoryController(activeProcessor)
                        : null));
    }

    /// {@return a cell factory for the hex address column}
    public static Callback<TableColumn<MemoryRow, Number>, TableCell<MemoryRow, Number>> addressCellFactory() {
        return _ -> new HexAddressTableCell();
    }

    /// {@return a cell value factory for the address column}
    public static Callback<CellDataFeatures<MemoryRow, Number>, ObservableValue<Number>> addressValueFactory() {
        return features -> {
            final var row = features.getValue();
            return new ReadOnlyObjectWrapper<>(Integer.valueOf(row != null ? row.baseAddress() : 0));
        };
    }

    /// Creates a cell value factory that extracts the byte at the given column.
    ///
    /// @param col the column index (0–15)
    /// @return a cell value factory for the byte column
    public static Callback<CellDataFeatures<MemoryRow, Number>, ObservableValue<Number>> byteValueFactory(
            final int col) {
        return features -> {
            final var row = features.getValue();
            return new ReadOnlyObjectWrapper<>(Integer.valueOf(row != null ? row.getByteValue(col) : 0));
        };
    }

    /// Creates a cell factory for a memory byte column at the given index.
    ///
    /// @param col the column index (0–15)
    /// @return a cell factory for the byte column
    public static Callback<TableColumn<MemoryRow, Number>, TableCell<MemoryRow, Number>> byteCellFactory(
            final int col) {
        return _ -> new MemoryByteTableCell(col);
    }

    /// {@return a cell factory for the ASCII column}
    public static Callback<TableColumn<MemoryRow, String>, TableCell<MemoryRow, String>> asciiCellFactory() {
        return _ -> new AsciiTableCell();
    }

    /// {@return a cell value factory for the ASCII column}
    public static Callback<CellDataFeatures<MemoryRow, String>, ObservableValue<String>> asciiValueFactory() {
        return cdf -> {
            final var row = cdf.getValue();
            return new javafx.beans.property.SimpleStringProperty(row != null ? row.asciiText() : "");
        };
    }

    private final MemoryController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("memory.title"));
    private final Subscription titleSubscription;

    private MemoryView(final MemoryController controller) {
        this.controller = requireNonNull(controller);

        // Update the title when the processor changes to show the memory size.
        this.titleSubscription = this.controller.getViewModel().processorProperty().subscribe(this::updateTitle);
    }

    private void updateTitle(final CPU processor) {
        if (processor != null) {
            this.title.set(Messages.getString("memory.title.pattern")
                    .formatted(FormatUtil.formatBytes(processor.getMemory().size())));
        } else {
            this.title.set(Messages.getString("memory.title"));
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
