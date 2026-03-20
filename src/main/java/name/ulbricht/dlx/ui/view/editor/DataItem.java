package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import name.ulbricht.dlx.compiler.DataDeclaration;

/// Represents a data declaration.
/// 
/// @param <T> the type of the data declaration represented by this item
public final class DataItem<T extends DataDeclaration> {

    private final ReadOnlyObjectWrapper<T> declaration = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper address = new ReadOnlyIntegerWrapper();
    private final ReadOnlyStringWrapper label = new ReadOnlyStringWrapper();
    private final ReadOnlyIntegerWrapper size = new ReadOnlyIntegerWrapper();

    /// Creates a new data item for the given declaration.
    /// 
    /// @param address     the address of the data declaration
    /// @param declaration the data declaration represented by this item
    DataItem(final int address, final T declaration) {
        requireNonNull(declaration);

        this.address.set(address);
        this.declaration.set(declaration);
        this.label.set(declaration.label());
        this.size.set(declaration.size());
    }

    /// {@return a read-only property representing the address of the data
    public ReadOnlyIntegerProperty addressProperty() {
        return this.address.getReadOnlyProperty();
    }

    /// {@return the address of the data declaration represented by this item}
    public int getAddress() {
        return addressProperty().get();
    }

    /// {@return a read-only property representing the data declaration}
    public ReadOnlyObjectProperty<T> declarationProperty() {
        return declaration.getReadOnlyProperty();
    }

    /// {@return the data declaration represented by this item}
    public T getDeclaration() {
        return declarationProperty().get();
    }

    /// {@return a read-only property representing the label}
    public ReadOnlyStringProperty labelProperty() {
        return label.getReadOnlyProperty();
    }

    /// {@return the label represented by this item}
    public String getLabel() {
        return labelProperty().get();
    }

    /// {@return a read-only property representing the size}
    public ReadOnlyIntegerProperty sizeProperty() {
        return size.getReadOnlyProperty();
    }

    /// {@return the size represented by this item}
    public int getSize() {
        return sizeProperty().get();
    }
}
