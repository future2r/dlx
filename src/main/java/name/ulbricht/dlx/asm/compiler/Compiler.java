package name.ulbricht.dlx.asm.compiler;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.parser.ParsedElement;
import name.ulbricht.dlx.asm.parser.ParsedInstruction;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.util.TextPosition;

/// DLX assembler compiler.
public final class Compiler {

    private List<Diagnostic> diagnostics;

    /// Creates a new instance.
    public Compiler() {
    }

    /// Compiles the given parsed program into a compiled program.
    /// 
    /// @param parsed the parsed program to compile
    /// @return the compiled program
    public CompiledProgram compile(final ParsedProgram parsed) {
        requireNonNull(parsed);

        this.diagnostics = new ArrayList<>();

        // Placeholder for the compilation logic

        // Return dummy for now
        addError("Dummy error message", new ParsedInstruction(new TextPosition(0, 0), null, "opcode", List.of()), 0);
        return new CompiledProgram(parsed.id(), this.diagnostics);
    }

    private void addError(final String msg, final ParsedElement element, final int len) {
        this.diagnostics.add(new Diagnostic(Diagnostic.Stage.COMPILING, Diagnostic.Severity.ERROR,
                new TextPosition(element.pos().line(), element.pos().column(), len), msg));
    }
}
