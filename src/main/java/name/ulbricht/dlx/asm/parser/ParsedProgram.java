package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of parsing a DLX assembly source file.
///
/// Multiple `.data` and `.text` segment switches in the source are supported;
/// the parser merges all data declarations into `data` and all instructions into
/// `code`, preserving their relative order within each section.
///
/// The `errors` list collects all problems found during both lexing and parsing.
/// An empty list means the source was accepted without any errors.
///
/// @param data   the list of data declarations, in source order
/// @param code   the list of instructions, in source order
/// @param errors the combined list of lexer and parser diagnostics; empty when
///               the source was error-free
public record ParsedProgram(
        List<ParsedDataDeclaration> data,
        List<ParsedInstruction> code,
        List<Diagnostic> errors) {

    /// Validates and defensively copies all lists.
    public ParsedProgram {
        requireNonNull(data, "data must not be null");
        requireNonNull(code, "code must not be null");
        requireNonNull(errors, "errors must not be null");

        data = List.copyOf(data);
        code = List.copyOf(code);
        errors = List.copyOf(errors);
    }

    /// {@return true if the source contained any errors, false otherwise}
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}
