package name.ulbricht.dlx.ui.view.main;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/// View model for the main application view.
public final class MainViewModel {

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canSave = new ReadOnlyBooleanWrapper();

    /// Creates a new main view model instance.
    public MainViewModel() {
        this.canSave.bind(this.dirty);
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
}
