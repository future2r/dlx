package name.ulbricht.dlx.asm.parser;

/// A memory operand with an integer byte offset and a base register.
///
/// Represents the `offset(Rbase)` addressing mode where the offset is an integer
/// literal, e.g. `100(r0)`.
///
/// @param offset  the signed byte offset
/// @param baseReg the base register index, 0–31
public record MemoryOperand(int offset, int baseReg) implements Operand {
}
