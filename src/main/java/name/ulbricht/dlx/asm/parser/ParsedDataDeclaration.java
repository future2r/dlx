package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;

import name.ulbricht.dlx.util.TextPosition;

/// A data declaration parsed from a `.data` section.
///
/// The `values` list holds [Integer] elements for numeric directives (`word`,
/// `half`, `byte`, `space`, `align`) and a single [String] element for string
/// directives (`ascii`, `asciiz`).
///
/// @param pos       0-based source position of the directive token
/// @param label     the optional label that names this datum; `null` if absent
/// @param directive the directive keyword without the leading dot, lowercase
///                  (e.g. `word`, `ascii`)
/// @param values    the directive's operand values
public record ParsedDataDeclaration(TextPosition pos, String label, String directive, List<Object> values)
        implements ParsedElement {

    /// Validates and defensively copies the record components.
    public ParsedDataDeclaration {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(directive, "directive must not be null");
        requireNonNull(values, "values must not be null");
        values = List.copyOf(values);
    }
}
