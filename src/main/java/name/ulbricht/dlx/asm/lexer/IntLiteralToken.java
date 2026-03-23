package name.ulbricht.dlx.asm.lexer;

/// An integer literal. Covers decimal (`42`, `-5`) and hexadecimal (`0xFF`).
/// 
/// @param line  Line number of the comma token.
/// @param col   Column number of the comma token.
/// @param raw   The entire literal text, e.g. `"42"`, `"-5"`, `"0xFF"`.
/// @param value The parsed integer value.
public record IntLiteralToken(int line, int col, String raw, int value) implements Token {
}
