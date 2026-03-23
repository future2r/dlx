package name.ulbricht.dlx.asm.lexer;

/// `'('` in a memory operand: `offset(rBase)`.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
public record LeftParenToken(int line, int col) implements Token {

    @Override
    public String raw() {
        return "(";
    }
}
