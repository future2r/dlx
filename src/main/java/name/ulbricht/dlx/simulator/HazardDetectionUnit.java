package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Detects **load-use data hazards** that cannot be resolved by forwarding
/// alone and therefore require a pipeline stall.
///
/// ## What is a load-use hazard?
/// A load-use hazard arises when a load instruction (LB, LH, LW, LBU, LHU) is
/// immediately followed by an instruction that reads the register being loaded:
///
/// ```asm
/// LW  R1, 0(R2)   ; IF → ID → EX → MEM  ← data available here
/// ADD R3, R1, R4  ; IF → ID → EX         ← needs R1 here - one cycle too early
/// ```
///
/// Even with EX/MEM forwarding the loaded value is not yet in any latch when the
/// dependent instruction reaches EX. The pipeline must therefore be **stalled
/// for one cycle**:
///
/// - The IF and ID stages are held (the IF/ID latch is preserved unchanged).
/// - A bubble ([IdExLatch#BUBBLE]) is injected into the ID/EX latch.
/// - After one cycle the load result is in the MEM/WB latch and MEM/WB
///   forwarding resolves the dependency.
///
/// ## Scope
/// This unit only detects the *structural* condition. The
/// [CPU][name.ulbricht.dlx.simulator.CPU] is responsible for acting on the
/// result by freezing the PC and the IF/ID latch and inserting a bubble.
///
/// All other RAW hazards (where the producing instruction is an ALU instruction)
/// are handled transparently by the [Forwarding] unit without a stall.
final class HazardDetectionUnit {

    /// Private constructor - this class is not instantiable.
    private HazardDetectionUnit() {
    }

    /// Returns `true` when a one-cycle stall must be inserted between the
    /// instruction currently in EX (represented by `idEx`) and the instruction
    /// currently in ID (encoded raw in `ifId`).
    ///
    /// The method checks three conditions that must all be true:
    ///
    /// 1. The EX-stage instruction is a **load** (`ctrl.memory().memRead() ==
    ///    true`).
    /// 2. The load destination register is **not R0** (writes to R0 are always
    ///    discarded, so no stall is needed).
    /// 3. The load destination register **matches rs1 or rs2** of the instruction
    ///    currently in ID. The `rs2` check uses the raw bit field at bits 20–16,
    ///    which covers both the `rs2` field of R-format instructions and the `rd`
    ///    field of I-format store instructions.
    ///
    /// @param idEx the current ID/EX latch (the potential load instruction)
    /// @param ifId the current IF/ID latch (the potential dependent instruction)
    /// @return `true` if a stall is required; `false` otherwise
    static boolean detectLoadUseHazard(final IdExLatch idEx, final IfIdLatch ifId) {
        requireNonNull(idEx, "idEx must not be null");
        requireNonNull(ifId, "ifId must not be null");
        // Condition 1: the instruction in EX must be a load.
        if (!idEx.ctrl().memory().memRead())
            return false;

        // Condition 2: a load to R0 can never cause a hazard.
        if (idEx.rd() == 0)
            return false;

        // Extract the register indices from the raw instruction word in the
        // IF/ID latch. We do this instead of using decoded fields because
        // InstructionDecodeStage has not yet run for this word this cycle.
        final var word = ifId.instructionWord();
        // Bits 25–21: rs1 field (first source register for all formats)
        final var rs1 = (word >>> 21) & 0x1F;
        // Bits 20–16: rs2 field (R-format) / rd field (I-format).
        // For I-format stores this is the data-source register,
        // which can equally cause a load-use hazard.
        final var rs2 = (word >>> 16) & 0x1F;

        // Condition 3: the load's destination conflicts with a source of the
        // next instruction.
        return idEx.rd() == rs1 || idEx.rd() == rs2;
    }
}
