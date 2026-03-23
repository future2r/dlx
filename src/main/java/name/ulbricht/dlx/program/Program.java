package name.ulbricht.dlx.program;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    /// {@return the encoded bytes of this program}
    public byte[] encoded() {
        return Stream.concat(data().stream(), code().stream())
                .map(ProgramElement::encode)
                .collect(
                        ByteArrayOutputStream::new,
                        (out, arr) -> {
                            try {
                                out.write(arr);
                            } catch (final IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                        },
                        (o1, o2) -> {
                            try {
                                o2.writeTo(o1);
                            } catch (final IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                        })
                .toByteArray();
    }

    /// {@return the entry point address of this program, i.e. the address of the
    /// first instruction}
    public int entryPoint() {
        // Skip the data secion
        final var codeStart = data.stream().mapToInt(DataDeclaration::size).sum();

        // find the address of the instruction with label "main"
        final int mainStart = code.stream()
                .takeWhile(instuctionCall -> !"main".equals(instuctionCall.label()))
                .mapToInt(InstructionCall::size)
                .sum();

        // If there is no instruction with label "main", the entry point is the first
        // instruction. Otherwise, it is the instruction with label "main".
        return mainStart == code().size() * 4 ? codeStart : codeStart + mainStart;
    }
}
