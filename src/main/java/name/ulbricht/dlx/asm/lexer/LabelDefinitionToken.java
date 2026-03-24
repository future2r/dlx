package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A label definition at the current location.
///
/// @param pos  0-based source position of this token.
/// @param raw  The entire label text, including the trailing colon, e.g. `"loop:"`.
/// @param name The label name without the trailing colon, e.g. `"loop"`.
public record LabelDefinitionToken(TextPosition pos, String raw, String name) implements Token {

    /// Validates the record components.
    public LabelDefinitionToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
        requireNonNull(name, "name must not be null");
    }
}
