package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of tokenising a DLX assembly source file.
///
/// Produced by [Lexer#tokenize]. Carries both the flat token list (whitespace
/// and comments already stripped in [LexerMode#ASSEMBLER] mode) and any
/// diagnostics the lexer detected.
///
/// @param id          a unique identifier for the tokenised program
/// @param tokens      the flat token list in source order
/// @param diagnostics the list of diagnostics produced during tokenisation
public record TokenizedProgram(UUID id, List<Token> tokens, List<Diagnostic> diagnostics) {

    /// Validates and defensively copies both lists.
    public TokenizedProgram {
        requireNonNull(id);
        requireNonNull(tokens, "tokens must not be null");
        requireNonNull(diagnostics, "diagnostics must not be null");

        tokens = List.copyOf(tokens);
        diagnostics = List.copyOf(diagnostics);
    }
}
