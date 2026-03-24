package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import java.util.List;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of tokenising a DLX assembly source file.
///
/// Produced by [Lexer#tokenize]. Carries both the flat token list (whitespace
/// and comments already stripped in [LexerMode#ASSEMBLER] mode) and any errors
/// the lexer detected.
///
/// @param tokens the flat token list in source order
/// @param errors the list of diagnostics produced during tokenisation; empty
///               when the source was error-free
public record TokenizedProgram(List<Token> tokens, List<Diagnostic> errors) {

    /// Validates and defensively copies both lists.
    public TokenizedProgram {
        requireNonNull(tokens, "tokens must not be null");
        requireNonNull(errors, "errors must not be null");

        tokens = List.copyOf(tokens);
        errors = List.copyOf(errors);
    }

    /// {@return true if the source contained any errors, false otherwise}
    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }
}
