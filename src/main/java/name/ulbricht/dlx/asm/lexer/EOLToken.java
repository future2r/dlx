package name.ulbricht.dlx.asm.lexer;

/// End of a source line. Marks instruction boundary for parser.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
public record EOLToken(int line, int col) implements Token {

    @Override
    public String raw() {
        return "\n";
    }
}
