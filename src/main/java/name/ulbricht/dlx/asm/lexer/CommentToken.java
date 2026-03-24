package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// Everything from `';'` to end of line, including the `';'`.
///
/// @param pos 0-based source position of this token.
/// @param raw The entire comment text, including the leading `';'`.
public record CommentToken(TextPosition pos, String raw) implements Token {

    /// Validates the record components.
    public CommentToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
    }
}
