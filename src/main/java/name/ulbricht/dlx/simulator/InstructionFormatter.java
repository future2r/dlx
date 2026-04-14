package name.ulbricht.dlx.simulator;

/// Formats a raw 32-bit instruction word into a human-readable assembly string.
///
/// This is the inverse of the assembler: it takes a machine word and produces a
/// mnemonic with operands, e.g. `"ADD R1, R2, R3"` or `"LW R5, 8(R2)"`.
///
/// The formatter is stateless — all behaviour lives in the single static method
/// [#format(int)].
public final class InstructionFormatter {

    /// Private constructor — this class is not instantiable.
    private InstructionFormatter() {
    }

    /// Formats a 32-bit instruction word as a readable assembly string.
    ///
    /// Returns `"NOP"` for the canonical all-zero encoding. If the word cannot be
    /// decoded (unknown opcode or function code) the method returns a hex
    /// representation instead of throwing.
    ///
    /// @param word the 32-bit instruction word
    /// @return a human-readable assembly string
    public static String format(final int word) {
        if (word == 0) {
            return "NOP";
        }

        final Instruction instr;
        try {
            instr = InstructionDecoder.decode(word);
        } catch (final IllegalArgumentException _) {
            return "0x%08X".formatted(Integer.valueOf(word));
        }

        return switch (instr) {
            case final RegisterInstruction r -> formatRegister(r);
            case final ImmediateInstruction i -> formatImmediate(i);
            case final JumpInstruction j -> formatJump(j);
        };
    }

    private static String formatRegister(final RegisterInstruction r) {
        final var mnemonic = r.func().name();
        return "%s R%d, R%d, R%d".formatted(mnemonic,
                Integer.valueOf(r.rd()), Integer.valueOf(r.rs1()), Integer.valueOf(r.rs2()));
    }

    private static String formatImmediate(final ImmediateInstruction i) {
        final var mnemonic = i.opcode().name();
        final var imm = (int) i.immediate();

        return switch (i.opcode()) {
            // Branches: BEQZ R1, #offset
            case BEQZ, BNEZ -> "%s R%d, #%d".formatted(mnemonic,
                    Integer.valueOf(i.rs1()), Integer.valueOf(imm));
            // Loads: LW R1, offset(R2)
            case LB, LH, LW, LBU, LHU -> "%s R%d, %d(R%d)".formatted(mnemonic,
                    Integer.valueOf(i.rd()), Integer.valueOf(imm), Integer.valueOf(i.rs1()));
            // Stores: SW offset(R1), R2 — rd is the data source
            case SB, SH, SW -> "%s %d(R%d), R%d".formatted(mnemonic,
                    Integer.valueOf(imm), Integer.valueOf(i.rs1()), Integer.valueOf(i.rd()));
            // Jump register: JR R1 / JALR R1
            case JR, JALR -> "%s R%d".formatted(mnemonic, Integer.valueOf(i.rs1()));
            // LHI: LHI R1, #imm
            case LHI -> "%s R%d, #0x%04X".formatted(mnemonic,
                    Integer.valueOf(i.rd()), Integer.valueOf(imm & 0xFFFF));
            // Trap: TRAP #imm
            case TRAP -> "TRAP #0x%02X".formatted(Integer.valueOf(imm & 0xFF));
            // Arithmetic/logic/shift/set immediate: ADDI R1, R2, #imm
            default -> "%s R%d, R%d, #%d".formatted(mnemonic,
                    Integer.valueOf(i.rd()), Integer.valueOf(i.rs1()), Integer.valueOf(imm));
        };
    }

    private static String formatJump(final JumpInstruction j) {
        final var mnemonic = j.opcode().name();
        return "%s #%d".formatted(mnemonic, Integer.valueOf(j.distance()));
    }
}
