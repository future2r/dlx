package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// An integer literal. Covers decimal (`42`, `-5`) and hexadecimal (`0xFF`).
///
/// @param pos   0-based source position of this token.
/// @param raw   The entire literal text as written, e.g. `"42"`, `"-5"`, `"0xFF"`.
/// @param value The parsed integer value.
public record IntLiteralToken(TextPosition pos, String raw, int value) implements Token {

    /// Validates the record components.
    public IntLiteralToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
    }
}
