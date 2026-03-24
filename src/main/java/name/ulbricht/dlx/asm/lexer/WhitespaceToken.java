package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// One or more space / tab characters between other tokens.
///
/// @param pos 0-based source position of this token.
/// @param raw The entire whitespace text, e.g. `" "`, `"\t"`, `"  "`.
public record WhitespaceToken(TextPosition pos, String raw) implements Token {

    /// Validates the record components.
    public WhitespaceToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
    }
}
