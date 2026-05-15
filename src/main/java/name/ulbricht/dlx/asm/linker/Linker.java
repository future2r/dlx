package name.ulbricht.dlx.asm.linker;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.lexer.DirectiveToken;
import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.lexer.StringLiteralToken;
import name.ulbricht.dlx.asm.lexer.Token;
import name.ulbricht.dlx.asm.lexer.TokenizedProgram;
import name.ulbricht.dlx.io.SourceFile;
import name.ulbricht.dlx.util.TextPosition;

/// The linker stage of the DLX assembler.
///
/// Sits between the lexer and the parser. Resolves `.include "relative/path.s"`
/// directives by lexing each referenced file with its own [SourceUnit#id] and
/// splicing the resulting token lines into the master's stream. The output is a
/// [LinkedProgram] in which every logical line knows which source file produced
/// it, so downstream stages can attribute their diagnostics to the right file.
///
/// Path resolution is relative to the directory of the **including** file (the
/// master for top-level includes; the included file for nested ones). Absolute
/// paths are rejected. Cycles are detected and reported. Repeated includes of
/// the same file along different paths are allowed — the file's tokens are
/// spliced in once per inclusion, but the file is only lexed once and
/// contributes a single [SourceUnit] to [LinkedProgram#units].
public final class Linker {

    private static final System.Logger log = System.getLogger(Linker.class.getName());

    /// Creates a new linker.
    public Linker() {
    }

    /// Links the master source with any files it transitively includes.
    ///
    /// The master is always lexed regardless of whether it has a file path; an
    /// unsaved master can still be linked, but any `.include` directives it contains
    /// will produce a diagnostic because there is no directory to resolve the
    /// relative path against.
    ///
    /// @param masterId     stable identifier of the master source unit
    /// @param masterPath   the master's file path, or `null` if it has never been
    ///                     saved
    /// @param masterSource the master's current source text
    /// @param buffers      live-buffer provider; pass [BufferProvider#NONE] to
    ///                     always read includes from disk
    /// @return the linked program with diagnostics from lexing each unit plus any
    ///         include-resolution errors
    public LinkedProgram link(
            final UUID masterId,
            final Path masterPath,
            final String masterSource,
            final BufferProvider buffers) {

        requireNonNull(masterId, "masterId must not be null");
        requireNonNull(masterSource, "masterSource must not be null");
        requireNonNull(buffers, "buffers must not be null");

        log.log(System.Logger.Level.INFO, "Starting linking of program " + masterId + ".");

        final var state = new State(buffers);

        final var masterTokenized = lex(masterId, masterSource);
        final var masterUnit = new SourceUnit(masterId, masterPath, masterSource, masterTokenized);
        state.registerUnit(masterUnit);

        final var masterCanonical = canonicalize(masterPath);
        if (masterCanonical != null) {
            state.stack.add(masterCanonical);
        }

        processUnit(masterUnit, masterPath, state);

        if (masterCanonical != null) {
            state.stack.remove(masterCanonical);
        }

        final var errorCount = state.diagnostics.stream()
                .filter(d -> d.severity() == Diagnostic.Severity.ERROR).count();
        if (errorCount == 0) {
            log.log(System.Logger.Level.INFO, "Linking of program " + masterId + " completed successfully.");
        } else {
            log.log(System.Logger.Level.WARNING,
                    "Linking of program " + masterId + " completed with " + errorCount + " error(s).");
        }

        return new LinkedProgram(masterId, state.unitOrder(), state.outputLines, state.outputSourceIds,
                state.diagnostics);
    }

    /// Walks every line of a unit, splicing regular lines into the output and
    /// recursing into includes.
    private void processUnit(final SourceUnit unit, final Path unitPath, final State state) {
        for (final var line : unit.tokenized().lines()) {
            switch (classify(line)) {
                case final RegularLine r -> {
                    state.outputLines.add(r.tokens());
                    state.outputSourceIds.add(unit.id());
                }
                case final IncludeLine inc -> resolveInclude(inc, unit, unitPath, state);
                case final MalformedIncludeLine bad -> state.diagnostics.add(linkerError(bad.directive(), unit.id(),
                        "Malformed .include directive (expected: .include \"path\")"));
            }
        }
    }

    /// Resolves a single include and recurses into the resolved file's lines.
    private void resolveInclude(
            final IncludeLine inc,
            final SourceUnit parent,
            final Path parentPath,
            final State state) {

        final var canonical = resolveIncludePath(inc, parent, parentPath, state);
        if (canonical == null) {
            return;
        }

        final var unit = loadUnit(canonical, inc, parent, state);
        if (unit == null) {
            return;
        }

        state.stack.add(canonical);
        processUnit(unit, canonical, state);
        state.stack.remove(canonical);
    }

