package name.ulbricht.dlx.ui.view.main;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import name.ulbricht.dlx.simulator.CPU;

/// View model for the main application view.
public final class MainViewModel {

    private final ReadOnlyObjectWrapper<CPU> processor = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canSave = new ReadOnlyBooleanWrapper();

    /// Creates a new main view model instance.
    public MainViewModel() {
        this.processor.set(new CPU());
        this.canSave.bind(this.dirty);
    }

    /// {@return a read-only property representing the current processor}
    public ReadOnlyObjectProperty<CPU> processorProperty() {
        return this.processor.getReadOnlyProperty();
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// {@return a read-only property indicating whether the current file has
    /// unsaved changes}
    public ReadOnlyBooleanProperty dirtyProperty() {
        return this.dirty.getReadOnlyProperty();
    }

    /// {@return whether the current file has unsaved changes}
    public boolean isDirty() {
        return dirtyProperty().get();
    }

    /// {@return a read-only property indicating whether the current file can
    /// be saved}
    public ReadOnlyBooleanProperty canSaveProperty() {
        return this.canSave.getReadOnlyProperty();
    }

    /// {@return whether the current file can be saved}
    public boolean isCanSave() {
        return canSaveProperty().get();
    }

    void reset() {
        this.processor.set(new CPU());
    }
}
