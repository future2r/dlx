package name.ulbricht.dlx.asm.parser;

/// An integer immediate operand.
///
/// @param value the integer value of the immediate
public record ImmediateOperand(int value) implements Operand {
}