    /// Validates the include's raw path and resolves it against the parent's
    /// directory. Returns the canonical path, or `null` if the include cannot be
    /// resolved (a diagnostic has already been added in that case).
    private static Path resolveIncludePath(
            final IncludeLine inc,
            final SourceUnit parent,
            final Path parentPath,
            final State state) {

        final var raw = inc.pathToken().value();
        final var asPath = safeAsPath(raw);
        if (asPath == null) {
            state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                    "Invalid include path: " + raw));
            return null;
        }
        if (asPath.isAbsolute()) {
            state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                    "Absolute include paths are not supported: " + raw));
            return null;
        }
        if (parentPath == null) {
            state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                    "Cannot resolve include: save this file first to establish a working directory."));
            return null;
        }

        final var parentDir = parentPath.getParent();
        final var resolved = (parentDir == null ? asPath : parentDir.resolve(asPath)).normalize();

        try {
            final var canonical = resolved.toRealPath();
            if (state.stack.contains(canonical)) {
                state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                        "Cyclic include of " + canonical));
                return null;
            }
            return canonical;
        } catch (final IOException _) {
            state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                    "Cannot find include file: " + resolved));
            return null;
        }
    }

    /// Returns the already-registered [SourceUnit] for the canonical path or loads,
    /// lexes, and registers a new one. Returns `null` if the file cannot be read (a
    /// diagnostic has already been added in that case).
    private static SourceUnit loadUnit(final Path canonical, final IncludeLine inc, final SourceUnit parent,
            final State state) {

        final var cached = state.unitsByPath.get(canonical);
        if (cached != null) {
            return cached;
        }

        final var childSource = readSource(canonical, inc, parent, state);
        if (childSource == null) {
            return null;
        }

        final var childUuid = state.uuidByPath.computeIfAbsent(canonical,
                p -> UUID.nameUUIDFromBytes(p.toString().getBytes(UTF_8)));
        final var childTokenized = lex(childUuid, childSource);
        final var unit = new SourceUnit(childUuid, canonical, childSource, childTokenized);
        state.registerUnit(unit);
        return unit;
    }

    /// Reads the source for an include, preferring a live editor buffer over the
    /// on-disk content. Returns `null` on I/O failure with a diagnostic
    /// already recorded.
    private static String readSource(final Path canonical, final IncludeLine inc, final SourceUnit parent,
            final State state) {

        final var fromBuffer = state.buffers.sourceFor(canonical);
        if (fromBuffer.isPresent()) {
            return fromBuffer.get();
        }
        try {
            return SourceFile.read(canonical);
        } catch (final IOException e) {
            state.diagnostics.add(linkerError(inc.directive(), parent.id(),
                    "Cannot read include file: " + canonical + " (" + e.getMessage() + ")"));
            return null;
        }
    }

    private static TokenizedProgram lex(final UUID id, final String source) {
        return new Lexer(LexerMode.ASSEMBLER).tokenize(id, source);
    }

    /// Classifies a single tokenised line as a regular line, a well-formed include,
    /// or a malformed include.
    private static ClassifiedLine classify(final List<Token> line) {
        if (line.isEmpty() || !(line.getFirst() instanceof final DirectiveToken dt)
                || !"include".equals(dt.name())) {
            return new RegularLine(line);
        }
        if (line.size() == 2 && line.get(1) instanceof final StringLiteralToken slt) {
            return new IncludeLine(dt, slt);
        }
        return new MalformedIncludeLine(dt);
    }

    private static Path safeAsPath(final String raw) {
        try {
            return Path.of(raw);
        } catch (final InvalidPathException _) {
            return null;
        }
    }

    private static Path canonicalize(final Path path) {
        if (path == null) {
            return null;
        }
        try {
            return path.toRealPath();
        } catch (final IOException _) {
            return null;
        }
    }

    private static Diagnostic linkerError(final Token at, final UUID sourceId, final String message) {
        final var pos = at.pos();
        return new Diagnostic(Diagnostic.Stage.LINKING, Diagnostic.Severity.ERROR, sourceId,
                new TextPosition(pos.line(), pos.column(), at.raw().length()), message);
    }

    /// Mutable bookkeeping shared across the recursive resolution walk.
    private static final class State {

        private final BufferProvider buffers;
        private final Map<Path, SourceUnit> unitsByPath = new LinkedHashMap<>();
        private final Map<UUID, SourceUnit> unitsById = new LinkedHashMap<>();
        private final Map<Path, UUID> uuidByPath = new HashMap<>();
        private final Set<Path> stack = new HashSet<>();
        private final List<List<Token>> outputLines = new ArrayList<>();
        private final List<UUID> outputSourceIds = new ArrayList<>();
        private final List<Diagnostic> diagnostics = new ArrayList<>();

        State(final BufferProvider buffers) {
            this.buffers = buffers;
        }

        void registerUnit(final SourceUnit unit) {
            if (this.unitsById.putIfAbsent(unit.id(), unit) != null) {
                return;
            }
            if (unit.path() != null) {
                this.unitsByPath.put(unit.path(), unit);
            }
            this.diagnostics.addAll(unit.tokenized().diagnostics());
        }

        List<SourceUnit> unitOrder() {
            return List.copyOf(this.unitsById.values());
        }
    }

    /// Internal classification of a single tokenised line.
    private sealed interface ClassifiedLine permits RegularLine, IncludeLine, MalformedIncludeLine {
        // no members
    }

    private record RegularLine(List<Token> tokens) implements ClassifiedLine {
    }

    private record IncludeLine(DirectiveToken directive, StringLiteralToken pathToken) implements ClassifiedLine {
    }

    private record MalformedIncludeLine(DirectiveToken directive) implements ClassifiedLine {
    }
}
