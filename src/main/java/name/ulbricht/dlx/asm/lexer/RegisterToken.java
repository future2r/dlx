package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A general-purpose register `r0`..`r31`.
///
/// @param pos    0-based source position of this token.
/// @param raw    The entire register text as written, e.g. `"r0"`, `"R15"`, `"r31"`.
/// @param number The register index, e.g. `0`, `15`, `31`.
public record RegisterToken(TextPosition pos, String raw, int number) implements Token {

    /// Validates the record components.
    public RegisterToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
    }
}
