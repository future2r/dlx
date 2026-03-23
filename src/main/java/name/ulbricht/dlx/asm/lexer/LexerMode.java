package name.ulbricht.dlx.asm.lexer;

/// Defines the lexer mode.
public enum LexerMode {
    
    ///  Emits all tokens, including whitespace and comments.
    HIGHLIGHTING,

    ///  Emits only "semantic" tokens, skipping whitespace and comments.
    ASSEMBLER
}
