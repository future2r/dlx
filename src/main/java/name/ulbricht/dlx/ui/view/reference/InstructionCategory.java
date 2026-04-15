package name.ulbricht.dlx.ui.view.reference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import name.ulbricht.dlx.asm.Instruction;

/// Groups [Instruction] constants into logical categories for the reference
/// view.
public enum InstructionCategory {

    /// Arithmetic operations (register and immediate).
    ARITHMETIC(List.of(
            Instruction.ADD, Instruction.ADDU, Instruction.SUB, Instruction.SUBU,
            Instruction.ADDI, Instruction.ADDUI, Instruction.SUBI, Instruction.SUBUI)),

    /// Bitwise logic operations (register and immediate).
    LOGIC(List.of(
            Instruction.AND, Instruction.OR, Instruction.XOR,
            Instruction.ANDI, Instruction.ORI, Instruction.XORI)),

    /// Shift operations (register and immediate).
    SHIFTS(List.of(
            Instruction.SLL, Instruction.SRL, Instruction.SRA,
            Instruction.SLLI, Instruction.SRLI, Instruction.SRAI)),

    /// Set-if comparison operations (register and immediate).
    COMPARISON(List.of(
            Instruction.SLT, Instruction.SLE, Instruction.SEQ,
            Instruction.SGT, Instruction.SGE, Instruction.SNE,
            Instruction.SLTI, Instruction.SLEI, Instruction.SEQI,
            Instruction.SGTI, Instruction.SGEI, Instruction.SNEI)),

    /// Load, store, and load-high-immediate operations.
    MEMORY(List.of(
            Instruction.LB, Instruction.LH, Instruction.LW,
            Instruction.LBU, Instruction.LHU,
            Instruction.SB, Instruction.SH, Instruction.SW,
            Instruction.LHI)),

    /// Conditional branches and unconditional jumps.
    BRANCHES_AND_JUMPS(List.of(
            Instruction.BEQZ, Instruction.BNEZ,
            Instruction.J, Instruction.JAL,
            Instruction.JR, Instruction.JALR)),

    /// Simulator-specific instructions.
    SPECIAL(List.of(Instruction.TRAP));

    /// The instructions belonging to this category.
    public final List<Instruction> instructions;

    InstructionCategory(final List<Instruction> instructions) {
        this.instructions = instructions;
    }

    /// Reverse lookup from instruction to its category, built once.
    private static final Map<Instruction, InstructionCategory> BY_INSTRUCTION =
            Stream.of(values())
                    .flatMap(cat -> cat.instructions.stream().map(instr -> Map.entry(instr, cat)))
                    .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

    /// {@return the category that contains the given instruction}
    ///
    /// @param instruction the instruction to look up
    /// @throws IllegalArgumentException if the instruction is not in any category
    public static InstructionCategory categoryOf(final Instruction instruction) {
        final var category = BY_INSTRUCTION.get(instruction);
        if (category == null) {
            throw new IllegalArgumentException("No category for instruction: " + instruction);
        }
        return category;
    }
}
