package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// `','` separating operands.
///
/// @param pos 0-based source position of this token.
public record CommaToken(TextPosition pos) implements Token {

    /// Validates the record components.
    public CommaToken {
        requireNonNull(pos, "pos must not be null");
    }

    @Override
    public String raw() {
        return ",";
    }
}
