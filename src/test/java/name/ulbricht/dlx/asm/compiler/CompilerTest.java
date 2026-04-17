package name.ulbricht.dlx.asm.compiler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.asm.parser.Parser;
import name.ulbricht.dlx.simulator.CPU;

@DisplayName("Compiler")
final class CompilerTest {

        @Nested
        @DisplayName("Data section")
        class DataSection {

                @Test
                @DisplayName(".word emits 4 bytes big-endian")
                void wordEncoding() {
                        final var compiled = compile("""
                                        .data
                                        .word 10""");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 0, 0, 0, 10 }, compiled.program());
                        assertEquals(4, compiled.entryPoint());
                }

                @Test
                @DisplayName(".word with multiple values")
                void wordMultiple() {
                        final var compiled = compile("""
                                        .data
                                        .word 10, 32""");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 0, 0, 0, 10, 0, 0, 0, 32 }, compiled.program());
                }

                @Test
                @DisplayName(".half emits 2 bytes big-endian")
                void halfEncoding() {
                        final var compiled = compile("""
                                        .data
                                        .half 0x1234""");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 0x12, 0x34 }, compiled.program());
                }

                @Test
                @DisplayName(".byte emits 1 byte")
                void byteEncoding() {
                        final var compiled = compile("""
                                        .data
                                        .byte 42""");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 42 }, compiled.program());
                }

                @Test
                @DisplayName(".ascii emits string bytes without terminator")
                void asciiEncoding() {
                        final var compiled = compile(".data\n.ascii \"Hi\"");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 'H', 'i' }, compiled.program());
                }

                @Test
                @DisplayName(".asciiz emits string bytes with null terminator")
                void asciizEncoding() {
                        final var compiled = compile(".data\n.asciiz \"Hi\"");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 'H', 'i', 0 }, compiled.program());
                }

                @Test
                @DisplayName(".space emits zero bytes")
                void spaceEncoding() {
                        final var compiled = compile("""
                                        .data
                                        .space 4""");
                        assertNoErrors(compiled);
                        assertArrayEquals(new byte[] { 0, 0, 0, 0 }, compiled.program());
                }

                @Test
                @DisplayName(".align pads to boundary")
                void alignEncoding() {
                        final var compiled = compile("""
                                        .data
                                        .byte 1
                                        .align 2
                                        .word 42""");
                        assertNoErrors(compiled);
                        // 1 byte + 3 padding + 4 word = 8 bytes
                        assertEquals(8, compiled.program().length);
                        assertEquals(1, compiled.program()[0]);
                        // bytes 1-3 are padding (zero)
                        assertEquals(0, compiled.program()[1]);
                        assertEquals(0, compiled.program()[2]);
                        assertEquals(0, compiled.program()[3]);
                        // word 42 at offset 4
                        assertEquals(0, compiled.program()[4]);
                        assertEquals(0, compiled.program()[5]);
                        assertEquals(0, compiled.program()[6]);
                        assertEquals(42, compiled.program()[7]);
                }

                @Test
                @DisplayName(".align 0 is a no-op")
                void alignZero() {
                        final var compiled = compile("""
                                        .data
                                        .byte 1
                                        .align 0
                                        .byte 2""");
                        assertNoErrors(compiled);
                        assertEquals(2, compiled.program().length);
                        assertEquals(1, compiled.program()[0]);
                        assertEquals(2, compiled.program()[1]);
                }

                @Test
                @DisplayName(".align 1 pads to halfword boundary")
                void alignOne() {
                        final var compiled = compile("""
                                        .data
                                        .byte 1
                                        .align 1
                                        .byte 2""");
                        assertNoErrors(compiled);
                        // 1 byte + 1 padding + 1 byte = 3 bytes
                        assertEquals(3, compiled.program().length);
                        assertEquals(1, compiled.program()[0]);
                        assertEquals(0, compiled.program()[1]); // padding
                        assertEquals(2, compiled.program()[2]);
                }

                @Test
                @DisplayName(".align 3 pads to doubleword boundary")
                void alignThree() {
                        final var compiled = compile("""
                                        .data
                                        .byte 1
                                        .align 3
                                        .byte 2""");
                        assertNoErrors(compiled);
                        // 1 byte + 7 padding + 1 byte = 9 bytes
                        assertEquals(9, compiled.program().length);
                        assertEquals(1, compiled.program()[0]);
                        assertEquals(2, compiled.program()[8]);
                }

                @Test
                @DisplayName("Data labels resolve to correct addresses")
                void dataLabels() {
                        // a: .word 10 (addr 0)
                        // b: .word 20 (addr 4)
                        // lw r1, a(r0) should encode offset=0
                        // lw r2, b(r0) should encode offset=4
                        final var compiled = compile("""
                                        .data
                                        a: .word 10
                                        b: .word 20
                                        .text
                                        lw r1, a(r0)
                                        lw r2, b(r0)
                                        trap 0""");
                        assertNoErrors(compiled);
                        assertEquals(8, compiled.entryPoint());
                        // Check instruction at offset 8: lw r1, 0(r0)
                        // opcode LW=0x23, rs1=0, rd=1, imm=0
                        final var word0 = readWord(compiled.program(), 8);
                        assertEquals(0x8C010000, word0); // (0x23<<26)|(0<<21)|(1<<16)|0
                        // Check instruction at offset 12: lw r2, 4(r0)
                        final var word1 = readWord(compiled.program(), 12);
                        assertEquals(0x8C020004, word1); // (0x23<<26)|(0<<21)|(2<<16)|4
                }
        }

        @Nested
        @DisplayName("Instruction encoding")
        class InstructionEncoding {

                @Test
                @DisplayName("R-format: add r3, r1, r2")
                void rFormatAdd() {
                        final var compiled = compile("""
                                        .text
                                        add r3, r1, r2""");
                        assertNoErrors(compiled);
                        // SPECIAL(0)<<26 | rs1=1<<21 | rs2=2<<16 | rd=3<<11 | ADD=0x20
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x00221820, word);
                }

                @Test
                @DisplayName("R-format: sll r5, r3, r2")
                void rFormatSll() {
                        final var compiled = compile("""
                                        .text
                                        sll r5, r3, r2""");
                        assertNoErrors(compiled);
                        // SPECIAL<<26 | rs1=3<<21 | rs2=2<<16 | rd=5<<11 | SLL=0x04
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x00622804, word);
                }

                @Test
                @DisplayName("I-format: addi r1, r0, 10")
                void iFormatAddi() {
                        final var compiled = compile("""
                                        .text
                                        addi r1, r0, 10""");
                        assertNoErrors(compiled);
                        // ADDI=0x08<<26 | rs1=0<<21 | rd=1<<16 | imm=10
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x2001000A, word);
                }

                @Test
                @DisplayName("I-format: addi with negative immediate")
                void iFormatAddiNegative() {
                        final var compiled = compile("""
                                        .text
                                        addi r1, r0, -1""");
                        assertNoErrors(compiled);
                        // ADDI=0x08<<26 | rs1=0<<21 | rd=1<<16 | imm=0xFFFF
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x2001FFFF, word);
                }

                @Test
                @DisplayName("I-format load: lw r1, 8(r3)")
                void iFormatLoad() {
                        final var compiled = compile("""
                                        .text
                                        lw r1, 8(r3)""");
                        assertNoErrors(compiled);
                        // LW=0x23<<26 | rs1=3<<21 | rd=1<<16 | imm=8
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x8C610008, word);
                }

                @Test
                @DisplayName("I-format store: sw 8(r0), r2")
                void iFormatStore() {
                        final var compiled = compile("""
                                        .text
                                        sw 8(r0), r2""");
                        assertNoErrors(compiled);
                        // SW=0x2B<<26 | rs1=0<<21 | rd=2<<16 | imm=8
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0xAC020008, word);
                }

                @Test
                @DisplayName("I-format branch: beqz with forward label")
                void iFormatBranch() {
                        final var compiled = compile("""
                                        .text
                                        beqz r1, end
                                        addi r2, r0, 1
                                        end: trap 0""");
                        assertNoErrors(compiled);
                        // beqz at addr 0, end at addr 8: offset = 8 - (0+4) = 4
                        // BEQZ=0x04<<26 | rs1=1<<21 | rd=0<<16 | imm=4
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x10200004, word);
                }

                @Test
                @DisplayName("I-format branch: bnez with backward label")
                void iFormatBranchBackward() {
                        final var compiled = compile("""
                                        .text
                                        loop: addi r1, r1, -1
                                        bnez r1, loop""");
                        assertNoErrors(compiled);
                        // bnez at addr 4, loop at addr 0: offset = 0 - (4+4) = -8 = 0xFFF8
                        final var word = readWord(compiled.program(), 4);
                        assertEquals(0x1420FFF8, word);
                }

                @Test
                @DisplayName("I-format: jr r31")
                void iFormatJr() {
                        final var compiled = compile("""
                                        .text
                                        jr r31""");
                        assertNoErrors(compiled);
                        // JR=0x10<<26 | rs1=31<<21 | 0
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x43E00000, word);
                }

                @Test
                @DisplayName("I-format: lhi r1, 0x1234")
                void iFormatLhi() {
                        final var compiled = compile("""
                                        .text
                                        lhi r1, 0x1234""");
                        assertNoErrors(compiled);
                        // LHI=0x0F<<26 | rs1=0<<21 | rd=1<<16 | imm=0x1234
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x3C011234, word);
                }

                @Test
                @DisplayName("trap 0 encodes to 0xFC000000")
                void trapEncoding() {
                        final var compiled = compile("""
                                        .text
                                        trap 0""");
                        assertNoErrors(compiled);
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0xFC000000, word);
                }

                @Test
                @DisplayName("J-format: j with forward label")
                void jFormatJump() {
                        final var compiled = compile("""
                                        .text
                                        j end
                                        addi r1, r0, 1
                                        end: trap 0""");
                        assertNoErrors(compiled);
                        // j at addr 0, end at addr 8: distance = 8 - (0+4) = 4
                        // J=0x02<<26 | dist=4
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x08000004, word);
                }

                @Test
                @DisplayName("J-format: jal with label")
                void jFormatJal() {
                        final var compiled = compile("""
                                        .text
                                        jal func
                                        trap 0
                                        func: jr r31""");
                        assertNoErrors(compiled);
                        // jal at addr 0, func at addr 8: distance = 8 - (0+4) = 4
                        // JAL=0x03<<26 | dist=4
                        final var word = readWord(compiled.program(), 0);
                        assertEquals(0x0C000004, word);
                }
        }

        @Nested
        @DisplayName("Error handling")
        class ErrorHandling {

                @Test
                @DisplayName("Duplicate label produces error")
                void duplicateLabel() {
                        final var compiled = compileWithErrors("""
                                        .data
                                        x: .word 1
                                        x: .word 2""");
                        assertTrue(compiled.hasErrors());
                        assertEquals(0, compiled.program().length);
                        assertTrue(compiled.diagnostics().stream()
                                        .anyMatch(d -> d.message().contains("Duplicate label")));
                }

                @Test
                @DisplayName("Undefined label produces error")
                void undefinedLabel() {
                        final var compiled = compileWithErrors("""
                                        .text
                                        j nowhere""");
                        assertTrue(compiled.hasErrors());
                        assertEquals(0, compiled.program().length);
                        assertTrue(compiled.diagnostics().stream()
                                        .anyMatch(d -> d.message().contains("Undefined label")));
                }

                @Test
                @DisplayName("Negative alignment exponent produces error")
                void alignNegative() {
                        final var compiled = compileWithErrors("""
                                        .data
                                        .align -1""");
                        assertTrue(compiled.hasErrors());
                        assertEquals(0, compiled.program().length);
                        assertTrue(compiled.diagnostics().stream()
                                        .anyMatch(d -> d.message()
                                                        .contains("Alignment exponent must be between 0 and 8")));
                }

                @Test
                @DisplayName("Alignment exponent too large produces error")
                void alignTooLarge() {
                        final var compiled = compileWithErrors("""
                                        .data
                                        .align 9""");
                        assertTrue(compiled.hasErrors());
                        assertEquals(0, compiled.program().length);
                        assertTrue(compiled.diagnostics().stream()
                                        .anyMatch(d -> d.message()
                                                        .contains("Alignment exponent must be between 0 and 8")));
                }

                @Test
                @DisplayName("Wrong operand count is caught by the parser")
                void wrongOperandCount() {
                        // 'add r1, r2' is rejected by the parser (missing third operand),
                        // so the instruction never reaches the compiler. Verify that the
                        // parser produces diagnostics for this case.
                        final var parsed = parse("""
                                        .text
                                        add r1, r2""");
                        assertFalse(parsed.diagnostics().isEmpty());
                }
        }

        @Nested
        @DisplayName("Integration")
        class Integration {

                @Test
                @DisplayName("Example program compiles and runs correctly")
                void exampleProgram() throws InterruptedException {
                        final var compiled = compile("""
                                        .data
                                        a:   .word 10
                                        b:   .word 32
                                        res: .word 0
                                        .text
                                        lw r1, a(r0)
                                        lw r2, b(r0)
                                        add r3, r1, r2
                                        sw res(r0), r3
                                        trap 0""");
                        assertNoErrors(compiled);

                        // Verify structure
                        assertEquals(12, compiled.entryPoint());
                        assertEquals(32, compiled.program().length);

                        // Verify code section encoding
                        assertEquals(0x8C010000, readWord(compiled.program(), 12)); // lw r1, 0(r0)
                        assertEquals(0x8C020004, readWord(compiled.program(), 16)); // lw r2, 4(r0)
                        assertEquals(0x00221820, readWord(compiled.program(), 20)); // add r3, r1, r2
                        assertEquals(0xAC030008, readWord(compiled.program(), 24)); // sw 8(r0), r3
                        assertEquals(0xFC000000, readWord(compiled.program(), 28)); // trap 0

                        // Run on CPU and verify results
                        final var cpu = new CPU();
                        cpu.loadProgram(compiled.program(), compiled.entryPoint());
                        cpu.run();

                        assertTrue(cpu.isHalted());
                        assertEquals(10, cpu.getRegisters().read(1));
                        assertEquals(32, cpu.getRegisters().read(2));
                        assertEquals(42, cpu.getRegisters().read(3));
                }

                @Test
                @DisplayName("Empty program compiles")
                void emptyProgram() {
                        final var compiled = compile("");
                        assertNoErrors(compiled);
                        assertEquals(0, compiled.program().length);
                }
        }

        // =====================================================================
        // Helpers
        // =====================================================================

        private static CompiledProgram compile(final String source) {
                final var compiled = compileWithErrors(source);
                assertFalse(compiled.hasErrors(),
                                "Expected no errors but got: " + compiled.diagnostics());
                return compiled;
        }

        private static CompiledProgram compileWithErrors(final String source) {
                final var parsed = parse(source);
                return new Compiler().compile(parsed);
        }

        private static ParsedProgram parse(final String source) {
                final var tokenized = new Lexer(LexerMode.ASSEMBLER).tokenize(UUID.randomUUID(), source);
                return new Parser().parse(tokenized);
        }

        private static int readWord(final byte[] buf, final int offset) {
                return ((buf[offset] & 0xFF) << 24)
                                | ((buf[offset + 1] & 0xFF) << 16)
                                | ((buf[offset + 2] & 0xFF) << 8)
                                | (buf[offset + 3] & 0xFF);
        }

        private static void assertNoErrors(final CompiledProgram compiled) {
                assertFalse(compiled.hasErrors(),
                                "Expected no errors but got: " + compiled.diagnostics());
        }
}
