package name.ulbricht.dlx.asm.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.lexer.TokenizedProgram;
import name.ulbricht.dlx.util.TextPosition;

@SuppressWarnings({ "static-method", "boxing" })
@DisplayName("Parser")
final class ParserTest {

        @Nested
        @DisplayName("Segments")
        class Segments {

                @Test
                @DisplayName("Empty source produces empty program")
                void emptySource() {
                        final var program = parse("");
                        assertTrue(program.data().isEmpty());
                        assertTrue(program.code().isEmpty());
                }

                @Test
                @DisplayName(".data segment switch")
                void segmentSwitchData() {
                        final var program = parse(".data");
                        assertTrue(program.data().isEmpty());
                        assertTrue(program.code().isEmpty());
                }

                @Test
                @DisplayName(".text segment switch")
                void segmentSwitchText() {
                        final var program = parse(".text");
                        assertTrue(program.data().isEmpty());
                        assertTrue(program.code().isEmpty());
                }

                @Test
                @DisplayName("Multiple sections are merged")
                void mergeMultipleSections() {
                        final var program = parse("""
                                        .data
                                        .word 1
                                        .text
                                        halt
                                        .data
                                        .word 2""");
                        assertIterableEquals(
                                        List.of(
                                                        new ParsedDataDeclaration(pos(0, 0), null, "word", List.of(1)),
                                                        new ParsedDataDeclaration(pos(0, 0), null, "word", List.of(2))),
                                        // note: positions differ — normalise to a fixed pos for comparison
                                        program.data().stream()
                                                        .map(d -> new ParsedDataDeclaration(pos(0, 0), null, d.directive(),
                                                                        d.values()))
                                                        .toList());
                        assertEquals(1, program.code().size());
                        assertEquals("halt", program.code().get(0).opcode());
                }
        }

        @Nested
        @DisplayName("Data directives")
        class DataDirectives {

