package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.util.TextPosition;

/// Represents a problem in the problem list.
public final class ProblemItem {

    private final ReadOnlyObjectWrapper<Diagnostic.Stage> source = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<TextPosition> textPosition = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper message = new ReadOnlyStringWrapper();

    /// Creates a new problem item from the given diagnostic.
    /// 
    /// @param diagnostic the diagnostic to create the problem item from
    public ProblemItem(final Diagnostic diagnostic) {
        requireNonNull(diagnostic);

        this.source.set(diagnostic.stage());
        this.textPosition.set(diagnostic.pos());
        this.message.set(diagnostic.message());
    }

    /// {@return a read-only property representing the source of the problem}
    public ReadOnlyObjectProperty<Diagnostic.Stage> sourceProperty() {
        return this.source.getReadOnlyProperty();
    }

    /// {@return the source of the problem}
    public Diagnostic.Stage getSource() {
        return sourceProperty().get();
    }

    /// {@return a read-only property representing the text position of the problem}
    public ReadOnlyObjectProperty<TextPosition> textPositionProperty() {
        return this.textPosition.getReadOnlyProperty();
    }

    /// {@return the text position of the problem}
    public TextPosition getTextPosition() {
        return textPositionProperty().get();
    }

    /// {@return a read-only property representing the message of the problem}
    public ReadOnlyStringProperty messageProperty() {
        return this.message.getReadOnlyProperty();
    }

    /// {@return the message of the problem}
    public String getMessage() {
        return messageProperty().get();
    }
}
