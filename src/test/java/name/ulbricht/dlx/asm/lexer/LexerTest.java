package name.ulbricht.dlx.asm.lexer;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.util.TextPosition;

@DisplayName("Lexer")
final class LexerTest {

    @Nested
    @DisplayName("Basic tokens")
    class BasicTokens {

        @Test
        @DisplayName("Empty line produces whitespace and EOL")
        void lineEmpty() {
            final var line = "   ";

            assertLexer(line,
                    new WhitespaceToken(pos(0, 0), line),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Comment line")
        void lineComment() {
            final var line = "   ; This is a comment";

            assertLexer(line,
                    new WhitespaceToken(pos(0, 0), "   "),
                    new CommentToken(pos(0, 3), "; This is a comment"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Label definition")
        void lineLabel() {
            final var line = "  name:  ";

            assertLexer(line,
                    new WhitespaceToken(pos(0, 0), "  "),
                    new LabelDefinitionToken(pos(0, 2), "name:", "name"),
                    new WhitespaceToken(pos(0, 7), "  "),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".data directive")
        void lineDirective() {
            final var line = "   .data";

            assertLexer(line,
                    new WhitespaceToken(pos(0, 0), "   "),
                    new DirectiveToken(pos(0, 3), ".data", "data"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Unknown character produces UnknownToken")
        void lineUnknown() {
            final var line = "Hello, World!";

            assertLexer(line,
                    new LabelReferenceToken(pos(0, 0), "Hello", "hello"),
                    new CommaToken(pos(0, 5)),
                    new WhitespaceToken(pos(0, 6), " "),
                    new LabelReferenceToken(pos(0, 7), "World", "world"),
                    new UnknownToken(pos(0, 12), "!"),
                    new EOLToken(pos(0, line.length())));
        }
    }

    @Nested
    @DisplayName("Data directives")
    class DataDirectives {

        @Test
        @DisplayName(".byte data declaration")
        void lineByteData() {
            final var line = "name: .byte 42";

            assertLexer(line,
                    new LabelDefinitionToken(pos(0, 0), "name:", "name"),
                    new WhitespaceToken(pos(0, 5), " "),
                    new DirectiveToken(pos(0, 6), ".byte", "byte"),
                    new WhitespaceToken(pos(0, 11), " "),
                    new IntLiteralToken(pos(0, 12), "42", 42),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".half data declaration with negative value")
        void lineHalfData() {
            final var line = "name: .half -1000";

            assertLexer(line,
                    new LabelDefinitionToken(pos(0, 0), "name:", "name"),
                    new WhitespaceToken(pos(0, 5), " "),
                    new DirectiveToken(pos(0, 6), ".half", "half"),
                    new WhitespaceToken(pos(0, 11), " "),
                    new IntLiteralToken(pos(0, 12), "-1000", -1000),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".word data declaration with hex value")
        void lineWordData() {
            final var line = "name: .word 0xBABE123";

            assertLexer(line,
                    new LabelDefinitionToken(pos(0, 0), "name:", "name"),
                    new WhitespaceToken(pos(0, 5), " "),
                    new DirectiveToken(pos(0, 6), ".word", "word"),
                    new WhitespaceToken(pos(0, 11), " "),
                    new IntLiteralToken(pos(0, 12), "0xBABE123", 0xBABE123),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".space directive")
        void lineSpaceData() {
            final var line = ".space 100";

            assertLexer(line,
                    new DirectiveToken(pos(0, 0), ".space", "space"),
                    new WhitespaceToken(pos(0, 6), " "),
                    new IntLiteralToken(pos(0, 7), "100", 100),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".ascii directive")
        void lineAsciiData() {
            final var line = ".ascii \"Hello, World!\"";

            assertLexer(line,
                    new DirectiveToken(pos(0, 0), ".ascii", "ascii"),
                    new WhitespaceToken(pos(0, 6), " "),
                    new StringLiteralToken(pos(0, 7), "\"Hello, World!\"", "Hello, World!"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".asciiz directive")
        void lineAsciizData() {
            final var line = ".asciiz \"Hello, World!\"";

            assertLexer(line,
                    new DirectiveToken(pos(0, 0), ".asciiz", "asciiz"),
                    new WhitespaceToken(pos(0, 7), " "),
                    new StringLiteralToken(pos(0, 8), "\"Hello, World!\"", "Hello, World!"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName(".align directive")
        void lineAlign() {
            final var line = ".align 4";

            assertLexer(line,
                    new DirectiveToken(pos(0, 0), ".align", "align"),
                    new WhitespaceToken(pos(0, 6), " "),
                    new IntLiteralToken(pos(0, 7), "4", 4),
                    new EOLToken(pos(0, line.length())));
        }
    }

    @Nested
    @DisplayName("Instructions")
    class Instructions {

        @Test
        @DisplayName("Instruction with label and trailing comment")
        void lineRegisterInstruction() {
            final var line = "name: ADDI r1, r2, r3 ; add registers";

            assertLexer(line,
                    new LabelDefinitionToken(pos(0, 0), "name:", "name"),
                    new WhitespaceToken(pos(0, 5), " "),
                    new InstructionToken(pos(0, 6), "ADDI", "addi"),
                    new WhitespaceToken(pos(0, 10), " "),
                    new RegisterToken(pos(0, 11), "r1", 1),
                    new CommaToken(pos(0, 13)),
                    new WhitespaceToken(pos(0, 14), " "),
                    new RegisterToken(pos(0, 15), "r2", 2),
                    new CommaToken(pos(0, 17)),
                    new WhitespaceToken(pos(0, 18), " "),
                    new RegisterToken(pos(0, 19), "r3", 3),
                    new WhitespaceToken(pos(0, 21), " "),
                    new CommentToken(pos(0, 22), "; add registers"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Load with label memory operand")
        void lineImmediateInstructionLabel() {
            final var line = "lw R2, val(R0)";

            assertLexer(line,
                    new InstructionToken(pos(0, 0), "lw", "lw"),
                    new WhitespaceToken(pos(0, 2), " "),
                    new RegisterToken(pos(0, 3), "R2", 2),
                    new CommaToken(pos(0, 5)),
                    new WhitespaceToken(pos(0, 6), " "),
                    new LabelReferenceToken(pos(0, 7), "val", "val"),
                    new LeftParenToken(pos(0, 10)),
                    new RegisterToken(pos(0, 11), "R0", 0),
                    new RightParenToken(pos(0, 13)),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Load with integer memory operand")
        void lineImmediateInstructionAbsolute() {
            final var line = "lw R2, 100(R0)";

            assertLexer(line,
                    new InstructionToken(pos(0, 0), "lw", "lw"),
                    new WhitespaceToken(pos(0, 2), " "),
                    new RegisterToken(pos(0, 3), "R2", 2),
                    new CommaToken(pos(0, 5)),
                    new WhitespaceToken(pos(0, 6), " "),
                    new IntLiteralToken(pos(0, 7), "100", 100),
                    new LeftParenToken(pos(0, 10)),
                    new RegisterToken(pos(0, 11), "R0", 0),
                    new RightParenToken(pos(0, 13)),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Jump to label")
        void lineJumpInstructionLabel() {
            final var line = "j loop";

            assertLexer(line,
                    new InstructionToken(pos(0, 0), "j", "j"),
                    new WhitespaceToken(pos(0, 1), " "),
                    new LabelReferenceToken(pos(0, 2), "loop", "loop"),
                    new EOLToken(pos(0, line.length())));
        }

        @Test
        @DisplayName("Jump to register")
        void lineJumpInstructionRegister() {
            final var line = "j r31";

            assertLexer(line,
                    new InstructionToken(pos(0, 0), "j", "j"),
                    new WhitespaceToken(pos(0, 1), " "),
                    new RegisterToken(pos(0, 2), "r31", 31),
                    new EOLToken(pos(0, line.length())));
        }
    }

    @Nested
    @DisplayName("Complete programs")
    class Programs {

        @Test
        @DisplayName("Simple program")
        void simpleProgram() {
            final var program = """
                    ; My program
                    .data
                    op: .word 42
                    .text
                    main:
                        lw r1, op(r0)
                        addi r2, r1, 10
                        sw r2, op(r0)
                        halt""";

            assertLexer(program,
                    new CommentToken(pos(0, 0), "; My program"),
                    new EOLToken(pos(0, 12)),

                    new DirectiveToken(pos(1, 0), ".data", "data"),
                    new EOLToken(pos(1, 5)),

                    new LabelDefinitionToken(pos(2, 0), "op:", "op"),
                    new WhitespaceToken(pos(2, 3), " "),
                    new DirectiveToken(pos(2, 4), ".word", "word"),
                    new WhitespaceToken(pos(2, 9), " "),
                    new IntLiteralToken(pos(2, 10), "42", 42),
                    new EOLToken(pos(2, 12)),

                    new DirectiveToken(pos(3, 0), ".text", "text"),
                    new EOLToken(pos(3, 5)),

                    new LabelDefinitionToken(pos(4, 0), "main:", "main"),
                    new EOLToken(pos(4, 5)),

                    new WhitespaceToken(pos(5, 0), "    "),
                    new InstructionToken(pos(5, 4), "lw", "lw"),
                    new WhitespaceToken(pos(5, 6), " "),
                    new RegisterToken(pos(5, 7), "r1", 1),
                    new CommaToken(pos(5, 9)),
                    new WhitespaceToken(pos(5, 10), " "),
                    new LabelReferenceToken(pos(5, 11), "op", "op"),
                    new LeftParenToken(pos(5, 13)),
                    new RegisterToken(pos(5, 14), "r0", 0),
                    new RightParenToken(pos(5, 16)),
                    new EOLToken(pos(5, 17)),

                    new WhitespaceToken(pos(6, 0), "    "),
                    new InstructionToken(pos(6, 4), "addi", "addi"),
                    new WhitespaceToken(pos(6, 8), " "),
                    new RegisterToken(pos(6, 9), "r2", 2),
                    new CommaToken(pos(6, 11)),
                    new WhitespaceToken(pos(6, 12), " "),
                    new RegisterToken(pos(6, 13), "r1", 1),
                    new CommaToken(pos(6, 15)),
                    new WhitespaceToken(pos(6, 16), " "),
                    new IntLiteralToken(pos(6, 17), "10", 10),
                    new EOLToken(pos(6, 19)),

                    new WhitespaceToken(pos(7, 0), "    "),
                    new InstructionToken(pos(7, 4), "sw", "sw"),
                    new WhitespaceToken(pos(7, 6), " "),
                    new RegisterToken(pos(7, 7), "r2", 2),
                    new CommaToken(pos(7, 9)),
                    new WhitespaceToken(pos(7, 10), " "),
                    new LabelReferenceToken(pos(7, 11), "op", "op"),
                    new LeftParenToken(pos(7, 13)),
                    new RegisterToken(pos(7, 14), "r0", 0),
                    new RightParenToken(pos(7, 16)),
                    new EOLToken(pos(7, 17)),

                    new WhitespaceToken(pos(8, 0), "    "),
                    new InstructionToken(pos(8, 4), "halt", "halt"),
                    new EOLToken(pos(8, 8)));
        }
    }

    private static TextPosition pos(final int line, final int col) {
        return new TextPosition(line, col);
    }

    private static void assertLexer(final String source, final Token... expected) {
        final var tokens = List.of(expected);

        // first, use the highlighting lexer, that uses all tokens
        final var highlightingLexer = new Lexer(LexerMode.HIGHLIGHTING);
        final var highlightingTokens = highlightingLexer.tokenize(UUID.randomUUID(), source).tokens();

        assertIterableEquals(tokens, highlightingTokens);

        // second, use the assembler lexer, that uses only assembler tokens
        final var assemblerLexer = new Lexer(LexerMode.ASSEMBLER);
        final var assemblerTokens = assemblerLexer.tokenize(UUID.randomUUID(), source).tokens();

        // filter out non-assembler tokens from the expected list
        final var filteredTokens = tokens.stream()
                .filter(token -> !(token instanceof WhitespaceToken))
                .filter(token -> !(token instanceof CommentToken))
                .toList();

        assertIterableEquals(filteredTokens, assemblerTokens);
    }

}
