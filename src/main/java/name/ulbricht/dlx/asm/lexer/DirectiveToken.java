package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A dot-directive.
///
/// @param pos  0-based source position of this token.
/// @param raw  The entire directive text, including the leading dot, e.g. `".word"`.
/// @param name Lowercase normalised name WITHOUT the leading dot.
public record DirectiveToken(TextPosition pos, String raw, String name) implements Token {

    /// Validates the record components.
    public DirectiveToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
        requireNonNull(name, "name must not be null");
    }
}
