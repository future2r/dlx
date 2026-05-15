package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.util.TextPosition;

/// An instruction parsed from a `.text` section.
///
/// @param sourceId identifier of the source unit this instruction was parsed
///                 from
/// @param pos      0-based source position of the instruction token
/// @param label    the optional label attached to this instruction; `null` if
///                 absent
/// @param opcode   the instruction mnemonic, lowercase (e.g. `add`, `lw`)
/// @param operands the list of parsed operands in source order
public record ParsedInstruction(UUID sourceId, TextPosition pos, String label, String opcode,
        List<Operand> operands) implements ParsedElement {

    /// Validates and defensively copies the record components.
    public ParsedInstruction {
        requireNonNull(sourceId, "sourceId must not be null");
        requireNonNull(pos, "pos must not be null");
        requireNonNull(opcode, "opcode must not be null");
        requireNonNull(operands, "operands must not be null");
        operands = List.copyOf(operands);
    }
}
