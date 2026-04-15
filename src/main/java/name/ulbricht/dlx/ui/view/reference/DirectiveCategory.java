package name.ulbricht.dlx.ui.view.reference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import name.ulbricht.dlx.asm.Directive;

/// Groups [Directive] constants into logical categories for the reference view.
public enum DirectiveCategory {

    /// Segment control directives.
    SEGMENTS(List.of(Directive.DATA, Directive.TEXT)),

    /// Data declaration directives.
    DATA_DECLARATIONS(List.of(
            Directive.WORD, Directive.HALF, Directive.BYTE,
            Directive.ASCII, Directive.ASCIIZ)),

    /// Memory layout directives.
    MEMORY_LAYOUT(List.of(Directive.SPACE, Directive.ALIGN));

    private final List<Directive> directives;

    DirectiveCategory(final List<Directive> directives) {
        this.directives = directives;
    }

    /// {@return the directives belonging to this category}
    public List<Directive> directives() {
        return this.directives;
    }

    /// Reverse lookup from directive to its category, built once.
    private static final Map<Directive, DirectiveCategory> BY_DIRECTIVE =
            Stream.of(values())
                    .flatMap(cat -> cat.directives().stream().map(dir -> Map.entry(dir, cat)))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    /// {@return the category that contains the given directive}
    ///
    /// @param directive the directive to look up
    /// @throws IllegalArgumentException if the directive is not in any category
    public static DirectiveCategory categoryOf(final Directive directive) {
        final var category = BY_DIRECTIVE.get(directive);
        if (category == null) {
            throw new IllegalArgumentException("No category for directive: " + directive);
        }
        return category;
    }
}
