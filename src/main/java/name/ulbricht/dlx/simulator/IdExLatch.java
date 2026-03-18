package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Pipeline latch between the **ID** (Instruction Decode) and **EX** (Execute)
/// stages.
///
/// This record holds everything the EX stage needs to execute an instruction:
/// the control signals, the register operand values (possibly to be overridden
/// by forwarding), the sign-extended immediate, the original register indices
/// (for forwarding and hazard detection), and the destination register.
///
/// ## Register index conventions
/// `rs1` and `rs2` always hold the *indices* of the physical registers that were
/// read in ID, regardless of instruction format:
///
/// - For R-format: `rs1` = first source, `rs2` = second source.
/// - For I-format stores (`SB`, `SH`, `SW`): `rs1` = base-address register,
///   `rs2` = data-source register (the `rd` field of the instruction word).
/// - For branches, jumps, and other I-format: `rs2 = 0`.
///
/// The [Forwarding] unit and [HazardDetectionUnit] rely on these indices - not
/// on the values - to determine whether forwarding or a stall is needed.
///
/// @param pc        the byte address of the instruction being executed; used to
///                  compute branch/jump targets and link addresses
/// @param ctrl      control signals produced by the ID stage for this
///                  instruction
/// @param rs1Val    the value read from register `rs1` in the ID stage; may be
///                  replaced by a forwarded value in EX
/// @param rs2Val    the value read from register `rs2` (or the store-data
///                  register) in the ID stage; may be replaced by forwarding
/// @param immediate the sign-extended 16-bit (I-format) or 26-bit (J-format)
///                  immediate / distance value
/// @param rs1       index of the first source register (0–31), used by
///                  forwarding and hazard detection
/// @param rs2       index of the second source / store-data register (0–31),
///                  used by forwarding and hazard detection
/// @param rd        index of the destination register (0–31); 0 means no
///                  register write-back will occur
public record IdExLatch(
        int pc,
        ControlSignals ctrl,
        int rs1Val,
        int rs2Val,
        int immediate,
        int rs1,
        int rs2,
        int rd) {
    /// Validates that `ctrl` is not `null`.
    ///
    /// @throws NullPointerException if `ctrl` is `null`
    public IdExLatch {
        requireNonNull(ctrl, "ctrl must not be null");
    }

    /// Canonical pipeline bubble for the ID/EX latch.
    ///
    /// All register indices and values are zero; `ctrl` is [ControlSignals#NOP] so
    /// no side effects are produced when this bubble flows through EX, MEM, and WB.
    public static final IdExLatch BUBBLE = new IdExLatch(0, ControlSignals.NOP, 0, 0, 0, 0, 0, 0);
}
