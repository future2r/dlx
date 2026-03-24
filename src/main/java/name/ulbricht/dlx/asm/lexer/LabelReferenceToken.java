package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A label used as an operand (branch target, data address).
///
/// @param pos  0-based source position of this token.
/// @param raw  The entire label reference text, e.g. `"loop"`.
/// @param name The label name, lowercase normalised, e.g. `"loop"`.
public record LabelReferenceToken(TextPosition pos, String raw, String name) implements Token {

    /// Validates the record components.
    public LabelReferenceToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
        requireNonNull(name, "name must not be null");
    }
}
