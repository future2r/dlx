package name.ulbricht.dlx.asm;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import name.ulbricht.dlx.util.TextPosition;

/// A structured error or warning produced during a stage of the assembler
/// pipeline.
///
/// The [#sourceId] identifies the source unit the [#pos] is interpreted
/// against. For single-file programs this is the master program id; for
/// multi-file programs (after include resolution) it is the id of the file the
/// diagnostic actually originates from.
///
/// @param stage    the stage of the compilation process where the diagnostic was
///                 produced
/// @param severity the severity level of the diagnostic
/// @param sourceId identifier of the source unit the [#pos] refers to
/// @param pos      0-based source position of the problematic token
/// @param message  human-readable description of the problem
public record Diagnostic(Stage stage, Severity severity, UUID sourceId, TextPosition pos, String message) {

    /// The severity level of a diagnostic.
    public enum Severity {

        /// Diagnostic produced for informational purposes, not indicating any problem.
        INFO,

        /// Diagnostic produced for a potential issue that may not necessarily be a problem.
        WARNING,

        /// Diagnostic produced for an error that prevents successful compilation.
        ERROR;

        /// {@return the corresponding log level for this severity}
        public System.Logger.Level toLogLevel() {
            return switch (this) {
                case INFO -> System.Logger.Level.INFO;
                case WARNING -> System.Logger.Level.WARNING;
                case ERROR -> System.Logger.Level.ERROR;
            };
        }
    }

    /// The stage of the compilation process where the diagnostic was produced.
    public enum Stage {

        /// Diagnostic produced during lexing.
        LEXING,

        /// Diagnostic produced during include resolution by the linker stage.
        LINKING,

        /// Diagnostic produced during parsing.
        PARSING,

        /// Diagnostic produced during compiling.
        COMPILING
    }

    /// Validates the record components.
    public Diagnostic {
        requireNonNull(stage, "stage must not be null");
        requireNonNull(severity, "severity must not be null");
        requireNonNull(sourceId, "sourceId must not be null");
        requireNonNull(pos, "pos must not be null");
        requireNonNull(message, "message must not be null");
    }
}
