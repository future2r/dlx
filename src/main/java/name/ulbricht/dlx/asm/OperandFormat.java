package name.ulbricht.dlx.asm;

/// Classifies the operand pattern that an [Instruction] expects.
///
/// Each constant corresponds to a distinct combination of register, immediate,
/// memory, and label operands that the parser must read and the compiler must
/// encode.
public enum OperandFormat {

    /// R-format: `Rd, Rs1, Rs2` (register-to-register).
    R,

    /// I-format arithmetic/logic/shift/set: `Rd, Rs1, Imm`.
    I_ARITH,

    /// Load: `Rd, Imm(Rs)` (register + displacement memory access).
    LOAD,

    /// Store: `Imm(Rs), Rsrc` (displacement memory access + source register).
    STORE,

    /// Register + immediate: `Rd, Imm` (used by LHI).
    RD_IMM,

    /// Register + label: `Rs, Label` (conditional branches).
    RS_LABEL,

    /// Label only: `Label` (unconditional jumps).
    LABEL,

    /// Register only: `Rs` (indirect jumps).
    RS,

    /// No operands (halt).
    NONE
}
