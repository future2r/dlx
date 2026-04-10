package name.ulbricht.dlx.ui.view.problems;

import java.util.UUID;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import name.ulbricht.dlx.asm.Diagnostic;

/// Represents the origin of source code that can produce diagnostics. This
/// abstraction decouples the problems view from specific editor implementations.
public interface SourceOrigin {

    /// {@return the unique identifier of this source origin}
    UUID id();

    /// {@return a read-only property representing the display name of this
    /// source origin}
    ReadOnlyStringProperty nameProperty();

    /// {@return the display name of this source origin}
    default String getName() {
        return nameProperty().get();
    }

    /// {@return a read-only property representing the diagnostics produced by this
    /// source origin}
    ReadOnlyListProperty<Diagnostic> diagnosticsProperty();
}
