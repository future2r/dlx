package name.ulbricht.dlx.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.asm.compiler.Compiler;
import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.asm.parser.Parser;
import name.ulbricht.dlx.simulator.CPU;

@DisplayName("Trap handler")
final class TrapHandlerTest {

    @Nested
    @DisplayName("CPU integration")
    class CpuIntegration {

        @Test
        @DisplayName("trap 1 prints an integer")
        void printInteger() throws InterruptedException {
            final var result = runProgram("""
                    .text
                    addi r1, r0, 42
                    trap 1
                    trap 0""");

            assertTrue(result.cpu().isHalted());
            assertEquals("42", result.console().snapshot());
        }

        @Test
        @DisplayName("trap 2 prints a character")
        void printCharacter() throws InterruptedException {
            final var result = runProgram("""
                    .text
                    addi r1, r0, 65
                    trap 2
                    trap 0""");

            assertTrue(result.cpu().isHalted());
            assertEquals("A", result.console().snapshot());
        }

        @Test
        @DisplayName("trap 3 prints a null-terminated string")
        void printString() throws InterruptedException {
            final var result = runProgram("""
                    .data
                    .word 0
                    msg: .asciiz "Hello"
                    .text
                    addi r1, r0, msg
                    trap 3
                    trap 0""");

            assertTrue(result.cpu().isHalted());
            assertEquals("Hello", result.console().snapshot());
        }

        @Test
        @DisplayName("trap 0 halts without writing to the console")
        void haltOnly() throws InterruptedException {
            final var result = runProgram("""
                    .text
                    trap 0""");

            assertTrue(result.cpu().isHalted());
            assertEquals("", result.console().snapshot());
        }
    }

    private static ExecutionResult runProgram(final String source) throws InterruptedException {
        final var compiled = compile(source);
        final var console = new Console();
        final var cpu = new CPU();
        cpu.addTrapListener(new TrapHandler(console));
        cpu.loadProgram(compiled.program(), compiled.entryPoint());
        cpu.run();
        return new ExecutionResult(cpu, console);
    }

    private static CompiledProgram compile(final String source) {
        final var compiled = compileWithErrors(source);
        assertFalse(compiled.hasErrors(), "Expected no errors but got: " + compiled.diagnostics());
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

    private record ExecutionResult(CPU cpu, Console console) {
    }
}