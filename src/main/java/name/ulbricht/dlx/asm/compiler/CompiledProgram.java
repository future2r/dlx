package name.ulbricht.dlx.asm.compiler;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of compiling a DLX assembly source file.
///
/// @param id          a unique identifier for the compiled program
/// @param program     the assembled binary (data section followed by code section),
///                    or `null` if compilation produced errors
/// @param entryPoint  the byte offset where the code section begins (i.e. the data
///                    section size); meaningful only when `program` is non-null
/// @param diagnostics a list of diagnostics produced during compilation
public record CompiledProgram(UUID id, byte[] program, int entryPoint, List<Diagnostic> diagnostics) {

    /// Validates and defensively copies all fields.
    public CompiledProgram {
        requireNonNull(id);
        requireNonNull(diagnostics);

        if (program != null) {
            program = Arrays.copyOf(program, program.length);
        }
        diagnostics = List.copyOf(diagnostics);
    }

    /// {@return true if the compiler produced any errors, false otherwise}
    public boolean hasErrors() {
        return this.diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }
}
