package name.ulbricht.dlx.program;

/// Represents a half-word data declaration in the program.
/// 
/// @param label the optional label of the data declaration
/// @param value the integer value of the half-word data declaration
public record HalfWordDataDeclaration(String label, short value) implements DataDeclaration {

    /// {@return the size of the data declaration in bytes, always `2`}
    @Override
    public int size() {
        return 2;
    }

    /// {@return the byte array value of the half-word data declaration}
    @Override
    public byte[] data() {
        return new byte[] {
                (byte) (this.value >> 8),
                (byte) this.value };
    }
}
