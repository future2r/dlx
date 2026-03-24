package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

/// An immediate operand whose value is the address of a data label.
///
/// Used by instructions that load an address into a register, e.g.
/// `addi r1, r0, str` or `lhi r1, str`.
/// The label is resolved to a concrete address by the compiler.
///
/// @param name the referenced label name, lowercase
public record LabelImmediateOperand(String name) implements Operand {

    /// Validates the record components.
    public LabelImmediateOperand {
        requireNonNull(name, "name must not be null");
    }
}
