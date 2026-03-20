package name.ulbricht.dlx.ui.view.editor;

import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.compiler.DataDeclaration;
import name.ulbricht.dlx.compiler.InstructionCall;
import name.ulbricht.dlx.compiler.Program;

public final class EditorViewModel {

    private final StringProperty source = new SimpleStringProperty();

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();

    private final ObjectProperty<Program> program = new SimpleObjectProperty<>();

    private final ObservableList<DataItem<?>> modifiableDataItems = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<DataItem<?>> dataItems = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(modifiableDataItems));

    private final ObservableList<CodeItem> modifiableCodeItems = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<CodeItem> codeItems = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(modifiableCodeItems));

    /// Creates a new editor view model instance.
    public EditorViewModel() {
        this.program.subscribe(this::programChanged);
    }

    /// {@return a property representing the source code}
    public StringProperty sourceProperty() {
        return this.source;
    }

    /// {@return the source code}
    public String getSource() {
        return sourceProperty().get();
    }

    /// Sets the source code.
    /// 
    /// @param source the source code to set
    public void setSource(final String source) {
        this.source.set(source);
    }

    /// {@return a read-only property indicating whether the current file has
    /// unsaved changes}
    public ReadOnlyBooleanProperty dirtyProperty() {
        return this.dirty.getReadOnlyProperty();
    }

    /// {@return whether the current file has unsaved changes}
    public boolean isDirty() {
        return dirtyProperty().get();
    }

    /// {@return a property representing the current program}
    public ObjectProperty<Program> programProperty() {
        return this.program;
    }

    /// {@return the current program}
    public Program getProgram() {
        return programProperty().get();
    }

    /// Sets the current program.
    /// 
    /// @param program the program to set
    public void setProgram(final Program program) {
        this.program.set(program);
    }

    /// {@return a read-only property representing the list of data items}
    public ReadOnlyListProperty<DataItem<?>> dataItemsProperty() {
        return this.dataItems.getReadOnlyProperty();
    }

    /// {@return the list of data items}
    public ObservableList<DataItem<?>> getDataItems() {
        return dataItemsProperty().get();
    }

    /// {@return a read-only property representing the list of code items}
    public ReadOnlyListProperty<CodeItem> codeItemsProperty() {
        return this.codeItems.getReadOnlyProperty();
    }

    /// {@return the list of code items}
    public ObservableList<CodeItem> getCodeItems() {
        return codeItemsProperty().get();
    }

    private void programChanged(final Program newProgram) {
        this.modifiableDataItems.clear();
        this.modifiableCodeItems.clear();

        if (newProgram != null) {

            // Container for both, data items and code items
            record Sections(List<DataItem<?>> data, List<CodeItem> code) {
            }

            // Process the addressed program elements and separate them into data items and
            // code items
            final var sections = newProgram.addressed().stream().collect(Collectors.teeing(
                    Collectors.filtering(addressed -> addressed.element() instanceof DataDeclaration,
                            Collectors.mapping(
                                    addressed -> new DataItem<>(addressed.address(),
                                            (DataDeclaration) addressed.element()),
                                    Collectors.toList())),
                    Collectors.filtering(addressed -> addressed.element() instanceof InstructionCall,
                            Collectors.mapping(
                                    addressed -> new CodeItem(addressed.address(),
                                            (InstructionCall) addressed.element()),
                                    Collectors.toList())),
                    Sections::new));

            this.modifiableDataItems.addAll(sections.data());
            this.modifiableCodeItems.addAll(sections.code());
        }
    }
}
