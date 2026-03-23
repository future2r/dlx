package name.ulbricht.dlx.program;

/// Represents a byte data declaration in the program.
/// 
/// @param label the optional label of the data declaration
/// @param value the integer value of the byte data declaration
public record ByteDataDeclaration(String label, byte value) implements DataDeclaration {

    /// {@return the size of the data declaration in bytes, always `1`}
    @Override
    public int size() {
        return 1;
    }

    /// {@return the byte array value of the byte data declaration}
    @Override
    public byte[] data() {
        return new byte[] { this.value };
    }
}
