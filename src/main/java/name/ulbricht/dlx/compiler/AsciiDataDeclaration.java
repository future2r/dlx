package name.ulbricht.dlx.compiler;

import static java.util.Objects.requireNonNull;

/// Represents an ASCII data declaration in the program.
/// 
/// @param label      the optional label of the data declaration
/// @param characters the chaarcters of the ASCII data declaration
public record AsciiDataDeclaration(String label, byte[] characters) implements DataDeclaration {

    /// Creates a new ASCII data declaration.
    public AsciiDataDeclaration {
        requireNonNull(characters, "characters must not be null");
    }

    /// {@return the size of the data declaration in bytes}
    @Override
    public int size() {
        return this.characters.length;
    }

    /// {@return the byte array value of the ASCII data declaration}
    @Override
    public byte[] data() {
        return this.characters.clone();
    }
}
