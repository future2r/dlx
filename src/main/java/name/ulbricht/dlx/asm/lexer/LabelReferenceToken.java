package name.ulbricht.dlx.asm.lexer;

/// A label used as an operand (branch target, data address).
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire label reference text, e.g. `"loop"`.
/// @param name The label name, e.g. `"loop"`.
public record LabelReferenceToken(int line, int col, String raw, String name)
        implements Token {
}
