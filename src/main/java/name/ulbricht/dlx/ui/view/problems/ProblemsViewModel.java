package name.ulbricht.dlx.ui.view.problems;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.asm.Diagnostic;

/// View model for the problems view.
public final class ProblemsViewModel {

    private final ListProperty<Diagnostic> diagnostics = new SimpleListProperty<>();

    private final ObservableList<ProblemItem> modifiableProblems = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<ProblemItem> problems = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableProblems));

    // Keep track of the current diagnostics list listener
    private final ListChangeListener<Diagnostic> diagnosticsListListener = change -> {
        while (change.next()) {
            if (change.wasPermutated() || change.wasUpdated()) {
                // For permutation/update, just refresh all
                updateProblemsFromDiagnostics(change.getList());
            } else {
                if (change.wasRemoved()) {
                    for (final var d : change.getRemoved()) {
                        removeProblemForDiagnostic(d);
                    }
                }
                if (change.wasAdded()) {
                    for (int i = change.getFrom(); i < change.getTo(); i++) {
                        final var d = change.getList().get(i);
                        addProblemForDiagnostic(d, i);
                    }
                }
            }
        }
    };

    /// Creates a new problems view model instance.
    public ProblemsViewModel() {
        // Subscribe for changes to the diagnostics property (list replacement)
        this.diagnostics.subscribe((oldList, newList) -> {
            if (oldList != null)
                oldList.removeListener(this.diagnosticsListListener);

            if (oldList != newList)
                updateProblemsFromDiagnostics(newList);

            if (newList != null)
                newList.addListener(this.diagnosticsListListener);
        });

        // If diagnostics is already set, initialize
        if (this.diagnostics.get() != null) {
            updateProblemsFromDiagnostics(this.diagnostics.get());
            this.diagnostics.get().addListener(this.diagnosticsListListener);
        }
    }

    private void updateProblemsFromDiagnostics(final ObservableList<? extends Diagnostic> list) {
        this.modifiableProblems.clear();
        if (list != null && !list.isEmpty())
            this.modifiableProblems.addAll(list.stream().map(ProblemItem::new).toList());
    }

    private void addProblemForDiagnostic(final Diagnostic diagnostic, final int index) {
        this.modifiableProblems.add(index, new ProblemItem(diagnostic));
    }

    private void removeProblemForDiagnostic(final Diagnostic diagnostic) {
        this.modifiableProblems.removeIf(item -> item.getDiagnostic().equals(diagnostic));
    }

    /// {@return the property for the diagnostics}
    public ListProperty<Diagnostic> diagnosticsProperty() {
        return this.diagnostics;
    }

    /// {@return the current diagnostics}
    public ObservableList<Diagnostic> getDiagnostics() {
        return diagnosticsProperty().get();
    }

    /// Sets the diagnostics to display.
    /// 
    /// @param diagnostics the diagnostics to set
    public void setDiagnostics(final ObservableList<Diagnostic> diagnostics) {
        this.diagnostics.set(diagnostics);
    }

    /// {@return a read-only list of problems}
    public ReadOnlyListProperty<ProblemItem> problemsProperty() {
        return this.problems.getReadOnlyProperty();
    }

    /// {@return the list of problems}
    public ObservableList<ProblemItem> getProblems() {
        return problemsProperty().get();
    }
}
