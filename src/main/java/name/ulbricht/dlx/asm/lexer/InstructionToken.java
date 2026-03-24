package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.util.TextPosition;

/// A known instruction mnemonic.
///
/// @param pos  0-based source position of this token.
/// @param raw  The entire instruction text as written, e.g. `"ADD"`, `"lw"`, `"Beqz"`.
/// @param name Lowercase normalised mnemonic, e.g. `"add"`, `"lw"`, `"beqz"`.
public record InstructionToken(TextPosition pos, String raw, String name) implements Token {

    /// Validates the record components.
    public InstructionToken {
        requireNonNull(pos, "pos must not be null");
        requireNonNull(raw, "raw must not be null");
        requireNonNull(name, "name must not be null");
    }
}
