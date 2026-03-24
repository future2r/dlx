package name.ulbricht.dlx.asm.parser;

/// A parsed instruction operand.
///
/// Operands are the arguments to an instruction. Depending on the instruction
/// format, an operand can be a register, an integer immediate, a label
/// reference, or a memory address expressed as an offset plus a base register.
public sealed interface Operand
                permits RegisterOperand, ImmediateOperand, LabelOperand, MemoryOperand, LabelMemoryOperand {
        // no members
}
