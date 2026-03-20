package name.ulbricht.dlx.ui.view.internals;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;

/// Represents a register item in the internals view, containing the register
/// index, value, and change status.
public final class RegisterItem {

    private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper value = new ReadOnlyIntegerWrapper();
    private final ReadOnlyBooleanWrapper changed = new ReadOnlyBooleanWrapper();

    /// Creates a new register item with the specified index.
    public RegisterItem(final int index) {
        this.index.set(index);
    }

    /// {@return a property representing the register index}
    public ReadOnlyIntegerProperty indexProperty() {
        return index.getReadOnlyProperty();
    }

    /// {@return the register index}
    public int getIndex() {
        return indexProperty().get();
    }

    /// {@return a property representing the register value}
    public ReadOnlyIntegerProperty valueProperty() {
        return value.getReadOnlyProperty();
    }

    /// {@return the register value}
    public int getValue() {
        return valueProperty().get();
    }

    /// Updates the register value.
    ///
    /// @param newValue the new value to set
    void updateValue(final int newValue) {
        value.set(newValue);
    }

    /// {@return a property representing whether the register value has changed}
    public ReadOnlyBooleanProperty changedProperty() {
        return changed.getReadOnlyProperty();
    }

    /// {@return whether the register value has changed}
    public boolean isChanged() {
        return changedProperty().get();
    }

    /// Updates the change status of the register value.
    void updateChanged(final boolean changed) {
        this.changed.set(changed);
    }
}
