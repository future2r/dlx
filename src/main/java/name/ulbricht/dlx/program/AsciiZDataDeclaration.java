package name.ulbricht.dlx.program;

import static java.util.Objects.requireNonNull;

/// Represents a null-terminated ASCII data declaration in the program.
/// 
/// @param label      the optional label of the data declaration
/// @param characters the characters of the null-terminated ASCII
///                   data declaration
public record AsciiZDataDeclaration(String label, byte[] characters) implements DataDeclaration {

    /// Creates a new null-terminated ASCII data declaration.
    public AsciiZDataDeclaration {
        requireNonNull(characters, "characters must not be null");
    }

    /// {@return the size of the data declaration in bytes}
    @Override
    public int size() {
        return this.characters.length + 1;
    }

    /// {@return the byte array value of the null-terminated ASCII data declaration}
    @Override
    public byte[] data() {
        final var data = new byte[this.characters.length + 1];
        System.arraycopy(this.characters, 0, data, 0, this.characters.length);
        data[this.characters.length] = 0;
        return data;
    }
}
