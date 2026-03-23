package name.ulbricht.dlx.asm.lexer;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

final class LexerTest {

    @Test
    void lineEmpty() {
        final var line = "   ";

        assertLexer(line,
                new WhitespaceToken(1, 0, line),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineComment() {
        final var line = "   ; This is a comment";

        assertLexer(line,
                new WhitespaceToken(1, 0, "   "),
                new CommentToken(1, 3, "; This is a comment"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineLabel() {
        final var line = "  name:  ";

        assertLexer(line,
                new WhitespaceToken(1, 0, "  "),
                new LabelDefinitionToken(1, 2, "name:", "name"),
                new WhitespaceToken(1, 7, "  "),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineDirective() {
        final var line = "   .data";

        assertLexer(line,
                new WhitespaceToken(1, 0, "   "),
                new DirectiveToken(1, 3, ".data", "data"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineByteData() {
        final var line = "name: .byte 42";

        assertLexer(line,
                new LabelDefinitionToken(1, 0, "name:", "name"),
                new WhitespaceToken(1, 5, " "),
                new DirectiveToken(1, 6, ".byte", "byte"),
                new WhitespaceToken(1, 11, " "),
                new IntLiteralToken(1, 12, "42", 42),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineHalfData() {
        final var line = "name: .half -1000";

        assertLexer(line,
                new LabelDefinitionToken(1, 0, "name:", "name"),
                new WhitespaceToken(1, 5, " "),
                new DirectiveToken(1, 6, ".half", "half"),
                new WhitespaceToken(1, 11, " "),
                new IntLiteralToken(1, 12, "-1000", -1000),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineWordData() {
        final var line = "name: .word 0xBABE123";

        assertLexer(line,
                new LabelDefinitionToken(1, 0, "name:", "name"),
                new WhitespaceToken(1, 5, " "),
                new DirectiveToken(1, 6, ".word", "word"),
                new WhitespaceToken(1, 11, " "),
                new IntLiteralToken(1, 12, "0xBABE123", 0xBABE123),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineSpaceData() {
        final var line = ".space 100";

        assertLexer(line,
                new DirectiveToken(1, 0, ".space", "space"),
                new WhitespaceToken(1, 6, " "),
                new IntLiteralToken(1, 7, "100", 100),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineAsciiData() {
        final var line = ".ascii \"Hello, World!\"";

        assertLexer(line,
                new DirectiveToken(1, 0, ".ascii", "ascii"),
                new WhitespaceToken(1, 6, " "),
                new StringLiteralToken(1, 7, "\"Hello, World!\"", "Hello, World!"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineAsciizData() {
        final var line = ".asciiz \"Hello, World!\"";

        assertLexer(line,
                new DirectiveToken(1, 0, ".asciiz", "asciiz"),
                new WhitespaceToken(1, 7, " "),
                new StringLiteralToken(1, 8, "\"Hello, World!\"", "Hello, World!"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineAlign() {
        final var line = ".align 4";

        assertLexer(line,
                new DirectiveToken(1, 0, ".align", "align"),
                new WhitespaceToken(1, 6, " "),
                new IntLiteralToken(1, 7, "4", 4),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineRegisterInstruction() {
        final var line = "name: ADDI r1, r2, r3 ; add registers";

        assertLexer(line,
                new LabelDefinitionToken(1, 0, "name:", "name"),
                new WhitespaceToken(1, 5, " "),
                new InstructionToken(1, 6, "ADDI", "addi"),
                new WhitespaceToken(1, 10, " "),
                new RegisterToken(1, 11, "r1", 1),
                new CommaToken(1, 13),
                new WhitespaceToken(1, 14, " "),
                new RegisterToken(1, 15, "r2", 2),
                new CommaToken(1, 17),
                new WhitespaceToken(1, 18, " "),
                new RegisterToken(1, 19, "r3", 3),
                new WhitespaceToken(1, 21, " "),
                new CommentToken(1, 22, "; add registers"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineImmediateInstructionLabel() {
        final var line = "lw R2, val(R0)";

        assertLexer(line,
                new InstructionToken(1, 0, "lw", "lw"),
                new WhitespaceToken(1, 2, " "),
                new RegisterToken(1, 3, "R2", 2),
                new CommaToken(1, 5),
                new WhitespaceToken(1, 6, " "),
                new LabelReferenceToken(1, 7, "val", "val"),
                new LeftParenToken(1, 10),
                new RegisterToken(1, 11, "R0", 0),
                new RightParenToken(1, 13),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineImmediateInstructionAbsolute() {
        final var line = "lw R2, 100(R0)";

        assertLexer(line,
                new InstructionToken(1, 0, "lw", "lw"),
                new WhitespaceToken(1, 2, " "),
                new RegisterToken(1, 3, "R2", 2),
                new CommaToken(1, 5),
                new WhitespaceToken(1, 6, " "),
                new IntLiteralToken(1, 7, "100", 100),
                new LeftParenToken(1, 10),
                new RegisterToken(1, 11, "R0", 0),
                new RightParenToken(1, 13),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineJumpInstructionLabel() {
        final var line = "j loop";

        assertLexer(line,
                new InstructionToken(1, 0, "j", "j"),
                new WhitespaceToken(1, 1, " "),
                new LabelReferenceToken(1, 2, "loop", "loop"),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineJumpInstructionRegister() {
        final var line = "j r31";

        assertLexer(line,
                new InstructionToken(1, 0, "j", "j"),
                new WhitespaceToken(1, 1, " "),
                new RegisterToken(1, 2, "r31", 31),
                new EOLToken(1, line.length()));
    }

    @Test
    void lineUnknown() {
        final var line = "Hello, World!";

        assertLexer(line,
                new LabelReferenceToken(1, 0, "Hello", "hello"),
                new CommaToken(1, 5),
                new WhitespaceToken(1, 6, " "),
                new LabelReferenceToken(1, 7, "World", "world"),
                new UnknownToken(1, 12, "!"),
                new EOLToken(1, line.length()));
    }

    @Test
    void simpleProgram() {
        final var program = """
                ; My program
                .data
                op: .word 42
                .text
                main:
                    lw r1, op
                    addi r2, r1, 10
                    sw r2, op
                    halt
                """;

        assertLexer(program,
                new CommentToken(1, 0, "; My program"),
                new EOLToken(1, 12),

                new DirectiveToken(2, 0, ".data", "data"),
                new EOLToken(2, 5),

                new LabelDefinitionToken(3, 0, "op:", "op"),
                new WhitespaceToken(3, 3, " "),
                new DirectiveToken(3, 4, ".word", "word"),
                new WhitespaceToken(3, 9, " "),
                new IntLiteralToken(3, 10, "42", 42),
                new EOLToken(3, 12),

                new DirectiveToken(4, 0, ".text", "text"),
                new EOLToken(4, 5),

                new LabelDefinitionToken(5, 0, "main:", "main"),
                new EOLToken(5, 5),

                new WhitespaceToken(6, 0, "    "),
                new InstructionToken(6, 4, "lw", "lw"),
                new WhitespaceToken(6, 6, " "),
                new RegisterToken(6, 7, "r1", 1),
                new CommaToken(6, 9),
                new WhitespaceToken(6, 10, " "),
                new LabelReferenceToken(6, 11, "op", "op"),
                new EOLToken(6, 13),

                new WhitespaceToken(7, 0, "    "),
                new InstructionToken(7, 4, "addi", "addi"),
                new WhitespaceToken(7, 8, " "),
                new RegisterToken(7, 9, "r2", 2),
                new CommaToken(7, 11),
                new WhitespaceToken(7, 12, " "),
                new RegisterToken(7, 13, "r1", 1),
                new CommaToken(7, 15),
                new WhitespaceToken(7, 16, " "),
                new IntLiteralToken(7, 17, "10", 10),
                new EOLToken(7, 19),

                new WhitespaceToken(8, 0, "    "),
                new InstructionToken(8, 4, "sw", "sw"),
                new WhitespaceToken(8, 6, " "),
                new RegisterToken(8, 7, "r2", 2),
                new CommaToken(8, 9),
                new WhitespaceToken(8, 10, " "),
                new LabelReferenceToken(8, 11, "op", "op"),
                new EOLToken(8, 13),

                new WhitespaceToken(9, 0, "    "),
                new InstructionToken(9, 4, "halt", "halt"),
                new EOLToken(9, 8));
    }

    private static void assertLexer(final String source, final Token... expected) {
        final var lines = List.of(source.split("\\R"));
        final var tokens = List.of(expected);

        // first, use the highlighting lexer, that uses all tokens
        final var highlightingLexer = new Lexer(LexerMode.HIGHLIGHTING);
        final var highlightingTokens = highlightingLexer.tokenize(lines);

        assertIterableEquals(tokens, highlightingTokens);

        // second, use the assembler lexer, that uses only assembler tokens
        final var assemblerLexer = new Lexer(LexerMode.ASSEMBLER);
        final var assemblerTokens = assemblerLexer.tokenize(lines);

        // filter out non-assembler tokens from the expected list
        final var filteredTokens = tokens.stream()
                .filter(token -> !(token instanceof WhitespaceToken))
                .filter(token -> !(token instanceof CommentToken))
                .toList();

        assertIterableEquals(filteredTokens, assemblerTokens);
    }

}
