package name.ulbricht.dlx.asm;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A structured error or warning produced during lexing or parsing.
///
/// @param stage   the stage of the compilation process where the diagnostic was
///                produced
/// @param pos     0-based source position of the problematic token
/// @param message human-readable description of the problem
public record Diagnostic(Stage stage, TextPosition pos, String message) {

    /// The stage of the compilation process where the diagnostic was produced.
    public enum Stage {

        /// Diagnostic produced during lexing.
        LEXING,

        /// Diagnostic produced during parsing.
        PARSING
    }

    /// Validates the record components.
    public Diagnostic {
        requireNonNull(stage, "stage must not be null");
        requireNonNull(pos, "pos must not be null");
        requireNonNull(message, "message must not be null");
    }
}
