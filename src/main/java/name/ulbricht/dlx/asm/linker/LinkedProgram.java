package name.ulbricht.dlx.asm.linker;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.lexer.Token;
import name.ulbricht.dlx.asm.lexer.TokenizedProgram;

/// The result of the linker stage: a flat, line-grouped token stream that
/// represents the fully-expanded program with all `.include` directives
/// resolved.
///
/// The parser consumes a [LinkedProgram] instead of a single
/// [TokenizedProgram], allowing every diagnostic it produces to be attributed
/// to the originating source file via the parallel [#lineSourceIds] list.
///
/// Single-file programs go through the linker too — they end up with a
/// one-element [#units] list, [#lineSourceIds] containing only the master's id,
/// and no linker diagnostics. The [#singleUnit] factory wraps a bare
/// [TokenizedProgram] when no include resolution is needed (used by tests and
/// callers that already have a tokenised program).
///
/// @param masterId       id of the master source unit (always the first entry
///                       in [#units])
/// @param units          every distinct source file referenced by the program,
///                       master first, followed by includes in resolution order
/// @param linesByUnit    the program's logical lines in linked order; each
///                       inner list comes entirely from a single source unit
/// @param lineSourceIds  parallel to [#linesByUnit]; identifies which
///                       [SourceUnit] each line originates from
/// @param diagnostics    aggregated diagnostics from per-file lexing plus any
///                       errors raised during include resolution
public record LinkedProgram(
        UUID masterId,
        List<SourceUnit> units,
        List<List<Token>> linesByUnit,
        List<UUID> lineSourceIds,
        List<Diagnostic> diagnostics) {

    /// Validates and defensively copies all lists. The two line-parallel lists
    /// must have the same size.
    public LinkedProgram {
        requireNonNull(masterId, "masterId must not be null");
        requireNonNull(units, "units must not be null");
        requireNonNull(linesByUnit, "linesByUnit must not be null");
        requireNonNull(lineSourceIds, "lineSourceIds must not be null");
        requireNonNull(diagnostics, "diagnostics must not be null");

        if (linesByUnit.size() != lineSourceIds.size()) {
            throw new IllegalArgumentException(
                    "linesByUnit and lineSourceIds must have the same size: "
                            + linesByUnit.size() + " vs " + lineSourceIds.size());
        }

        units = List.copyOf(units);
        linesByUnit = linesByUnit.stream().map(List::copyOf).toList();
        lineSourceIds = List.copyOf(lineSourceIds);
        diagnostics = List.copyOf(diagnostics);
    }

    /// Wraps a single [TokenizedProgram] as a trivial [LinkedProgram] with no
    /// included children. Used by callers that have already lexed their source
    /// and do not need include resolution (tests and the linker's own internal
    /// single-file fast path).
    ///
    /// The wrapper carries no [SourceUnit#path]; if a caller needs path
    /// metadata for downstream UI features it should go through the full
    /// [Linker] entry point instead.
    ///
    /// @param tokenized the lexer output to wrap
    /// @return a [LinkedProgram] containing exactly one [SourceUnit] and one
    ///         line group per logical line in `tokenized`
    public static LinkedProgram singleUnit(final TokenizedProgram tokenized) {
        requireNonNull(tokenized, "tokenized must not be null");

        final var unit = new SourceUnit(tokenized.id(), null, "", tokenized);
        final var lines = tokenized.lines();
        final var sourceIds = lines.stream().map(_ -> tokenized.id()).toList();
        return new LinkedProgram(tokenized.id(), List.of(unit), lines, sourceIds, tokenized.diagnostics());
    }
}
