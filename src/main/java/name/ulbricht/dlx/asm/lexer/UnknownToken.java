package name.ulbricht.dlx.asm.lexer;

/// An unrecognised character or malformed token.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The offending character(s).
public record UnknownToken(int line, int col, String raw) implements Token {
}
