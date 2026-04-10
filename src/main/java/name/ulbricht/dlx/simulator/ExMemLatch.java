package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Pipeline latch between the **EX** (Execute) and **MEM** (Memory Access)
/// stages.
///
/// After the EX stage finishes, this latch holds:
///
/// - The control signals that govern the MEM and WB stages.
/// - The ALU result, which serves as either the computed value to be written
///   back (for arithmetic instructions) or the effective memory address (for
///   load/store instructions).
/// - The store-data value (`rs2Val`), which is the register value to be written
///   to memory for store instructions. For non-store instructions this field is
///   unused by MEM but still flows through.
/// - The destination register index for WB.
///
/// ## Forwarding source
/// The MEM/WB latch reads `aluResult` from this latch during the same cycle that
/// EX runs. The [Forwarding] unit therefore uses this latch as the *EX/MEM
/// forwarding path* - supplying the just-computed ALU result to the immediately
/// following instruction one cycle before it would be available via the register
/// file.
///
/// @param ctrl      the control signals governing the MEM and WB stages
/// @param aluResult the 32-bit result produced by the ALU; for loads/stores this
///                  is the effective byte address (`rs1 + imm`)
/// @param rs2Val    the (possibly forwarded) value of the store-data register;
///                  used by the MEM stage for store instructions
/// @param rd        index of the destination register (0–31); 0 means no
///                  register write-back will occur in WB
/// @param immediate the sign-extended 16-bit immediate from the ID stage;
///                  carried through for trap dispatch in WB
public record ExMemLatch(
        ControlSignals ctrl,
        int aluResult,
        int rs2Val,
        int rd,
        int immediate) {
    /// Validates that `ctrl` is not `null`.
    ///
    /// @throws NullPointerException if `ctrl` is `null`
    public ExMemLatch {
        requireNonNull(ctrl, "ctrl must not be null");
    }

    /// Canonical pipeline bubble for the EX/MEM latch.
    ///
    /// `ctrl` is [ControlSignals#NOP]; all integer fields are zero.
    public static final ExMemLatch BUBBLE = new ExMemLatch(ControlSignals.NOP, 0, 0, 0, 0);
}
