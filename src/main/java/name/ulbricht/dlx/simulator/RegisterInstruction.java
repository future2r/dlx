package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// R-format instruction (register-to-register).
///
/// Bit layout of the 32-bit instruction word:
///
/// ```
/// | opcode (6) | rs1 (5) | rs2 (5) | rd (5) | unused (5) | func (6) |
/// ```
///
/// The ALU reads `rs1` and `rs2`, executes the operation indicated by `func`,
/// and writes the result into `rd`. The 5-bit `unused` field (bits 10–6) is
/// reserved and must be zero; the decoder ignores it.
///
/// The opcode is always [OperationCode#SPECIAL] (0x00) for R-format
/// instructions.
///
/// @param opcode always [OperationCode#SPECIAL]
/// @param rs1    index of the first source register (0–31)
/// @param rs2    index of the second source register (0–31)
/// @param rd     index of the destination register (0–31); 0 means the result is
///               discarded (R0 is immutable)
/// @param func   the 6-bit function code that selects the ALU operation
public record RegisterInstruction(OperationCode opcode, int rs1, int rs2, int rd, FunctionCode func)
        implements Instruction {

    /// Validates that `opcode` and `func` are not `null`.
    ///
    /// @throws NullPointerException if `opcode` or `func` is `null`
    public RegisterInstruction {
        requireNonNull(opcode, "opcode must not be null");
        requireNonNull(func, "func must not be null");
    }
}
