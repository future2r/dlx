package name.ulbricht.dlx.asm.lexer;

/// A general-purpose register `r0`..`r31`.
/// 
/// @param line   Line number of the comma token.
/// @param col    Column number of the comma token.
/// @param raw    The entire register text, e.g. `"r0"`, `"R15"`, `"r31"`.
/// @param number The register index, e.g. `0`, `15`, `31`.
public record RegisterToken(int line, int col, String raw, int number) implements Token {
}
