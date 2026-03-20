package name.ulbricht.dlx.compiler;

/// Represents a memory reservation data declaration in the program.
/// 
/// @param label the optional label of the data declaration
/// @param size  the size of the memory reservation in bytes
public record SpaceDataDeclaration(String label, int size) implements DataDeclaration {

    /// {@return the byte array value of the memory reservation data declaration}
    @Override
    public byte[] data() {
        return new byte[this.size];
    }
}
