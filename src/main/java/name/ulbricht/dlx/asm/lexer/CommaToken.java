package name.ulbricht.dlx.asm.lexer;

/// `','` separating operands.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
public record CommaToken(int line, int col) implements Token {

    @Override
    public String raw() {
        return ",";
    }
}
