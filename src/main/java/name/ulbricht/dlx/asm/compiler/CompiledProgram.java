package name.ulbricht.dlx.asm.compiler;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import name.ulbricht.dlx.asm.Diagnostic;

/// The result of compiling a DLX assembly source file.
///
/// @param id          a unique identifier for the compiled program
/// @param program     the assembled binary (code section at offset 0, followed by
///                    the data section), or an empty array if compilation produced
///                    errors; the CPU program counter always starts at address 0
/// @param diagnostics a list of diagnostics produced during compilation
public record CompiledProgram(UUID id, byte[] program, List<Diagnostic> diagnostics) {

    /// Validates and defensively copies all fields.
    public CompiledProgram {
        requireNonNull(id);
        requireNonNull(program);
        requireNonNull(diagnostics);

        program = Arrays.copyOf(program, program.length);
        diagnostics = List.copyOf(diagnostics);
    }

    /// {@return true if the compiler produced any errors, false otherwise}
    public boolean hasErrors() {
        return this.diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }
}
