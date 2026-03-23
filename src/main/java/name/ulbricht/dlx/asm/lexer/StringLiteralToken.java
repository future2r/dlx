package name.ulbricht.dlx.asm.lexer;

/// A quoted string: `"hello\n"`
///
/// `value` — processed content (escape sequences resolved). `raw` — includes the
/// surrounding quote characters.
/// 
/// @param line  Line number of the comma token.
/// @param col   Column number of the comma token.
/// @param raw   The entire string literal text, including the surrounding
///              quotes, e.g. `"hello\n"`.
/// @param value The processed string value, e.g. `hello` followed by a
///              newline character.
public record StringLiteralToken(int line, int col, String raw, String value) implements Token {
}
