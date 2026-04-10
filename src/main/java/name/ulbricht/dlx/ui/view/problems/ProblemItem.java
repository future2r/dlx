package name.ulbricht.dlx.ui.view.problems;

/// Represents an item in the problems tree view. This is a sealed interface
/// with two permitted implementations: {@link SourceOriginItem} for editor group
/// nodes and {@link DiagnosticItem} for individual diagnostic entries.
public sealed interface ProblemItem permits SourceOriginItem, DiagnosticItem {

    /// {@return the source origin that produced this item}
    SourceOrigin sourceOrigin();
}
