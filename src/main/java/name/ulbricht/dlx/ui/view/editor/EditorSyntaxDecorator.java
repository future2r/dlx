package name.ulbricht.dlx.ui.view.editor;

import jfx.incubator.scene.control.richtext.SyntaxDecorator;
import jfx.incubator.scene.control.richtext.TextPos;
import jfx.incubator.scene.control.richtext.model.CodeTextModel;
import jfx.incubator.scene.control.richtext.model.RichParagraph;
import name.ulbricht.dlx.asm.lexer.CommaToken;
import name.ulbricht.dlx.asm.lexer.CommentToken;
import name.ulbricht.dlx.asm.lexer.DirectiveToken;
import name.ulbricht.dlx.asm.lexer.EOLToken;
import name.ulbricht.dlx.asm.lexer.InstructionToken;
import name.ulbricht.dlx.asm.lexer.IntLiteralToken;
import name.ulbricht.dlx.asm.lexer.LabelDefinitionToken;
import name.ulbricht.dlx.asm.lexer.LabelReferenceToken;
import name.ulbricht.dlx.asm.lexer.LeftParenToken;
import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.lexer.RegisterToken;
import name.ulbricht.dlx.asm.lexer.RightParenToken;
import name.ulbricht.dlx.asm.lexer.StringLiteralToken;
import name.ulbricht.dlx.asm.lexer.UnknownToken;

/// Syntax decorator for the editor's code area.
final class EditorSyntaxDecorator implements SyntaxDecorator {

    @Override
    public RichParagraph createRichParagraph(final CodeTextModel model, final int index) {
        final var builder = RichParagraph.builder();

        // get the text of the line
        final var line = model.getPlainText(index);

        // Use the lexer in highlighting mode to get the tokens of the line
        final var lexer = new Lexer(LexerMode.HIGHLIGHTING);
        final var tokens = lexer.tokenizeLine(line, index);

        // Handle all the tokens
        for (final var token : tokens) {

            final var style = switch (token) {
                case final InstructionToken _ -> "code-instruction";
                case final RegisterToken _ -> "code-register";
                case final IntLiteralToken _ -> "code-int";
                case final StringLiteralToken _ -> "code-string";
                case final DirectiveToken _ -> "code-directive";
                case final LabelDefinitionToken _ -> "code-label-definition";
                case final LabelReferenceToken _ -> "code-label-reference";
                case final CommentToken _ -> "code-comment";
                case final CommaToken _ -> "code-comma";
                case final LeftParenToken _ -> "code-paren";
                case final RightParenToken _ -> "code-paren";
                case final UnknownToken _ -> "code-unknown";
                case final EOLToken _ -> null; // do not add EOL tokens
                default -> "code-default";
            };

            if (style != null) {
                // get the raw text from the token
                final var text = token.raw();

                // Add the text with its style to the builder
                builder.addWithStyleNames(text, style);
            }
        }

        // build the rich paragraph
        return builder.build();
    }

    @Override
    public void handleChange(final CodeTextModel model, final TextPos start, final TextPos end, final int charsTop,
            final int linesAdded, final int charsBottom) {
        // nothing to do, the syntax can be determined based on a single line
    }
}
