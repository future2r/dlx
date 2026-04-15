package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// **ID** (Instruction Decode) stage.
///
/// This is the second of the five pipeline stages. It:
///
/// 1. Decodes the raw instruction word from the IF/ID latch into a typed
///    [Instruction] value.
/// 2. Asks the [ControlUnit] to produce the [ControlSignals] for that
///    instruction.
/// 3. Reads the required source-register values from the
///    [Registers][name.ulbricht.dlx.simulator.Registers].
/// 4. Packages everything into an [IdExLatch] latch for the EX stage.
///
/// ## Register read timing
/// Register reads use the state of the register file at the **start** of the
/// current cycle - before the WB stage has written its result. Any remaining RAW
/// hazards are handled transparently by the [Forwarding] unit in the EX stage.
/// The only case that cannot be forwarded - a load immediately followed by a
/// dependent instruction - is caught by [HazardDetectionUnit] and causes a stall
/// *before* ID runs for the dependent instruction.
///
/// ## Store instruction operand layout
/// For store instructions (`SB`, `SH`, `SW`) the I-format `rd` bit field is the
/// *data source* register (not a destination). The decode step therefore places
/// `registers[rd]` in `rs2Val` and sets the `rs2` index accordingly, so the
/// forwarding unit can track the register correctly.
///
/// ## NOP / bubble detection
/// An all-zero instruction word (`0x00000000`) is the canonical NOP encoding.
/// The stage short-circuits and returns [IdExLatch#BUBBLE] directly without
/// performing a full decode, avoiding unnecessary table lookups.
final class InstructionDecodeStage {

    /// Private constructor - this class is not instantiable.
    private InstructionDecodeStage() {
    }

    /// Decodes the instruction in `ifId` and returns the new ID/EX latch value.
    ///
    /// @param ifId the current IF/ID latch produced by the IF stage
    /// @param regs the register file to read source operands from
    /// @return the populated [IdExLatch] latch; [IdExLatch#BUBBLE] for a NOP
    static IdExLatch execute(final IfIdLatch ifId, final Registers regs) {
        requireNonNull(ifId, "ifId must not be null");
        requireNonNull(regs, "regs must not be null");

        // An all-zero word is the NOP encoding (SLL R0, R0, 0).
        // Return a bubble immediately to avoid an unnecessary decode round-trip.
        if (ifId.instructionWord() == 0) {
            return IdExLatch.BUBBLE;
        }

        // Full decode: map the raw word to a typed Instruction value.
        final var instr = InstructionDecoder.decode(ifId.instructionWord());

        // Ask the control unit for the full set of pipeline control signals.
        final var ctrl = ControlUnit.decode(instr);

        return switch (instr) {

            // -----------------------------------------------------------------
            // R-format: two register source operands, one destination
            // -----------------------------------------------------------------
            case final RegisterInstruction r -> {
                final var rs1Val = regs.read(r.rs1());
                final var rs2Val = regs.read(r.rs2());
                // Immediate is not used by R-format instructions; pass 0.
                yield new IdExLatch(ifId.pc(), ctrl, rs1Val, rs2Val, 0,
                        r.rs1(), r.rs2(), r.rd());
            }

            // -----------------------------------------------------------------
            // I-format: one register source + sign-extended immediate;
            // store instructions also have a data-source register in the rd field
            // -----------------------------------------------------------------
            case final ImmediateInstruction i -> {
                // The short→int cast sign-extends the 16-bit immediate to 32 bits.
                final var imm = (int) i.immediate();
                final var rs1Val = regs.read(i.rs1());

                // For stores the 'rd' field of the instruction word holds the
                // data-source register index (not a destination). We map it to
                // rs2 so the forwarding unit sees it correctly.
                final var rs2Idx = isStore(i.opcode()) ? i.rd() : 0;
                final var rs2Val = regs.read(rs2Idx);

                // Determine the write-back destination register.
                final var rdIdx = destinationReg(i, ctrl);

                yield new IdExLatch(ifId.pc(), ctrl, rs1Val, rs2Val, imm,
                        i.rs1(), rs2Idx, rdIdx);
            }

            // -----------------------------------------------------------------
            // J-format: no register operands; only a 26-bit signed distance
            // -----------------------------------------------------------------
            case final JumpInstruction j -> {
                // JAL / JALR implicitly write to R31 (the link register).
                // All other J-format instructions (plain J) do not write.
                final var rdIdx = ctrl.flow().jalLink() ? 31 : 0;
                // No register values to read; pass the sign-extended distance
                // as the immediate.
                yield new IdExLatch(ifId.pc(), ctrl, 0, 0, j.distance(),
                        0, 0, rdIdx);
            }
        };
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /// Returns `true` for the three store opcodes (`SB`, `SH`, `SW`).
    ///
    /// Used to decide whether the I-format `rd` field is a data-source register
    /// (stores) or a destination register (everything else).
    ///
    /// @param op the opcode of the I-format instruction
    /// @return `true` if the instruction is a store
    private static boolean isStore(final OperationCode op) {
        return op == OperationCode.SB || op == OperationCode.SH || op == OperationCode.SW;
    }

    /// Resolves the destination register index for an I-format instruction.
    ///
    /// - Returns `0` when the instruction does not write a register
    ///   (`ctrl.regWrite() == false`).
    /// - Returns `31` for JALR (`ctrl.flow().jalLink() == true`), which implicitly
    ///   targets R31 regardless of the `rd` bit field.
    /// - Otherwise returns `i.rd()` directly.
    ///
    /// @param i    the decoded I-format instruction
    /// @param ctrl the control signals already generated for that instruction
    /// @return the destination register index (0 = no write-back)
    private static int destinationReg(final ImmediateInstruction i, final ControlSignals ctrl) {
        if (!ctrl.regWrite())
            return 0; // store / branch / JR / trap
        if (ctrl.flow().jalLink())
            return 31; // JALR implicitly links to R31
        return i.rd();
    }
}
