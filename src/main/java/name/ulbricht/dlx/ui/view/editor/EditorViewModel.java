package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.asm.compiler.Compiler;
import name.ulbricht.dlx.asm.linker.BufferProvider;
import name.ulbricht.dlx.asm.linker.LinkedProgram;
import name.ulbricht.dlx.asm.linker.Linker;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.asm.parser.Parser;
import name.ulbricht.dlx.io.SourceFile;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.problems.SourceOrigin;

/// View model for the editor view.
///
/// Owns the source-text pipeline for one tab: source -> [Linker] ->
/// [ParsedProgram] -> [CompiledProgram] (on demand). Linker output is exposed
/// via [#linkedProgramProperty] and triggers automatic re-parsing whenever the
/// source text or the file path changes; the workspace's [BufferProvider]
/// additionally triggers re-links when an open included file is edited.
///
/// For programs that pull in other files via `.include`, the linker produces
/// one [name.ulbricht.dlx.asm.linker.SourceUnit] per included file. Each
/// non-master unit is published as a virtual [SourceOrigin] via
/// [#includedOriginsProperty] so the problems view can group its errors and so
/// the main controller can open the file on demand.
public final class EditorViewModel implements SourceOrigin {

    private static final AtomicInteger UNTITLED_COUNTER = new AtomicInteger();

    private final UUID programId = UUID.randomUUID();
    private final int untitledNumber = UNTITLED_COUNTER.incrementAndGet();

    private final ReadOnlyObjectWrapper<Path> file = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();

    private final StringProperty source = new SimpleStringProperty();
    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();

