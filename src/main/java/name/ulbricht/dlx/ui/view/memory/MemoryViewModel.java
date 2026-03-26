package name.ulbricht.dlx.ui.view.memory;

import java.util.Arrays;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.simulator.Access;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.MemoryAccessListener;

/// View model for the memory hex viewer.
///
/// This class exposes observable data only — it has no knowledge of any UI
/// controls. The Controller (View layer) listens to property changes and
/// translates them into `TableView` operations such as `refresh()` and
/// `scrollTo()`.
public final class MemoryViewModel implements MemoryAccessListener {

    private final ObjectProperty<CPU> processor = new SimpleObjectProperty<>();

    private final ObservableList<MemoryRow> modifiableRows = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<MemoryRow> rows = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableRows));

    /// Boolean flip-flop. Every toggle signals the View that visible cells
    /// should be repainted. The Controller listens to this property and calls
    /// `tableView.refresh()`.
    private final BooleanProperty refreshFlag = new SimpleBooleanProperty();

    /// Shadow copy of the memory contents, updated only on the FX thread.
    private byte[] shadow = new byte[0];

    /// Per-byte access state, updated only on the FX thread.
    private Access[] accessState = new Access[0];

    /// Creates a new memory view model instance.
    public MemoryViewModel() {
        this.processor.subscribe(this::processorChanged);
    }

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    /// {@return a property representing the current processor}
    public ObjectProperty<CPU> processorProperty() {
        return this.processor;
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// Sets the current processor.
    ///
    /// @param processor the processor to set
    public void setProcessor(final CPU processor) {
        processorProperty().set(processor);
    }

    /// {@return a property representing the list of memory rows}
    public ReadOnlyListProperty<MemoryRow> rowsProperty() {
        return this.rows.getReadOnlyProperty();
    }

    /// {@return the list of memory rows}
    public ObservableList<MemoryRow> getRows() {
        return rowsProperty().get();
    }

    /// A boolean that toggles every time visible cells need to be repainted.
    /// The Controller should listen to this property and call
    /// `tableView.refresh()` whenever it changes.
    ///
    /// {@return a read-only property that toggles on each data change}
    public ReadOnlyBooleanProperty refreshFlagProperty() {
        return this.refreshFlag;
    }

    /// Toggles the refresh flag, signalling the View that visible cells need
    /// repainting.
    private void signalRefresh() {
        this.refreshFlag.set(!this.refreshFlag.get());
    }

    private void processorChanged(final CPU oldProcessor, final CPU newProcessor) {
        if (oldProcessor != null) {
            oldProcessor.getMemory().removeAccessListener(this);
        }

        this.modifiableRows.clear();

        if (newProcessor != null) {
            final var memSize = newProcessor.getMemory().size();
            this.shadow = new byte[memSize];
            this.accessState = new Access[memSize];

            // Create one MemoryRow per 16-byte line.
            final var rowCount = (memSize + MemoryRow.BYTES_PER_ROW - 1) / MemoryRow.BYTES_PER_ROW;
            for (var i = 0; i < rowCount; i++) {
                this.modifiableRows.add(new MemoryRow(i * MemoryRow.BYTES_PER_ROW, this.shadow, this.accessState));
            }

            newProcessor.getMemory().addAccessListener(this);
        } else {
            this.shadow = new byte[0];
            this.accessState = new Access[0];
        }
    }

    @Override
    public void memoryAccessed(final MemoryAccess access) {
        // Events may originate from the CPU's virtual thread.
        Platform.runLater(() -> handleMemoryAccess(access));
    }

    private void handleMemoryAccess(final MemoryAccess access) {
        final var addr = access.address();
        final var bytes = access.value();
        final var type = access.type();

        // Update the shadow byte array and access state.
        for (var i = 0; i < bytes.length; i++) {
            final var byteAddr = addr + i;
            if (byteAddr >= this.shadow.length)
                break;
            if (type == Access.WRITE) {
                this.shadow[byteAddr] = bytes[i];
            }
            this.accessState[byteAddr] = type;
        }

        // Signal the View that visible cells need repainting.
        signalRefresh();
    }

    /// Clears all per-byte access highlights without changing the shadow data.
    /// Called before each simulation step to reset the previous cycle's highlights.
    void clearAccessHighlights() {
        Arrays.fill(this.accessState, null);
        signalRefresh();
    }
}
