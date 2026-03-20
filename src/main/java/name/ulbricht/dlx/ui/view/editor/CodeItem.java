package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import name.ulbricht.dlx.compiler.InstructionCall;

/// Represents an instruction call.
public final class CodeItem {

    private final ReadOnlyObjectWrapper<InstructionCall> instructionCall = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyIntegerWrapper address = new ReadOnlyIntegerWrapper();
    private final ReadOnlyStringWrapper label = new ReadOnlyStringWrapper();

    /// Creates a new code item for the given instruction call.
    ///
    /// @param address         the address of the instruction call
    /// @param instructionCall the instruction call represented by this item
    CodeItem(final int address, final InstructionCall instructionCall) {
        requireNonNull(instructionCall);

        this.address.set(address);
        this.instructionCall.set(instructionCall);
        this.label.set(instructionCall.label());
    }

    /// {@return a read-only property representing the address of the
    /// instruction call}
    public ReadOnlyIntegerProperty addressProperty() {
        return this.address.getReadOnlyProperty();
    }

    /// {@return the address of the instruction call represented by this item}
    public int getAddress() {
        return addressProperty().get();
    }

    /// {@return a read-only property representing the instruction call}
    public ReadOnlyObjectProperty<InstructionCall> instructionCallProperty() {
        return instructionCall.getReadOnlyProperty();
    }

    /// {@return the instruction call represented by this item}
    public InstructionCall getInstructionCall() {
        return instructionCallProperty().get();
    }

    /// {@return a read-only property representing the label}
    public ReadOnlyStringProperty labelProperty() {
        return label.getReadOnlyProperty();
    }

    /// {@return the label represented by this item}
    public String getLabel() {
        return labelProperty().get();
    }
}