                @Test
                @DisplayName(".word with single value")
                void wordSingle() {
                        final var program = parse("""
                                        .data
                                        .word 42""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "word", List.of(42))),
                                        program.data());
                        assertTrue(program.code().isEmpty());
                }

                @Test
                @DisplayName(".word with multiple values")
                void wordMultiple() {
                        final var program = parse("""
                                        .data
                                        .word 1, 2, 3""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "word", List.of(1, 2, 3))),
                                        program.data());
                }

                @Test
                @DisplayName(".word with hex value")
                void wordHex() {
                        final var program = parse("""
                                        .data
                                        .word 0xBABE123""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "word", List.of(0xBABE123))),
                                        program.data());
                }

                @Test
                @DisplayName(".half with negative value")
                void halfNegative() {
                        final var program = parse("""
                                        .data
                                        .half -1000""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "half", List.of(-1000))),
                                        program.data());
                }

                @Test
                @DisplayName(".byte directive")
                void byteValue() {
                        final var program = parse("""
                                        .data
                                        .byte 42""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "byte", List.of(42))),
                                        program.data());
                }

                @Test
                @DisplayName(".ascii directive")
                void asciiDirective() {
                        final var program = parse("""
                                        .data
                                        .ascii "Hello, World!" \s""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "ascii", List.of("Hello, World!"))),
                                        program.data());
                }

                @Test
                @DisplayName(".asciiz directive")
                void asciizDirective() {
                        final var program = parse("""
                                        .data
                                        .asciiz "hi" \s""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "asciiz", List.of("hi"))),
                                        program.data());
                }

                @Test
                @DisplayName(".space directive")
                void spaceDirective() {
                        final var program = parse("""
                                        .data
                                        .space 100""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "space", List.of(100))),
                                        program.data());
                }

                @Test
                @DisplayName(".align directive")
                void alignDirective() {
                        final var program = parse("""
                                        .data
                                        .align 4""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 0), null, "align", List.of(4))),
                                        program.data());
                }

                @Test
                @DisplayName("Label on data declaration")
                void labelOnData() {
                        final var program = parse("""
                                        .data
                                        op: .word 0""");
                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(1, 4), "op", "word", List.of(0))),
                                        program.data());
                }
        }

        @Nested
        @DisplayName("Labels")
        class Labels {

                @Test
                @DisplayName("Label-only line floats to next")
                void labelFloatsToNextLine() {
                        final var program = parse("""
                                        .text
                                        main:
                                        halt""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(2, 0), "main", "halt", List.of())),
                                        program.code());
                }

                @Test
                @DisplayName("Label on instruction line overrides pending label")
                void labelOnSameLineWins() {
                        final var program = parse("""
                                        .text
                                        foo:
                                        bar: halt""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(2, 5), "bar", "halt", List.of())),
                                        program.code());
                }
        }

        @Nested
        @DisplayName("Instructions")
        class Instructions {

                @Test
                @DisplayName("halt instruction — no operands")
                void haltInstruction() {
                        final var program = parse("""
                                        .text
                                        halt""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "halt", List.of())),
                                        program.code());
                }

                @Test
                @DisplayName("add — R-format Rd, Rs1, Rs2")
                void addInstruction() {
                        final var program = parse("""
                                        .text
                                        add r3, r1, r2""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "add",
                                                        List.of(new RegisterOperand(3), new RegisterOperand(1),
                                                                        new RegisterOperand(2)))),
                                        program.code());
                }

                @Test
                @DisplayName("sub — R-format Rd, Rs1, Rs2")
                void subInstruction() {
                        final var program = parse("""
                                        .text
                                        sub r3, r1, r2""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "sub",
                                                        List.of(new RegisterOperand(3), new RegisterOperand(1),
                                                                        new RegisterOperand(2)))),
                                        program.code());
                }

                @Test
                @DisplayName("addi — I-format Rd, Rs1, Imm")
                void addiInstruction() {
                        final var program = parse("""
                                        .text
                                        addi r2, r1, 10""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "addi",
                                                        List.of(new RegisterOperand(2), new RegisterOperand(1),
                                                                        new ImmediateOperand(10)))),
                                        program.code());
                }

                @Test
                @DisplayName("addi with label immediate — address load")
                void addiLabelImmediate() {
                        final var program = parse("""
                                        .text
                                        addi r1, r0, str""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "addi",
                                                        List.of(new RegisterOperand(1), new RegisterOperand(0),
                                                                        new LabelImmediateOperand("str")))),
                                        program.code());
                }

                @Test
                @DisplayName("addi with negative immediate")
                void addiNegativeImmediate() {
                        final var program = parse("""
                                        .text
                                        addi r2, r1, -5""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "addi",
                                                        List.of(new RegisterOperand(2), new RegisterOperand(1),
                                                                        new ImmediateOperand(-5)))),
                                        program.code());
                }

                @Test
                @DisplayName("lw with integer offset")
                void lwWithIntOffset() {
                        final var program = parse("""
                                        .text
                                        lw r2, 100(r0)""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "lw",
                                                        List.of(new RegisterOperand(2), new MemoryOperand(100, 0)))),
                                        program.code());
                }

                @Test
                @DisplayName("lw with label offset")
                void lwWithLabelOffset() {
                        final var program = parse("""
                                        .text
                                        lw r1, op(r0)""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "lw",
                                                        List.of(new RegisterOperand(1), new LabelMemoryOperand("op", 0)))),
                                        program.code());
                }

                @Test
                @DisplayName("lw with non-zero base register")
                void lwWithNonZeroBase() {
                        final var program = parse("""
                                        .text
                                        lw r1, 8(r3)""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "lw",
                                                        List.of(new RegisterOperand(1), new MemoryOperand(8, 3)))),
                                        program.code());
                }

                @Test
                @DisplayName("sw with integer offset")
                void swWithIntOffset() {
                        final var program = parse("""
                                        .text
                                        sw 8(r0), r2""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "sw",
                                                        List.of(new MemoryOperand(8, 0), new RegisterOperand(2)))),
                                        program.code());
                }

                @Test
                @DisplayName("sw with label offset")
                void swWithLabelOffset() {
                        final var program = parse("""
                                        .text
                                        sw op(r0), r2""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "sw",
                                                        List.of(new LabelMemoryOperand("op", 0), new RegisterOperand(2)))),
                                        program.code());
                }

                @Test
                @DisplayName("lhi — Rd, Imm")
                void lhiInstruction() {
                        final var program = parse("""
                                        .text
                                        lhi r1, 0x1234""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "lhi",
                                                        List.of(new RegisterOperand(1), new ImmediateOperand(0x1234)))),
                                        program.code());
                }

                @Test
                @DisplayName("lhi with label immediate — upper address half")
                void lhiLabelImmediate() {
                        final var program = parse("""
                                        .text
                                        lhi r1, str""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "lhi",
                                                        List.of(new RegisterOperand(1), new LabelImmediateOperand("str")))),
                                        program.code());
                }

                @Test
                @DisplayName("ori with label immediate — lower address half")
                void oriLabelImmediate() {
                        final var program = parse("""
                                        .text
                                        ori r1, r1, str""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "ori",
                                                        List.of(new RegisterOperand(1), new RegisterOperand(1),
                                                                        new LabelImmediateOperand("str")))),
                                        program.code());
                }

                @Test
                @DisplayName("beqz — Rs, Label")
                void beqzInstruction() {
                        final var program = parse("""
                                        .text
                                        beqz r1, loop""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "beqz",
                                                        List.of(new RegisterOperand(1), new LabelOperand("loop")))),
                                        program.code());
                }

                @Test
                @DisplayName("bnez — Rs, Label")
                void bnezInstruction() {
                        final var program = parse("""
                                        .text
                                        bnez r2, end""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "bnez",
                                                        List.of(new RegisterOperand(2), new LabelOperand("end")))),
                                        program.code());
                }

                @Test
                @DisplayName("j — Label")
                void jInstruction() {
                        final var program = parse("""
                                        .text
                                        j loop""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "j",
                                                        List.of(new LabelOperand("loop")))),
                                        program.code());
                }

                @Test
                @DisplayName("jal — Label")
                void jalInstruction() {
                        final var program = parse("""
                                        .text
                                        jal func""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "jal",
                                                        List.of(new LabelOperand("func")))),
                                        program.code());
                }

                @Test
                @DisplayName("jr — Rs")
                void jrInstruction() {
                        final var program = parse("""
                                        .text
                                        jr r31""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "jr",
                                                        List.of(new RegisterOperand(31)))),
                                        program.code());
                }

                @Test
                @DisplayName("jalr — Rs")
                void jalrInstruction() {
                        final var program = parse("""
                                        .text
                                        jalr r1""");
                        assertIterableEquals(
                                        List.of(new ParsedInstruction(pos(1, 0), null, "jalr",
                                                        List.of(new RegisterOperand(1)))),
                                        program.code());
                }
        }

        @Nested
        @DisplayName("Complete programs")
        class Programs {

                @Test
                @DisplayName("Simple program")
                void simpleProgram() {
                        final var program = parse("""
                                        ; My program
                                        .data
                                        op: .word 42
                                        .text
                                        main:
                                            lw r1, op(r0)
                                            addi r2, r1, 10
                                            sw op(r0), r2
                                            halt""");

                        assertIterableEquals(
                                        List.of(new ParsedDataDeclaration(pos(2, 4), "op", "word", List.of(42))),
                                        program.data());

                        assertIterableEquals(
                                        List.of(
                                                        new ParsedInstruction(pos(5, 4), "main", "lw",
                                                                        List.of(new RegisterOperand(1),
                                                                                        new LabelMemoryOperand("op", 0))),
                                                        new ParsedInstruction(pos(6, 4), null, "addi",
                                                                        List.of(new RegisterOperand(2), new RegisterOperand(1),
                                                                                        new ImmediateOperand(10))),
                                                        new ParsedInstruction(pos(7, 4), null, "sw",
                                                                        List.of(new LabelMemoryOperand("op", 0),
                                                                                        new RegisterOperand(2))),
                                                        new ParsedInstruction(pos(8, 4), null, "halt", List.of())),
                                        program.code());
                }
        }

        @Nested
        @DisplayName("Error handling")
        class ErrorHandling {

                @Test
                @DisplayName("Unsupported directive produces error")
                void errorUnsupportedDirective() {
                        final var program = new Parser().parse(lex(".float 1.0"));

                        assertTrue(program.data().isEmpty());
                        assertTrue(program.code().isEmpty());
                        // parser error: ".float" not supported
                        assertEquals(1, program.diagnostics().size());
                }

                @Test
                @DisplayName("Error recovery continues on next line")
                void errorRecoveryAfterBadLine() {
                        final var program = new Parser().parse(lex("""
                                        .text
                                        42
                                        halt"""));

                        assertEquals(1, program.diagnostics().size());
                        assertEquals(1, program.code().size());
                        assertEquals("halt", program.code().get(0).opcode());
                }

                @Test
                @DisplayName("Missing operand produces error")
                void errorMissingOperand() {
                        final var program = new Parser().parse(lex("""
                                        .text
                                        add r1, r2"""));
                        assertEquals(1, program.diagnostics().size());
                }
        }

        private static TextPosition pos(final int line, final int col) {
                return new TextPosition(line, col);
        }

        private static ParsedProgram parse(final String source) {
                final var program = new Parser().parse(lex(source));
                assertTrue(program.diagnostics().isEmpty(),
                                "Expected no diagnostics but got: " + program.diagnostics());
                return program;
        }

        private static TokenizedProgram lex(final String source) {
                return new Lexer(LexerMode.ASSEMBLER).tokenize(UUID.randomUUID(), source);
        }
}
