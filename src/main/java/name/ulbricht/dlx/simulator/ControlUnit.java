package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.simulator.ALU.Operation;
import name.ulbricht.dlx.simulator.ControlSignals.MemWidth;

/// Translates a decoded [Instruction] into the [ControlSignals] that govern the
/// rest of its journey through the pipeline.
///
/// This is the hardware equivalent of the DLX control unit ROM / PLA. The class
/// is purely functional - no state, no instantiation.

final class ControlUnit {

    /// Private constructor - this class is not instantiable.
    private ControlUnit() {
    }

    /// Produces the [ControlSignals] for the given decoded instruction.
    ///
    /// @param instr the decoded instruction (must not be `null`)
    /// @return the corresponding control-signal set; never `null`
    /// @throws IllegalArgumentException if the instruction carries an opcode or
    ///                                  function code that has no mapping
    static ControlSignals decode(final Instruction instr) {
        requireNonNull(instr, "instr must not be null");

        // Dispatch on the sealed subtype - the compiler guarantees exhaustiveness.
        return switch (instr) {
            case final RegisterInstruction r -> decodeR(r.func());
            case final ImmediateInstruction i -> decodeI(i.opcode());
            case final JumpInstruction j -> decodeJ(j.opcode());
        };
    }

    /// Maps an R-format function code to its control signals.
    ///
    /// All R-format instructions write to a destination register and use two
    /// register operands (no immediate source, no memory access). The NOP special
    /// case returns [ControlSignals#NOP] directly because it must not set
    /// `regWrite`.
    ///
    /// @param func the R-format function code
    /// @return the corresponding control signals
    private static ControlSignals decodeR(final FunctionCode func) {
        // NOP (SLL R0,R0,0) must not trigger a register write.
        if (func == FunctionCode.NOP)
            return ControlSignals.NOP;

        // Map every valid function code to an ALU operation.
        final var op = switch (func) {
            case ADD -> Operation.ADD;
            case ADDU -> Operation.ADDU;
            case SUB -> Operation.SUB;
            case SUBU -> Operation.SUBU;
            case AND -> Operation.AND;
            case OR -> Operation.OR;
            case XOR -> Operation.XOR;
            case SLL -> Operation.SLL;
            case SRL -> Operation.SRL;
            case SRA -> Operation.SRA;
            case SEQ -> Operation.SEQ;
            case SNE -> Operation.SNE;
            case SLT -> Operation.SLT;
            case SGT -> Operation.SGT;
            case SLE -> Operation.SLE;
            case SGE -> Operation.SGE;
            default -> throw new IllegalStateException("Unhandled func: " + func);
        };

        // All R-format instructions: regWrite=true, aluSrc=false (register operands).
        return regWrite(op, false);
    }

