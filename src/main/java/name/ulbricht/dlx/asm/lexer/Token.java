package name.ulbricht.dlx.asm.lexer;

import name.ulbricht.dlx.util.TextPosition;

/// DLX Token Type Definitions.
///
/// `pos` gives the 0-based source position of the token's first character.
/// `raw.length()` gives the span length, e.g. for underline error markers.
public sealed interface Token permits WhitespaceToken, CommentToken, EOLToken, DirectiveToken, LabelDefinitionToken,
        LabelReferenceToken, InstructionToken, RegisterToken,
        IntLiteralToken, StringLiteralToken, CommaToken, LeftParenToken, RightParenToken, UnknownToken {

    /// {@return 0-based source position of this token}
    TextPosition pos();

    /// {@return Original source text, never normalised}
    String raw();
}
