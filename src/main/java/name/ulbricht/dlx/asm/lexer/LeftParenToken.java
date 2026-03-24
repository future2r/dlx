package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// `'('` in a memory operand: `offset(rBase)`.
///
/// @param pos 0-based source position of this token.
public record LeftParenToken(TextPosition pos) implements Token {

    /// Validates the record components.
    public LeftParenToken {
        requireNonNull(pos, "pos must not be null");
    }

    @Override
    public String raw() {
        return "(";
    }
}
