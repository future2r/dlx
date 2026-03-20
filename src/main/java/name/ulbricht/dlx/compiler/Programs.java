package name.ulbricht.dlx.compiler;

import java.nio.charset.StandardCharsets;
import java.util.List;

import name.ulbricht.dlx.simulator.FunctionCode;
import name.ulbricht.dlx.simulator.ImmediateInstruction;
import name.ulbricht.dlx.simulator.OperationCode;
import name.ulbricht.dlx.simulator.RegisterInstruction;

/// Utility class for programs.
public final class Programs {

    /// {@returns an example program as a string}
    public static String createExampleSource() {
        return """
                .data
                op1: .word 10
                op2: .word 32
                res: .word 0
                b: .byte 42
                h: .halfword 4200
                buffer: .space 20
                msg: .ascii "Hello"
                name: .asciiz "World"

                .text
                main:
                    lw r1, op1(r0)
                    lw r2, op2(r0)
                    add r3, r1, r2
                    sw res(r0), r3
                    halt""";
    }

    /// {@returns an example program}
    public static Program createExampleProgram() {
        final var data = List.<DataDeclaration>of(
                new WordDataDeclaration("op1", 10),
                new WordDataDeclaration("op2", 32),
                new WordDataDeclaration("res", 0),
                new ByteDataDeclaration("b", (byte) 42),
                new HalfWordDataDeclaration("h", (short) 4200),
                new SpaceDataDeclaration("buffer", 20),
                new AsciiDataDeclaration("msg", "Hello".getBytes(StandardCharsets.US_ASCII)),
                new AsciiZDataDeclaration("name", "World".getBytes(StandardCharsets.US_ASCII)));

        final var code = List.of(
                // main : lw r1, op1(r0)
                new InstructionCall("main", new ImmediateInstruction(OperationCode.LW, 0, 1, (short) 0)),
                // lw r2, op2(r0)
                new InstructionCall(new ImmediateInstruction(OperationCode.LW, 0, 2, (short) 4)),
                // add r3, r1, r2
                new InstructionCall(new RegisterInstruction(OperationCode.SPECIAL, 1, 2, 3, FunctionCode.ADD)),
                // sw res(r0), r3
                new InstructionCall(new ImmediateInstruction(OperationCode.SW, 0, 3, (short) 8)),
                // halt
                new InstructionCall(new ImmediateInstruction(OperationCode.HALT, 0, 0, (short) 0)));

        return new Program(data, code);
    }

    /// Private constructor to prevent instantiation.
    private Programs() {
    }
}
