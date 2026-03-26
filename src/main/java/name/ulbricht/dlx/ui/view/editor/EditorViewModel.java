package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.asm.compiler.Compiler;
import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.lexer.TokenizedProgram;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.asm.parser.Parser;
import name.ulbricht.dlx.io.SourceFile;

/// View model for the editor view.
public final class EditorViewModel {

    private final UUID id = UUID.randomUUID();

    private final ReadOnlyObjectWrapper<Path> file = new ReadOnlyObjectWrapper<>();

    private final StringProperty source = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();

    private final ObservableList<Diagnostic> modifiableDiagnostics = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<Diagnostic> diagnostics = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableDiagnostics));

    private final ReadOnlyObjectWrapper<TokenizedProgram> tokenizedProgram = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ParsedProgram> parsedProgram = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<CompiledProgram> compiledProgram = new ReadOnlyObjectWrapper<>();

    /// Creates a new editor view model instance.
    public EditorViewModel() {
        this.tokenizedProgram.bind(this.source.map(this::tokenize));
        this.parsedProgram.bind(this.tokenizedProgram.map(this::parse));
    }

    /// {@return a read-only property representing the currently loaded file, or
    /// `null` if no file is loaded}
    public ReadOnlyObjectProperty<Path> fileProperty() {
        return this.file.getReadOnlyProperty();
    }

    /// {@return the currently loaded file, or `null` if no file is loaded}
    public Path getFile() {
        return fileProperty().get();
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
    /// @param source the new source code
    void setSource(final String source) {
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

    /// {@return a read-only property representing the list of diagnostics produced
    /// during lexing, parsing, and compilation}
    public ReadOnlyListProperty<Diagnostic> diagnosticsProperty() {
        return this.diagnostics.getReadOnlyProperty();
    }

    /// {@return the list of diagnostics produced during lexing, parsing,
    /// and compilation}
    public ObservableList<Diagnostic> getDiagnostics() {
        return diagnosticsProperty().get();
    }

    /// {@return a read-only property representing the tokenized program, or `null`
    /// if the source code has not been tokenized}
    public ReadOnlyObjectProperty<TokenizedProgram> tokenizedProgramProperty() {
        return this.tokenizedProgram.getReadOnlyProperty();
    }

    /// {@return the tokenized program, or `null` if the source code has not
    /// been tokenized}
    public TokenizedProgram getTokenizedProgram() {
        return tokenizedProgramProperty().get();
    }

    /// {@return a read-only property representing the parsed program, or `null` if
    /// there is none}
    public ReadOnlyObjectProperty<ParsedProgram> parsedProgramProperty() {
        return this.parsedProgram.getReadOnlyProperty();
    }

    /// {@return the parsed program, or `null` if there is none}
    public ParsedProgram getParsedProgram() {
        return parsedProgramProperty().get();
    }

    /// {@return a read-only property representing the compiled program, or `null`
    /// if there is none}
    public ReadOnlyObjectProperty<CompiledProgram> compiledProgramProperty() {
        return this.compiledProgram.getReadOnlyProperty();
    }

    /// {@return the compiled program, or `null` if there is none}
    public CompiledProgram getCompiledProgram() {
        return compiledProgramProperty().get();
    }

    private TokenizedProgram tokenize(final String src) {
        // Clear the diagnostics before starting a new tokenization
        this.modifiableDiagnostics.clear();

        if (src != null) {
            final var lines = List.of(src.split("\\R", -1));
            final var lexer = new Lexer(LexerMode.ASSEMBLER);
            final var tokenized = lexer.tokenize(this.id, lines);

            this.modifiableDiagnostics.addAll(tokenized.diagnostics());

            return tokenized;
        }
        return null;
    }

    private ParsedProgram parse(final TokenizedProgram tokenized) {
        if (tokenized != null) {
            final var parser = new Parser();
            final var parsed = parser.parse(tokenized);

            this.modifiableDiagnostics.addAll(parsed.diagnostics());

            return parsed;
        }
        return null;
    }

    /// Creates a new file with example source code.
    void newFile() throws IOException {
        final var fileName = "example.dlx";
        final String example;
        try (var in = getClass().getResourceAsStream(fileName)) {
            if (in == null)
                throw new FileNotFoundException(fileName);
            example = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        this.setSource(example);
        this.dirty.set(true);
        this.file.set(null);
    }

    /// Loads the content of the specified file into the editor.
    ///
    /// @param fileToLoad the file to load
    void loadFile(final Path fileToLoad) throws IOException {
        requireNonNull(fileToLoad);

        this.setSource(SourceFile.read(fileToLoad));
        this.dirty.set(false);
        this.file.set(fileToLoad);
    }

    /// Saves the current source code to the specified file. After saving, the dirty
    /// flag is cleared and the file path is updated.
    ///
    /// @param fileToSave the file to save to
    /// @throws IOException if an I/O error occurs
    public void saveFile(final Path fileToSave) throws IOException {
        requireNonNull(fileToSave);

        SourceFile.write(fileToSave, getSource());
        this.dirty.set(false);
        this.file.set(fileToSave);
    }

    /// Compiles the current parsed program. If compilation produces diagnostics,
    /// they are added to the view model's diagnostics list. If the diagnostics
    /// contain errors, the compiled program is set to `null`. If compilation is
    /// successful, the compiled program is stored in the view model.
    /// 
    /// @return `true` if compilation succeeded without errors, `false` otherwise
    public boolean compile() {
        // Remove all compiler problems
        this.modifiableDiagnostics.removeIf(d -> d.stage() == Diagnostic.Stage.COMPILING);

        final var parsed = getParsedProgram();
        if (parsed == null)
            return false;

        final var compiler = new Compiler();
        final var compiled = compiler.compile(parsed);

        this.modifiableDiagnostics.addAll(compiled.diagnostics());

        if (compiled.hasErrors()) {
            this.compiledProgram.set(null);
            return false;
        }

        this.compiledProgram.set(compiled);
        return true;
    }

    /// Marks the editor content as modified.
    void markDirty() {
        this.dirty.set(true);
    }
}