    /// Maps an I-format opcode to its control signals.
    ///
    /// This covers arithmetic/logic immediates, shifts, set-if immediates, LHI,
    /// loads, stores, branches, jump-register instructions, and trap.
    ///
    /// @param opcode an I-format opcode (not SPECIAL, J, or JAL)
    /// @return the corresponding control signals
    private static ControlSignals decodeI(final OperationCode opcode) {
        return switch (opcode) {

            // -- Arithmetic immediate: regWrite=true, aluSrc=true (immediate operand)
            case ADDI -> regWrite(Operation.ADD, true);
            case ADDUI -> regWrite(Operation.ADDU, true);
            case SUBI -> regWrite(Operation.SUB, true);
            case SUBUI -> regWrite(Operation.SUBU, true);

            // -- Logic immediate
            case ANDI -> regWrite(Operation.AND, true);
            case ORI -> regWrite(Operation.OR, true);
            case XORI -> regWrite(Operation.XOR, true);

            // -- Shift immediate
            case SLLI -> regWrite(Operation.SLL, true);
            case SRLI -> regWrite(Operation.SRL, true);
            case SRAI -> regWrite(Operation.SRA, true);

            // -- Set-if immediate
            case SEQI -> regWrite(Operation.SEQ, true);
            case SNEI -> regWrite(Operation.SNE, true);
            case SLTI -> regWrite(Operation.SLT, true);
            case SGTI -> regWrite(Operation.SGT, true);
            case SLEI -> regWrite(Operation.SLE, true);
            case SGEI -> regWrite(Operation.SGE, true);

            // -- LHI: regWrite=true, aluSrc=true, loadHighImm=true, ALU op=PASS_B.
            // The EX stage pre-shifts imm16 << 16 before handing it to the ALU.
            case LHI -> new ControlSignals(
                    true, false, false, false,
                    false, false, false, false,
                    false, true, true,
                    MemWidth.WORD, false,
                    Operation.PASS_B, false);

            // -- Loads: regWrite=true, memRead=true, memToReg=true, aluSrc=true.
            // The ALU computes the effective address rs1 + imm.
            case LB -> load(MemWidth.BYTE, false);
            case LH -> load(MemWidth.HALF, false);
            case LW -> load(MemWidth.WORD, false);
            case LBU -> load(MemWidth.BYTE, true);
            case LHU -> load(MemWidth.HALF, true);

            // -- Stores: regWrite=false, memWrite=true, aluSrc=true.
            // The ALU computes the effective address rs1 + imm.
            case SB -> store(MemWidth.BYTE);
            case SH -> store(MemWidth.HALF);
            case SW -> store(MemWidth.WORD);

            // -- BEQZ: branch=true, branchNotZero=false.
            // Branch condition (rs1 == 0) is evaluated in EX.
            case BEQZ -> new ControlSignals(
                    false, false, false, false,
                    true, false, false, false,
                    false, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            // -- BNEZ: branch=true, branchNotZero=true.
            // Branch condition (rs1 != 0) is evaluated in EX.
            case BNEZ -> new ControlSignals(
                    false, false, false, false,
                    true, true, false, false,
                    false, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            // -- JR: jump=true, jumpReg=true.
            // PC target = rs1 value; no register write.
            case JR -> new ControlSignals(
                    false, false, false, false,
                    false, false, true, true,
                    false, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            // -- JALR: jump=true, jumpReg=true, jalLink=true, regWrite=true.
            // PC target = rs1; R31 ← PC + 4.
            case JALR -> new ControlSignals(
                    true, false, false, false,
                    false, false, true, true,
                    true, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            // -- TRAP: trap=true; all other signals inactive.
            case TRAP -> new ControlSignals(
                    false, false, false, false,
                    false, false, false, false,
                    false, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, true);

            default -> throw new IllegalArgumentException(
                    "Unexpected I-format opcode: " + opcode);
        };
    }

    /// Maps a J-format opcode to its control signals.
    ///
    /// @param opcode either [OperationCode#J] or [OperationCode#JAL]
    /// @return the corresponding control signals
    private static ControlSignals decodeJ(final OperationCode opcode) {
        return switch (opcode) {

            // -- J: jump=true, jumpReg=false; no register write.
            // PC target = PC + sign_extend(dist26).
            case J -> new ControlSignals(
                    false, false, false, false,
                    false, false, true, false,
                    false, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            // -- JAL: jump=true, jalLink=true, regWrite=true.
            // PC target = PC + sign_extend(dist26); R31 ← PC + 4.
            case JAL -> new ControlSignals(
                    true, false, false, false,
                    false, false, true, false,
                    true, false, false,
                    MemWidth.WORD, false,
                    Operation.ADD, false);

            default -> throw new IllegalArgumentException(
                    "Unexpected J-format opcode: " + opcode);
        };
    }

    /// Builds signals for a register-result instruction (arithmetic, logic, shift,
    /// set-if - both R-format and I-format variants).
    ///
    /// Sets `regWrite = true`; all memory and branch/jump signals are `false`.
    ///
    /// @param op     the ALU operation to perform
    /// @param aluSrc `true` if the second ALU operand is the immediate value;
    ///               `false` if it is register rs2 (R-format)
    /// @return the constructed [ControlSignals]
    private static ControlSignals regWrite(final Operation op, final boolean aluSrc) {
        return new ControlSignals(
                true, false, false, false,
                false, false, false, false,
                false, aluSrc, false,
                MemWidth.WORD, false,
                op, false);
    }

    /// Builds signals for a load instruction.
    ///
    /// Sets `regWrite = true`, `memRead = true`, `memToReg = true`, and `aluSrc =
    /// true` (effective address = rs1 + imm).
    ///
    /// @param width    byte, half-word, or word
    /// @param unsigned `true` for LBU / LHU (zero-extend); `false` for LB / LH / LW
    ///                 (sign-extend)
    /// @return the constructed [ControlSignals]
    private static ControlSignals load(final MemWidth width, final boolean unsigned) {
        return new ControlSignals(
                true, true, false, true,
                false, false, false, false,
                false, true, false,
                width, unsigned,
                Operation.ADD, false);
    }

    /// Builds signals for a store instruction.
    ///
    /// Sets `memWrite = true` and `aluSrc = true` (effective address = rs1 + imm).
    /// `regWrite` is `false` - stores do not modify any register.
    ///
    /// @param width byte, half-word, or word
    /// @return the constructed [ControlSignals]
    private static ControlSignals store(final MemWidth width) {
        return new ControlSignals(
                false, false, true, false,
                false, false, false, false,
                false, true, false,
                width, false,
                Operation.ADD, false);
    }
}
