package name.ulbricht.dlx.asm.lexer;

/// A known instruction mnemonic.
/// 
/// @param line Line number of the comma token.
/// @param col  Column number of the comma token.
/// @param raw  The entire instruction text, e.g. `"ADD"`, `"lw"`, `"Beqz"`.
/// @param name Lowercase normalised name, e.g. `"add"`, `"lw"`, `"beqz"`.
public record InstructionToken(int line, int col, String raw, String name) implements Token {
}
