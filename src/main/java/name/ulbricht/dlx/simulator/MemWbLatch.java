package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Pipeline latch between the **MEM** (Memory Access) and **WB** (Write Back)
/// stages.
///
/// This is the last inter-stage latch in the pipeline. After MEM completes, this
/// record holds exactly what WB needs to finalise the instruction:
///
/// - For **arithmetic / logic / shift / set-if** instructions: `ctrl.memToReg()
///   == false`, so WB writes `aluResult` to the destination register.
/// - For **load** instructions: `ctrl.memToReg() == true`, so WB writes
///   `memData` (the value read from memory) to the destination register.
/// - For **store**, **branch**, **jump** (without link), and **NOP**:
///   `ctrl.regWrite() == false`, so WB writes nothing.
/// - For **JAL / JALR**: `ctrl.regWrite() == true` and `ctrl.memToReg() ==
///   false`, so WB writes `aluResult` (which holds `PC + 4`) to R31.
///
/// ## Forwarding source
/// The [Forwarding] unit uses this latch as the *MEM/WB forwarding path* to
/// supply a result to the instruction currently in EX, one cycle before WB would
/// write it to the register file. When `ctrl.memToReg()` is `true` the
/// forwarding unit uses `memData`; otherwise it uses `aluResult`.
///
/// @param ctrl      the control signals governing the WB stage
/// @param aluResult the 32-bit ALU result carried from the EX stage
/// @param memData   the value read from memory in the MEM stage (only valid when
///                  `ctrl.memRead()` was `true`)
/// @param rd        index of the destination register (0–31); 0 means no
///                  register write-back will occur
/// @param immediate the sign-extended 16-bit immediate from the ID stage;
///                  used by the CPU for trap dispatch after WB retires
public record MemWbLatch(
        ControlSignals ctrl,
        int aluResult,
        int memData,
        int rd,
        int immediate) {
    /// Validates that `ctrl` is not `null`.
    ///
    /// @throws NullPointerException if `ctrl` is `null`
    public MemWbLatch {
        requireNonNull(ctrl, "ctrl must not be null");
    }

    /// Canonical pipeline bubble for the MEM/WB latch.
    ///
    /// `ctrl` is [ControlSignals#NOP]; all integer fields are zero.
    public static final MemWbLatch BUBBLE = new MemWbLatch(ControlSignals.NOP, 0, 0, 0, 0);
}
