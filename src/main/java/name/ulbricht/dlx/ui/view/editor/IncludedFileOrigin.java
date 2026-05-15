package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.linker.SourceUnit;
import name.ulbricht.dlx.ui.view.problems.SourceOrigin;

/// Virtual [SourceOrigin] for a file that was pulled into a master via a
/// `.include` directive but may not have an editor tab of its own.
///
/// Identifying the origin by the deterministic [SourceUnit#id] keeps the
/// problems-view grouping stable across re-links. The diagnostics list is a
/// filtered live view of the master's full diagnostics list, so problems
/// produced by lexing, linking, parsing, or compiling this file appear under
/// this origin automatically without explicit notification.
final class IncludedFileOrigin implements SourceOrigin {

    private final UUID id;
    private final Path filePath;
    private final ReadOnlyStringWrapper name;
    private final ReadOnlyListWrapper<Diagnostic> diagnostics;

    /// Creates a new virtual origin backed by an included [SourceUnit].
    ///
    /// @param unit             the unit this origin represents; its
    ///                         [SourceUnit#path] must not be null (only the
    ///                         master is allowed to have a null path and the
    ///                         master is published as its own origin)
    /// @param masterDiagnostics the master editor's live diagnostics list,
    ///                          filtered down to entries whose
    ///                          [Diagnostic#sourceId] matches this unit
    IncludedFileOrigin(final SourceUnit unit, final ObservableList<Diagnostic> masterDiagnostics) {
        requireNonNull(unit, "unit must not be null");
        requireNonNull(masterDiagnostics, "masterDiagnostics must not be null");
        this.id = unit.id();
        this.filePath = requireNonNull(unit.path(), "included unit must have a path");
        this.name = new ReadOnlyStringWrapper(this.filePath.getFileName().toString());
        final var filtered = new FilteredList<>(masterDiagnostics, d -> this.id.equals(d.sourceId()));
        this.diagnostics = new ReadOnlyListWrapper<>(FXCollections.unmodifiableObservableList(filtered));
    }

    @Override
    public UUID id() {
        return this.id;
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return this.name.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyListProperty<Diagnostic> diagnosticsProperty() {
        return this.diagnostics.getReadOnlyProperty();
    }

    @Override
    public Optional<Path> path() {
        return Optional.of(this.filePath);
    }
}
