package name.ulbricht.dlx.simulator;

/// Sealed supertype for the three DLX instruction formats.
///
/// Every decoded instruction is one of:
///
/// | Subtype | Format | Description |
/// |---------|--------|-------------|
/// | [RegisterInstruction] | R | register-to-register operations |
/// | [ImmediateInstruction] | I | immediate, load/store, branch, jump-register |
/// | [JumpInstruction] | J | unconditional jump / jump-and-link |
///
/// Because the hierarchy is sealed, switch expressions over `Instruction` are
/// exhaustive without a `default` branch, which makes the compiler verify that
/// all formats are handled.
///
/// @see InstructionDecoder
public sealed interface Instruction permits RegisterInstruction, ImmediateInstruction, JumpInstruction {

    /// Returns the opcode field shared by all instruction formats.
    ///
    /// For R-format instructions this is always [OperationCode#SPECIAL]; the actual
    /// operation is in [RegisterInstruction#func()].
    ///
    /// @return the operation code
    OperationCode opcode();
}
