package name.ulbricht.dlx.asm.compiler;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of compiling a DLX assembly source file.
/// 
/// 
/// @param id          a unique identifier for the compiled program
/// @param diagnostics a list of diagnostics produced during compilation
public record CompiledProgram(UUID id, List<Diagnostic> diagnostics) {

    /// Validates and defensively copies all lists.
    public CompiledProgram {
        requireNonNull(id);
        requireNonNull(diagnostics);

        diagnostics = List.copyOf(diagnostics);
    }

    /// {@return true if the compiler produced any errors, false otherwise}
    public boolean hasErrors() {
        return this.diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }
}
