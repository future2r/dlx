package name.ulbricht.dlx.compiler;

/// Represents a word data declaration in the program.
/// 
/// @param label the optional label of the data declaration
/// @param value the integer value of the word data declaration
public record WordDataDeclaration(String label, int value) implements DataDeclaration {

    /// {@return the size of the data declaration in bytes, always `4`}
    @Override
    public int size() {
        return 4;
    }

    /// {@return the byte array value of the word data declaration}
    @Override
    public byte[] data() {
        return new byte[] {
                (byte) (this.value >> 24),
                (byte) (this.value >> 16),
                (byte) (this.value >> 8),
                (byte) this.value };
    }
}
