package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

/// A label reference operand, used as the target of branch and jump
/// instructions.
///
/// @param name the label name, lowercase
public record LabelOperand(String name) implements Operand {

    /// Validates the record components.
    public LabelOperand {
        requireNonNull(name, "name must not be null");
    }
}
