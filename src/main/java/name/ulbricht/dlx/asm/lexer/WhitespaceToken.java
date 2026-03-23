package name.ulbricht.dlx.asm.lexer;

/// One or more space / tab characters between other tokens.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire whitespace text, e.g. `" "`, `"\t"`, `" "`.
public record WhitespaceToken(int line, int col, String raw) implements Token {
}
