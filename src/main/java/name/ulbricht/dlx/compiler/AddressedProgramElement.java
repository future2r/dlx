package name.ulbricht.dlx.compiler;

import static java.util.Objects.requireNonNull;

/// Represents a program element with an associated address.
/// 
/// @param address the address of the program element
/// @param element the program element
public record AddressedProgramElement(int address, ProgramElement element) {

    public AddressedProgramElement {
        requireNonNull(element, "element must not be null");
    }
}
