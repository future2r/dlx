package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A quoted string: `"hello\n"`
///
/// `value` — processed content (escape sequences resolved). `raw` — includes the
/// surrounding quote characters.
///
/// @param pos   0-based source position of this token.
/// @param raw   The entire string literal as written, including surrounding quotes.
/// @param value The processed string value with escape sequences resolved.
public record StringLiteralToken(TextPosition pos, String raw, String value) implements Token {

    /// Validates the record components.
    public StringLiteralToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
        requireNonNull(value, "value must not be null");
    }
}
