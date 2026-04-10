package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.util.Subscription;

/// Represents a source origin node in the problems tree view. Each node
/// corresponds to one editor/source origin and displays the editor name along
/// with the number of diagnostics.
public final class SourceOriginItem implements ProblemItem {

    private final SourceOrigin sourceOrigin;
    private final ReadOnlyIntegerWrapper diagnosticCount = new ReadOnlyIntegerWrapper();
    private final Subscription diagnosticCountSubscription;

    /// Creates a new source group item for the given source origin.
    ///
    /// @param sourceOrigin the source origin this group represents
    public SourceOriginItem(final SourceOrigin sourceOrigin) {
        this.sourceOrigin = requireNonNull(sourceOrigin);
        this.diagnosticCountSubscription = this.sourceOrigin.diagnosticsProperty().sizeProperty()
                .subscribe((_, newSize) -> this.diagnosticCount.set(newSize.intValue()));
    }

    @Override
    public SourceOrigin sourceOrigin() {
        return this.sourceOrigin;
    }

    /// {@return a read-only property representing the display name of this source
    /// group, delegating to the source origin's name property}
    public ReadOnlyStringProperty nameProperty() {
        return this.sourceOrigin.nameProperty();
    }

    /// {@return the display name of this source group}
    public String getName() {
        return nameProperty().get();
    }

    /// {@return a read-only property representing the number of diagnostics in this
    /// source group}
    public ReadOnlyIntegerProperty diagnosticCountProperty() {
        return this.diagnosticCount.getReadOnlyProperty();
    }

    /// {@return the number of diagnostics in this source group}
    public int getDiagnosticCount() {
        return diagnosticCountProperty().get();
    }

    /// Disposes of this source group item and releases any resources it holds.
    void dispose() {
        this.diagnosticCountSubscription.unsubscribe();
    }
}
