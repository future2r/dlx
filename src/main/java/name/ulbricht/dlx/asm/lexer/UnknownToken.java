package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// An unrecognised character or malformed token.
///
/// @param pos 0-based source position of this token.
/// @param raw The offending character(s).
public record UnknownToken(TextPosition pos, String raw) implements Token {

    /// Validates the record components.
    public UnknownToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
    }
}
