package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// I-format instruction (immediate operand, load/store, branch, jump-register).
///
/// Bit layout of the 32-bit instruction word:
///
/// ```
/// | opcode (6) | rs1 (5) | rd (5) | immediate (16) |
/// ```
///
/// The `rd` field has dual semantics depending on the instruction class:
///
/// - **Arithmetic / logic / load instructions** - `rd` is the *destination*
///   register that receives the result.
/// - **Store instructions** (`SB`, `SH`, `SW`) - `rd` is the *source* register
///   whose value is written to memory. No register is modified.
/// - **Branch instructions** (`BEQZ`, `BNEZ`) - `rd` is unused (zero). `rs1` is
///   the register tested. `immediate` is the signed byte offset added to the
///   instruction's PC to form the branch target.
/// - **Jump-register instructions** (`JR`, `JALR`) - `rs1` holds the jump target
///   address. `rd` is unused except for `JALR`, where the hardware implicitly
///   writes `PC + 4` to R31 (the link register).
///
/// The 16-bit `immediate` field is stored as a Java `short` so that it is
/// automatically sign-extended when assigned to an `int`.
///
/// @param opcode    the instruction opcode (never [OperationCode#SPECIAL] or a
///                  J-format opcode)
/// @param rs1       index of the first source register (0–31)
/// @param rd        index of the destination register **or** the store-data
///                  source register, depending on instruction class (0–31)
/// @param immediate 16-bit signed immediate value; sign-extended to 32 bits
///                  during decode
public record ImmediateInstruction(OperationCode opcode, int rs1, int rd, short immediate)
        implements Instruction {

    /// Validates that `opcode` is not `null`.
    ///
    /// @throws NullPointerException if `opcode` is `null`
    public ImmediateInstruction {
        requireNonNull(opcode, "opcode must not be null");
    }
}
