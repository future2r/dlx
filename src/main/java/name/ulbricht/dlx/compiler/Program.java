package name.ulbricht.dlx.compiler;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/// Represents a compiled program.
/// 
/// @param data the list of data declarations in the program
/// @param code the list of instruction calls in the program
public record Program(List<DataDeclaration> data, List<InstructionCall> code) {

    /// Creates a new program.
    public Program {
        requireNonNull(data, "data must not be null");
        requireNonNull(code, "code must not be null");
    }

    /// {@return a list of all program elements in the program, each with its
    /// associated address}
    public List<AddressedProgramElement> addressed() {
        final AtomicInteger address = new AtomicInteger(0);

        return Stream.concat(data.stream(), code.stream())
                .map(element -> new AddressedProgramElement(address.getAndAdd(element.size()), element))
                .toList();
    }
}
