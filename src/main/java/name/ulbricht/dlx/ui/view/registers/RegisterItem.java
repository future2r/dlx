package name.ulbricht.dlx.ui.view.registers;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import name.ulbricht.dlx.simulator.Access;

/// Represents a register item in the internals view, containing the register
/// index, value, and change status.
public final class RegisterItem {

    private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper value = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<Access> access = new ReadOnlyObjectWrapper<>();

    /// Creates a new register item with the specified index.
    /// 
    /// @param index the index of the register
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

    /// {@return a property representing the last access type (read/write) of
    /// the register}
    public ReadOnlyObjectProperty<Access> accessProperty() {
        return this.access.getReadOnlyProperty();
    }

    /// {@return the last access type (read/write) of the register, or `null` if no
    /// access has occurred}
    public Access getAccess() {
        return accessProperty().get();
    }

    /// Updates the access status of the register value.
    void updateAccess(final Access newAccess) {
        this.access.set(newAccess);
    }
}
