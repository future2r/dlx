package name.ulbricht.dlx.ui.view.problems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.asm.parser.ParsedProgram;

/// View model for the problems view.
public final class ProblemsViewModel {

    private final ObjectProperty<ParsedProgram> parsedProgram = new SimpleObjectProperty<>();

    private final ObservableList<ProblemItem> modifiableProblems = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<ProblemItem> problems = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableProblems));

    /// Creates a new problems view model instance.
    public ProblemsViewModel() {
        this.parsedProgram.subscribe(this::programChanged);
    }

    /// {@return the parsed program property}
    public ObjectProperty<ParsedProgram> parsedProgramProperty() {
        return this.parsedProgram;
    }

    /// {@return the parsed program}
    public ParsedProgram getParsedProgram() {
        return parsedProgramProperty().get();
    }

    /// Sets the parsed program.
    /// 
    /// @param parsedProgram the parsed program to set
    public void setParsedProgram(final ParsedProgram parsedProgram) {
        this.parsedProgram.set(parsedProgram);
    }

    /// {@return a read-only list of problems}
    public ReadOnlyListProperty<ProblemItem> problemsProperty() {
        return this.problems.getReadOnlyProperty();
    }

    /// {@return the list of problems}
    public ObservableList<ProblemItem> getProblems() {
        return problemsProperty().get();
    }

    private void programChanged(final ParsedProgram newProgram) {
        this.modifiableProblems.clear();
        if (newProgram != null)
            this.modifiableProblems.addAll(newProgram.errors().stream()
                    .map(ProblemItem::new)
                    .toList());
    }
}
