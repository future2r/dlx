package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
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

    /// Groups the token list into logical lines by splitting on [EOLToken]
    /// boundaries. EOL tokens themselves are dropped; empty trailing lines are
    /// dropped too. The returned outer list and each inner list are
    /// unmodifiable.
    ///
    /// @return the tokens grouped by logical line
    public List<List<Token>> lines() {
        final var lines = new ArrayList<List<Token>>();
        var current = new ArrayList<Token>();
        for (final var token : this.tokens) {
            if (token instanceof EOLToken) {
                lines.add(List.copyOf(current));
                current = new ArrayList<>();
            } else {
                current.add(token);
            }
        }
        if (!current.isEmpty()) {
            lines.add(List.copyOf(current));
        }
        return List.copyOf(lines);
    }
}
