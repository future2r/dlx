package name.ulbricht.dlx.asm.lexer;

/// A dot-directive.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire directive text, including the leading dot, e.g.
///             `".word"`.
/// @param name Lowercase normalised name WITHOUT the leading dot.
public record DirectiveToken(int line, int col, String raw, String name) implements Token {
}
