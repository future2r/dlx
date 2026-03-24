package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

/// A memory operand with a label offset and a base register.
///
/// Represents the `label(Rbase)` addressing mode where the offset is a data
/// symbol whose address will be resolved at assembly time, e.g. `op(r0)`.
///
/// @param offsetLabel the label name that supplies the byte offset, lowercase
/// @param baseReg     the base register index, 0–31
public record LabelMemoryOperand(String offsetLabel, int baseReg) implements Operand {

    /// Validates the record components.
    public LabelMemoryOperand {
        requireNonNull(offsetLabel, "offsetLabel must not be null");
    }
}
