package name.ulbricht.dlx.ui.view.memory;

import static java.util.Objects.requireNonNull;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.ui.i18n.Messages;

/// Controller for the memory hex viewer.
public final class MemoryController {

    private final ObservableValue<CPU> activeProcessor;

    @FXML
    private Parent memoryRoot;

    @FXML
    private MemoryViewModel viewModel;

    @FXML
    private TableView<MemoryRow> memoryTable;

    /// Creates a new memory controller instance.
    /// 
    /// @param activeProcessor the observable value providing the currently
    ///                        active processor
    public MemoryController(final ObservableValue<CPU> activeProcessor) {
        this.activeProcessor = requireNonNull(activeProcessor);
    }

    @FXML
    private void initialize() {
        // Enable cell-level selection instead of full-row selection.
        this.memoryTable.getSelectionModel().setCellSelectionEnabled(true);

        // Repaint visible cells whenever the ViewModel signals a data change.
        this.viewModel.refreshFlagProperty().subscribe(() -> this.memoryTable.refresh());

        // Build the columns programmatically.
        buildColumns();

        this.viewModel.processorProperty().bind(this.activeProcessor);
    }

    private void buildColumns() {
        final var columns = this.memoryTable.getColumns();

        // Address column
        final var addressCol = new TableColumn<MemoryRow, Number>(
                Messages.getString("memory.column.address"));
        addressCol.setCellValueFactory(MemoryView.addressValueFactory());
        addressCol.setCellFactory(MemoryView.addressCellFactory());
        addressCol.setPrefWidth(80);
        addressCol.setSortable(false);
        addressCol.setReorderable(false);
        columns.add(addressCol);

        // 16 byte columns (00–0F)
        for (var col = 0; col < MemoryRow.BYTES_PER_ROW; col++) {
            final var byteCol = new TableColumn<MemoryRow, Number>(
                    "%02X".formatted(Integer.valueOf(col)));
            byteCol.setCellValueFactory(MemoryView.byteValueFactory(col));
            byteCol.setCellFactory(MemoryView.byteCellFactory(col));
            byteCol.setPrefWidth(28);
            byteCol.setMinWidth(28);
            byteCol.setSortable(false);
            byteCol.setReorderable(false);
            columns.add(byteCol);
        }

        // ASCII column
        final var asciiCol = new TableColumn<MemoryRow, String>(
                Messages.getString("memory.column.ascii"));
        asciiCol.setCellValueFactory(MemoryView.asciiValueFactory());
        asciiCol.setCellFactory(MemoryView.asciiCellFactory());
        asciiCol.setPrefWidth(130);
        asciiCol.setSortable(false);
        asciiCol.setReorderable(false);
        columns.add(asciiCol);
    }

    /// {@return the root node of the memory view}
    Parent getRoot() {
        return this.memoryRoot;
    }

    /// {@return the view model associated with this controller}
    MemoryViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.viewModel.processorProperty().unbind();
    }
}
