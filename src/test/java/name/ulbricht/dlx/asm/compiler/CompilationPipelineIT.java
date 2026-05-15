package name.ulbricht.dlx.asm.compiler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.linker.BufferProvider;
import name.ulbricht.dlx.asm.linker.LinkedProgram;
import name.ulbricht.dlx.asm.linker.Linker;
import name.ulbricht.dlx.asm.parser.Parser;
import name.ulbricht.dlx.io.SourceFile;

@SuppressWarnings("static-method")
@DisplayName("Compilation pipeline integration")
final class CompilationPipelineIT {

        static Stream<Arguments> exampleFiles() {
                return Stream.of(
                                Arguments.argumentSet("uppercase.s",
                                                Path.of("assets/examples/uppercase.s")),
                                Arguments.argumentSet("infinite.s",
                                                Path.of("assets/examples/infinite.s")),
                                Arguments.argumentSet("memcopy.s",
                                                Path.of("assets/examples/memcopy.s")));
        }

        @ParameterizedTest
        @MethodSource("exampleFiles")
        @DisplayName("Example file compiles without errors")
        void compilesWithoutErrors(final Path file) throws IOException {
                final var source = SourceFile.read(file);
                assertNotNull(source);
                assertFalse(source.isBlank(), "Source file should not be empty");

                final var tokenized = new Lexer(LexerMode.ASSEMBLER)
                                .tokenize(UUID.randomUUID(), source);
                assertNotNull(tokenized);

                final var parsed = new Parser().parse(LinkedProgram.singleUnit(tokenized));
                assertNotNull(parsed);
                assertTrue(parsed.diagnostics().isEmpty(),
                                "Parser should produce no diagnostics but got: "
                                                + parsed.diagnostics());

                final var compiled = new Compiler().compile(parsed);
                assertNotNull(compiled);
                assertFalse(compiled.hasErrors(),
                                "Compiler should produce no errors but got: "
                                                + compiled.diagnostics());

                assertTrue(compiled.program().length > 0,
                                "Compiled program should not be empty");
        }

        @Test
        @DisplayName("include_main.s links, parses, and compiles via the full linker pipeline")
        void includeExampleCompiles() throws IOException {
                final var masterPath = Path.of("assets/examples/include_main.s");
                final var source = SourceFile.read(masterPath);

                final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);
                assertTrue(linked.diagnostics().isEmpty(),
                                "Linker should produce no diagnostics but got: " + linked.diagnostics());
                assertTrue(linked.units().size() > 1,
                                "Include should pull in at least one additional source unit");

                final var parsed = new Parser().parse(linked);
                assertTrue(parsed.diagnostics().isEmpty(),
                                "Parser should produce no diagnostics but got: " + parsed.diagnostics());

                final var compiled = new Compiler().compile(parsed);
                assertFalse(compiled.hasErrors(),
                                "Compiler should produce no errors but got: " + compiled.diagnostics());
                assertTrue(compiled.program().length > 0,
                                "Compiled program should not be empty");
        }
}
