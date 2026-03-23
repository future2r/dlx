package name.ulbricht.dlx.asm.lexer;

/// Everything from `';'` to end of line, including the `';'`.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire comment text, including the leading `';'`.
public record CommentToken(int line, int col, String raw) implements Token {
}
