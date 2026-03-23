package name.ulbricht.dlx.program;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.simulator.ImmediateInstruction;
import name.ulbricht.dlx.simulator.Instruction;
import name.ulbricht.dlx.simulator.JumpInstruction;
import name.ulbricht.dlx.simulator.OperationCode;
import name.ulbricht.dlx.simulator.RegisterInstruction;

/// Represents an instruction call in the program.
/// 
/// @param label       the optional label of the instruction call
/// @param instruction the instruction being called
public record InstructionCall(String label, Instruction instruction) implements ProgramElement {

    /// Creates a new instruction call.
    public InstructionCall {
        requireNonNull(instruction, "instruction cannot be null");
    }

    /// Creates a new instruction call.
    /// 
    /// @param instruction the instruction being called
    public InstructionCall(final Instruction instruction) {
        this(null, instruction);
    }

    /// {@return the size of the instruction call in bytes, always `4`}
    @Override
    public int size() {
        return 4;
    }

    @Override
    public byte[] encode() {
        final var encoded = switch (instruction) {
            case final ImmediateInstruction i -> encodeI(i);
            case final RegisterInstruction r -> encodeR(r);
            case final JumpInstruction j -> encodeJ(j);
        };
        return intToBytes(encoded);
    }

    /// Encodes an R-format instruction word.
    ///
    /// ```
    /// | SPECIAL (6) | rs1 (5) | rs2 (5) | rd (5) | 0 (5) | func (6) |
    /// ```
    ///
    private static int encodeR(final RegisterInstruction instruction) {
        checkRegister(instruction.rs1(), "rs1");
        checkRegister(instruction.rs2(), "rs2");
        checkRegister(instruction.rd(), "rd");

        return (OperationCode.SPECIAL.code << 26)
                | (instruction.rs1() << 21)
                | (instruction.rs2() << 16)
                | (instruction.rd() << 11)
                | instruction.func().code;
    }

    /// Encodes an I-format instruction word.
    ///
    /// ```
    /// | opcode (6) | rs1 (5) | rd (5) | immediate (16) |
    /// ```
    ///
    private static int encodeI(final ImmediateInstruction instruction) {
        checkRegister(instruction.rs1(), "rs1");
        checkRegister(instruction.rd(), "rd");
        checkImmediate16(instruction.immediate());

        return (instruction.opcode().code << 26)
                | (instruction.rs1() << 21)
                | (instruction.rd() << 16)
                | (instruction.immediate() & 0xFFFF);
    }

    /// Encodes a J-format instruction word.
    ///
    /// ```
    /// | opcode (6) | distance (26) |
    /// ```
    ///
    private static int encodeJ(final JumpInstruction instruction) {
        checkDistance26(instruction.distance());

        return (instruction.opcode().code << 26)
                | (instruction.distance() & 0x03FF_FFFF);
    }

    private static void checkRegister(final int reg, final String name) {
        if (reg < 0 || reg > 31) {
            throw new IllegalArgumentException(
                    name + " must be in range 0..31, got: " + reg);
        }
    }

    private static void checkImmediate16(final int imm) {
        if (imm < -32_768 || imm > 32_767) {
            throw new IllegalArgumentException(
                    "Immediate must be in range -32768..32767, got: " + imm);
        }
    }

    private static void checkDistance26(final int distance) {
        final int min = -(1 << 25); // -33_554_432
        final int max = (1 << 25) - 1; // 33_554_431
        if (distance < min || distance > max) {
            throw new IllegalArgumentException(
                    "Distance must be in range " + min + ".." + max + ", got: " + distance);
        }
    }

    private static byte[] intToBytes(final int value) {
        return new byte[] {
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }
}
