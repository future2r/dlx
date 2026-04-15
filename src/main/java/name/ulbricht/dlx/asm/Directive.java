package name.ulbricht.dlx.asm;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// All DLX assembler directives with their name.
///
/// This enum is the single source of truth for directive names used by the
/// lexer (token classification), parser (directive dispatch), and compiler
/// (data emission).
///
/// Use [#fromName(String)] for an O(1) lookup by lowercase name, or
/// [#isKnown(String)] for a fast membership test.
public enum Directive {

    // -------------------------------------------------------------------------
    // Segment switches
    // -------------------------------------------------------------------------

    /// Switch to data segment.
    DATA("data"),

    /// Switch to text (code) segment.
    TEXT("text"),

    // -------------------------------------------------------------------------
    // Data declarations
    // -------------------------------------------------------------------------

    /// Declare one or more 32-bit words.
    WORD("word"),

    /// Declare one or more 16-bit half-words.
    HALF("half"),

    /// Declare one or more bytes.
    BYTE("byte"),

    /// Declare an ASCII string (no null terminator).
    ASCII("ascii"),

    /// Declare a null-terminated ASCII string.
    ASCIIZ("asciiz"),

    /// Reserve `n` bytes of zero-filled space.
    SPACE("space"),

    /// Align the next datum to a `2^n` byte boundary.
    ALIGN("align");

    // -------------------------------------------------------------------------
    // Fields and lookup
    // -------------------------------------------------------------------------

    
    private final String directiveName;

    /// Constructs a directive constant.
    Directive(final String directiveName) {
        this.directiveName = directiveName;
    }

    /// {@return the lowercase directive name as it appears in assembly source code
    /// (without the leading dot)}
    public String directiveName() {
        return this.directiveName;
    }

    /// O(1) lookup table keyed by lowercase directive name.
    private static final Map<String, Directive> BY_NAME =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(d -> d.directiveName, d -> d));

    /// Returns the directive whose [#directiveName] equals the given string.
    ///
    /// @param name a lowercase directive name such as `"word"` or `"data"`
    /// @return the matching constant, or empty if unknown
    public static Optional<Directive> fromName(final String name) {
        return Optional.ofNullable(BY_NAME.get(name));
    }

    /// Tests whether the given string is a known directive name.
    ///
    /// @param name a lowercase directive name
    /// @return `true` if a matching [Directive] constant exists
    public static boolean isKnown(final String name) {
        return BY_NAME.containsKey(name);
    }
}
