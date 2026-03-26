package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of parsing a DLX assembly source file.
///
/// Multiple `.data` and `.text` segment switches in the source are supported;
/// the parser merges all data declarations into `data` and all instructions into
/// `code`, preserving their relative order within each section.
///
/// The `diagnostics` list collects all problems found during both lexing and
/// parsing.
///
/// @param id          a unique identifier for the parsed program
/// @param data        the list of data declarations, in source order
/// @param code        the list of instructions, in source order
/// @param diagnostics the list of diagnostics produced during parsing
public record ParsedProgram(UUID id, List<ParsedDataDeclaration> data, List<ParsedInstruction> code,
        List<Diagnostic> diagnostics) {

    /// Validates and defensively copies all lists.
    public ParsedProgram {
        requireNonNull(id);
        requireNonNull(data, "data must not be null");
        requireNonNull(code, "code must not be null");
        requireNonNull(diagnostics, "diagnostics must not be null");

        data = List.copyOf(data);
        code = List.copyOf(code);
        diagnostics = List.copyOf(diagnostics);
    }
}
