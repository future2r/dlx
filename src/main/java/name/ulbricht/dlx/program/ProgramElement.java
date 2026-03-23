package name.ulbricht.dlx.program;

/// Represents a program element in the program.
public sealed interface ProgramElement permits DataDeclaration, InstructionCall {

    /// {@return the optional label of the program element}
    String label();

    /// {@return the size of the program element in bytes}
    int size();

    /// {@return the encoded bytes of this program element}
    byte[] encode();
}
