package name.ulbricht.dlx.ui.view.main;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import name.ulbricht.dlx.simulator.CPU;

/// View model for the main application view.
public final class MainViewModel {

    private final ReadOnlyObjectWrapper<CPU> processor = new ReadOnlyObjectWrapper<>();

    private final BooleanProperty canSave = new SimpleBooleanProperty();

    /// Creates a new main view model instance.
    public MainViewModel() {
        this.processor.set(new CPU());
    }

    /// {@return a read-only property representing the current processor}
    public ReadOnlyObjectProperty<CPU> processorProperty() {
        return this.processor.getReadOnlyProperty();
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// {@return the property indicating whether the current file can be saved}
    public BooleanProperty canSaveProperty() {
        return this.canSave;
    }

    /// {@return whether the current file can be saved}
    public boolean isCanSave() {
        return canSaveProperty().get();
    }

    /// Sets whether the current file can be saved.
    ///
    /// @param canSave whether the current file can be saved
    public void setCanSave(final boolean canSave) {
        this.canSave.set(canSave);
    }

    void reset() {
        this.processor.set(new CPU());
    }

}
