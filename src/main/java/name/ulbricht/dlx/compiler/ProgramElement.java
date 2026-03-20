package name.ulbricht.dlx.compiler;

public sealed interface ProgramElement permits DataDeclaration, InstructionCall {

    /// {@return the optional label of the program element}
    String label();

    /// {@return the size of the program element in bytes}
    int size();
}
