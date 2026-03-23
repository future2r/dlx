package name.ulbricht.dlx.asm.lexer;

/// A label definition at the current location.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire label definition text, including the trailing colon,
///             e.g. `"loop:"`.
/// @param name The label name without the trailing colon, e.g. `"loop"`.
public record LabelDefinitionToken(int line, int col, String raw, String name) implements Token {
}
