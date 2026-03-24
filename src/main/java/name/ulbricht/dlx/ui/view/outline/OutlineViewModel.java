package name.ulbricht.dlx.ui.view.outline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import name.ulbricht.dlx.asm.parser.ParsedProgram;

/// View model for the outline view.
public final class OutlineViewModel {

    private final ObjectProperty<ParsedProgram> parsedProgram = new SimpleObjectProperty<>();

    /// Creates a new outline view model instance.
    public OutlineViewModel() {
    }

    /// {@return a property representing the parsed program, or `null` if there
    /// is none}
    public ObjectProperty<ParsedProgram> parsedProgramProperty() {
        return this.parsedProgram;
    }

    /// {@return the parsed program, or `null` if there is none}
    public ParsedProgram getParsedProgram() {
        return parsedProgramProperty().get();
    }

    /// Sets the parsed program.
    /// 
    /// @param parsed the parsed program, or `null` if there is none
    public void setParsedProgram(final ParsedProgram parsed) {
        parsedProgramProperty().set(parsed);
    }
}
