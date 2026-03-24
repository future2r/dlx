package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// End of a source line. Marks instruction boundary for the parser.
///
/// @param pos 0-based source position just past the last character of the line.
public record EOLToken(TextPosition pos) implements Token {

    /// Validates the record components.
    public EOLToken {
        requireNonNull(pos, "pos must not be null");
    }

    @Override
    public String raw() {
        return "\n";
    }
}
