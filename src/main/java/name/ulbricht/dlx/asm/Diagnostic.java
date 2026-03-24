package name.ulbricht.dlx.asm;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A structured error or warning produced during lexing or parsing.
///
/// @param pos     0-based source position of the problematic token
/// @param length  character length of the problematic span (0 = point
///                diagnostic)
/// @param message human-readable description of the problem
public record Diagnostic(TextPosition pos, int length, String message) {

    /// Validates the record components.
    public Diagnostic {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(message, "message must not be null");
    }

    /// Returns a human-readable summary, e.g. `"Line 3, col 7 (len 2): message"`.
    @Override
    public String toString() {
        return "Line " + this.pos.displayLine() + ", col " + this.pos.displayCol()
                + " (len " + this.length + "): " + this.message;
    }
}
