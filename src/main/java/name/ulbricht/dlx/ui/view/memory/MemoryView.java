package name.ulbricht.dlx.ui.view.memory;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for displaying the memory state of the DLX simulator as a hex viewer.
public final class MemoryView implements ViewPart<MemoryViewModel> {

    /// Loads the memory view from the FXML file.
    ///
    /// @return The configured memory view with the loaded content.
    public static MemoryView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = MemoryView.class.getResource("MemoryView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load MemoryView FXML", ex);
        }

        final var controller = fxmlLoader.<MemoryController>getController();

        // Create and return the view
        return new MemoryView(controller);
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

    private MemoryView(final MemoryController controller) {
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

    @Override
    public MemoryViewModel getViewModel() {
        return this.controller.getViewModel();
    }
}
