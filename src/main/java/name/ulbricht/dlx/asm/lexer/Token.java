package name.ulbricht.dlx.asm.lexer;

/// DLX Token Type Definitions.
///
/// `raw` + `col` lets the highlighter place colour spans exactly. `raw.length()`
/// gives the underline length for error markers.
public sealed interface Token permits WhitespaceToken, CommentToken, EOLToken, DirectiveToken, LabelDefinitionToken,
        LabelReferenceToken, InstructionToken, RegisterToken,
        IntLiteralToken, StringLiteralToken, CommaToken, LeftParenToken, RightParenToken, UnknownToken {

    /// {@return 1-based source line number}
    int line();

    /// {@return 0-based character offset within the line}
    int col();

    /// {@return Original source text, never normalised}
    String raw();
}
