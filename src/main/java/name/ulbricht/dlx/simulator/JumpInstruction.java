package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// J-format instruction (unconditional jump or jump-and-link).
///
/// Bit layout of the 32-bit instruction word:
///
/// ```
/// | opcode (6) | distance (26) |
/// ```
///
/// The 26-bit `distance` field is sign-extended to 32 bits during decode and
/// then added directly to the address of the jump instruction itself to obtain
/// the target address. This convention allows forward and backward jumps within
/// a ±32 MiB window relative to the jump site.
///
/// Only [OperationCode#J] and [OperationCode#JAL] use this format:
///
/// - `J` - unconditional jump; no register is written.
/// - `JAL` - unconditional jump; additionally saves `PC + 4` in R31 as the
///   return address (link register).
///
/// @param opcode   either [OperationCode#J] or [OperationCode#JAL]
/// @param distance sign-extended 26-bit byte offset from the instruction's PC to
///                 the jump target
public record JumpInstruction(OperationCode opcode, int distance) implements Instruction {

    /// Validates that `opcode` is not `null`.
    ///
    /// @throws NullPointerException if `opcode` is `null`
    public JumpInstruction {
        requireNonNull(opcode, "opcode must not be null");
    }
}
