package name.ulbricht.dlx.program;

/// Represents a data declaration in the program.
public sealed interface DataDeclaration extends ProgramElement permits ByteDataDeclaration, HalfWordDataDeclaration,
        WordDataDeclaration, SpaceDataDeclaration, AsciiDataDeclaration, AsciiZDataDeclaration {

    /// {@return the data bytes of the declaration}
    byte[] data();

    @Override
    public default byte[] encode() {
        return data();
    }
}
