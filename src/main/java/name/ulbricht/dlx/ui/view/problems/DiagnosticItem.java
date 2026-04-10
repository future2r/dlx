package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.util.TextPosition;

/// Represents an individual diagnostic entry in the problems tree view.
///
/// @param diagnostic   the diagnostic this item represents
/// @param sourceOrigin the source origin that produced this diagnostic
public record DiagnosticItem(Diagnostic diagnostic, SourceOrigin sourceOrigin) implements ProblemItem {

    /// Creates a new diagnostic item.
    public DiagnosticItem {
        requireNonNull(diagnostic);
        requireNonNull(sourceOrigin);
    }

    /// {@return the text position of this diagnostic}
    public TextPosition textPosition() {
        return this.diagnostic.pos();
    }

    /// {@return the message of this diagnostic}
    public String message() {
        return this.diagnostic.message();
    }

    /// {@return the compilation stage of this diagnostic}
    public Diagnostic.Stage stage() {
        return this.diagnostic.stage();
    }
}
