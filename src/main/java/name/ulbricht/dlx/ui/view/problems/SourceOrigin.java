package name.ulbricht.dlx.ui.view.problems;

import java.nio.file.Path;
import java.util.Optional;
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

    /// {@return the on-disk path of this source origin, if known. Returned for
    /// editor view models that have a saved file as well as for virtual origins
    /// that represent an included file; empty for unsaved editors.}
    default Optional<Path> path() {
        return Optional.empty();
    }
}
