package name.ulbricht.dlx.asm.parser;

/// A register operand, referring to one of the 32 general-purpose registers.
///
/// @param number the register index, 0–31
public record RegisterOperand(int number) implements Operand {
}