    private final ObservableList<Diagnostic> modifiableDiagnostics = FXCollections.observableArrayList();
    // Source-attributed view used by SourceOrigin.diagnosticsProperty: only
    // entries whose sourceId matches this editor's own programId. Each
    // IncludedFileOrigin exposes a similar filtered view for its included
    // unit, so the problems view groups every error under exactly one source.
    private final ReadOnlyListWrapper<Diagnostic> diagnostics = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(
                    new FilteredList<>(this.modifiableDiagnostics, d -> this.programId.equals(d.sourceId()))));

    private final ReadOnlyObjectWrapper<LinkedProgram> linkedProgram = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<ParsedProgram> parsedProgram = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<CompiledProgram> compiledProgram = new ReadOnlyObjectWrapper<>();

    private final ObservableList<SourceOrigin> modifiableIncludedOrigins = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<SourceOrigin> includedOrigins = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(this.modifiableIncludedOrigins));

    private BufferProvider bufferProvider = BufferProvider.NONE;

    /// Creates a new editor view model instance. The buffer provider defaults
    /// to [BufferProvider#NONE]; callers that want cross-tab live-buffer
    /// resolution should call [#setBufferProvider] before setting the source.
    public EditorViewModel() {
        this.name.bind(this.file.map(f -> f.getFileName().toString())
                .orElse(Messages.getString("editor.title.untitled") + "-" + this.untitledNumber));
        this.source.subscribe(this::relink);
        this.file.subscribe(this::relink);
    }

    @Override
    public UUID id() {
        return this.programId;
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return this.name.getReadOnlyProperty();
    }

    /// {@return the display name of this editor (file name or "Untitled")}
    @Override
    public String getName() {
        return nameProperty().get();
    }

    @Override
    public Optional<Path> path() {
        return Optional.ofNullable(getFile());
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

    /// Replaces the buffer provider used during include resolution. Should be
    /// called by the main controller right after construction, before the
    /// first source text is loaded.
    ///
    /// @param bufferProvider the new provider; must not be null
    public void setBufferProvider(final BufferProvider bufferProvider) {
        this.bufferProvider = requireNonNull(bufferProvider, "bufferProvider must not be null");
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

    /// {@return a read-only property representing the diagnostics produced during
    /// linking, parsing, and compilation that originate from this editor's
    /// own source. Diagnostics from included files are exposed separately via
    /// each [IncludedFileOrigin] in [#includedOriginsProperty].}
    @Override
    public ReadOnlyListProperty<Diagnostic> diagnosticsProperty() {
        return this.diagnostics.getReadOnlyProperty();
    }

    /// {@return the list of diagnostics produced during linking, parsing, and
    /// compilation, filtered to only those originating from this editor's own
    /// source.}
    public ObservableList<Diagnostic> getDiagnostics() {
        return diagnosticsProperty().get();
    }

    /// {@return a read-only property representing the linker output, or `null`
    /// if no source has been set yet}
    public ReadOnlyObjectProperty<LinkedProgram> linkedProgramProperty() {
        return this.linkedProgram.getReadOnlyProperty();
    }

    /// {@return the current linker output, or `null` if no source has been set}
    public LinkedProgram getLinkedProgram() {
        return linkedProgramProperty().get();
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

    /// {@return a read-only property listing virtual source origins for every
    /// file this editor's master includes (transitively). Empty when the
    /// program has no `.include` directives.}
    public ReadOnlyListProperty<SourceOrigin> includedOriginsProperty() {
        return this.includedOrigins.getReadOnlyProperty();
    }

    /// Forces a re-link with the current source, file, and buffer provider.
    /// Called by the workspace when another editor's source changes so that
    /// masters whose included files are open get an up-to-date view.
    public void refresh() {
        relink();
    }

    private void relink() {
        this.modifiableDiagnostics.clear();
        this.compiledProgram.set(null);

        final var src = this.source.get();
        if (src == null) {
            this.linkedProgram.set(null);
            this.parsedProgram.set(null);
            this.modifiableIncludedOrigins.clear();
            return;
        }

        final var linked = new Linker().link(this.programId, getFile(), src, this.bufferProvider);
        this.linkedProgram.set(linked);
        this.modifiableDiagnostics.addAll(linked.diagnostics());
        rebuildIncludedOrigins(linked);

        final var parsed = new Parser().parse(linked);
        this.modifiableDiagnostics.addAll(parsed.diagnostics());
        this.parsedProgram.set(parsed);
    }

    private void rebuildIncludedOrigins(final LinkedProgram linked) {
        final var newOrigins = linked.units().stream()
                .filter(unit -> !this.programId.equals(unit.id()))
                .map(unit -> (SourceOrigin) new IncludedFileOrigin(unit, this.modifiableDiagnostics))
                .toList();
        this.modifiableIncludedOrigins.setAll(newOrigins);
    }

    /// Creates a new file with example source code.
    void newFile() throws IOException {
        final var fileName = "example.s";
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
    /// Refuses to compile when the current diagnostics list already contains
    /// errors from earlier pipeline stages (linking, lexing, parsing) — the
    /// parser skips bad lines and produces a partial program from whatever was
    /// salvageable, so compiling that subset would yield a misleadingly
    /// successful build. This matters especially for include errors: a problem
    /// in an included file otherwise lets the master appear to compile.
    ///
    /// @return `true` if compilation succeeded without errors, `false` otherwise
    public boolean compile() {
        // Remove all compiler problems
        this.modifiableDiagnostics.removeIf(d -> d.stage() == Diagnostic.Stage.COMPILING);

        final var parsed = getParsedProgram();
        if (parsed == null) {
            this.compiledProgram.set(null);
            return false;
        }

        if (hasPreCompileErrors()) {
            this.compiledProgram.set(null);
            return false;
        }

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

    /// {@return whether the diagnostics list currently contains any
    /// error-severity entry from a pre-compile stage (linking, lexing, or
    /// parsing).}
    private boolean hasPreCompileErrors() {
        return this.modifiableDiagnostics.stream()
                .anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR
                        && d.stage() != Diagnostic.Stage.COMPILING);
    }

    /// Marks the editor content as modified.
    void markDirty() {
        this.dirty.set(true);
    }
}
