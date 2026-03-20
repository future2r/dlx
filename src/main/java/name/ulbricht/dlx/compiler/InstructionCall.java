package name.ulbricht.dlx.compiler;

import static java.util.Objects.requireNonNull;

import name.ulbricht.dlx.simulator.Instruction;

/// Represents an instruction call in the program.
/// 
/// @param label       the optional label of the instruction call
/// @param instruction the instruction being called
public record InstructionCall(String label, Instruction instruction) implements ProgramElement {

    /// Creates a new instruction call.
    public InstructionCall {
        requireNonNull(instruction, "instruction cannot be null");
    }

    /// Creates a new instruction call.
    /// 
    /// @param instruction the instruction being called
    public InstructionCall(final Instruction instruction) {
        this(null, instruction);
    }

    /// {@return the size of the instruction call in bytes, always `4`}
    @Override
    public int size() {
        return 4;
    }
}
