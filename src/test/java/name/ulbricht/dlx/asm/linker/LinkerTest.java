package name.ulbricht.dlx.asm.linker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import name.ulbricht.dlx.asm.Diagnostic;

@DisplayName("Linker")
final class LinkerTest {

    @Nested
    @DisplayName("single file (no includes)")
    final class SingleFile {

        @Test
        @DisplayName("master with no includes yields one unit")
        void noIncludes() {
            final var masterId = UUID.randomUUID();
            final var linked = new Linker().link(masterId, null,
                    """
                    .data
                    x: .word 42
                    """, BufferProvider.NONE);

            assertEquals(1, linked.units().size());
            assertEquals(masterId, linked.units().getFirst().id());
            assertEquals(masterId, linked.masterId());
            assertFalse(linked.linesByUnit().isEmpty());
            assertTrue(linked.lineSourceIds().stream().allMatch(masterId::equals));
            assertNoErrors(linked);
        }

        @Test
        @DisplayName("unsaved master with no includes works")
        void unsavedMasterNoIncludes() {
            final var linked = new Linker().link(UUID.randomUUID(), null, "addi r1, r0, 1\n",
                    BufferProvider.NONE);
            assertNoErrors(linked);
        }
    }

    @Nested
    @DisplayName("happy path")
    final class HappyPath {

        @Test
        @DisplayName("master with one include yields two units and spliced lines")
        void singleInclude(@TempDir final Path dir) throws IOException {
            final var utils = dir.resolve("utils.s");
            Files.writeString(utils, ".data\nmsg: .asciiz \"hi\"\n");

            final var masterPath = dir.resolve("main.s");
            final var masterSource = """
                    .include "utils.s"
                    .text
                    addi r1, r0, 1
                    """;
            Files.writeString(masterPath, masterSource);

            final var masterId = UUID.randomUUID();
            final var linked = new Linker().link(masterId, masterPath, masterSource, BufferProvider.NONE);

            assertNoErrors(linked);
            assertEquals(2, linked.units().size(), "master + included file");
            assertEquals(masterId, linked.units().getFirst().id());

            final var childUnit = linked.units().get(1);
            assertEquals(utils.toRealPath(), childUnit.path());

            // The .include line must NOT appear in the linked output; child
            // lines must appear in its place with the child's sourceId.
            final var sourceIds = linked.lineSourceIds();
            assertTrue(sourceIds.contains(childUnit.id()),
                    "child unit lines must appear in the linked stream");
            assertTrue(sourceIds.contains(masterId),
                    "master text-segment lines must appear too");
        }
    }

    @Nested
    @DisplayName("error cases")
    final class Errors {

        @Test
        @DisplayName("absolute include path is rejected")
        void absolutePathRejected(@TempDir final Path dir) throws IOException {
            final var masterPath = dir.resolve("main.s");
            // Build a platform-appropriate absolute path so the test is portable
            // across Windows (drive letters) and POSIX (leading /).
            final var absolute = dir.resolve("anywhere.s").toAbsolutePath().toString().replace("\\", "/");
            final var source = ".include \"" + absolute + "\"\n";
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            assertSingleError(linked, "Absolute include paths are not supported");
        }

        @Test
        @DisplayName("missing file is reported")
        void missingFile(@TempDir final Path dir) throws IOException {
            final var masterPath = dir.resolve("main.s");
            final var source = ".include \"missing.s\"\n";
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            assertSingleError(linked, "Cannot find include file");
        }

        @Test
        @DisplayName("include from unsaved master is rejected")
        void unsavedMasterCannotInclude() {
            final var source = ".include \"utils.s\"\n";
            final var linked = new Linker().link(UUID.randomUUID(), null, source, BufferProvider.NONE);

            assertSingleError(linked, "save this file first");
        }

        @Test
        @DisplayName("cyclic include is detected")
        void cyclicInclude(@TempDir final Path dir) throws IOException {
            final var a = dir.resolve("a.s");
            final var b = dir.resolve("b.s");
            Files.writeString(a, ".include \"b.s\"\n");
            Files.writeString(b, ".include \"a.s\"\n");

            final var masterSource = Files.readString(a);
            final var linked = new Linker().link(UUID.randomUUID(), a, masterSource, BufferProvider.NONE);

            final var cycleErrors = linked.diagnostics().stream()
                    .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
                    .filter(d -> d.message().contains("Cyclic include"))
                    .toList();
            assertEquals(1, cycleErrors.size(), "exactly one cycle diagnostic expected");
        }

        @Test
        @DisplayName("malformed include directive is reported")
        void malformedInclude(@TempDir final Path dir) throws IOException {
            final var masterPath = dir.resolve("main.s");
            final var source = ".include\n"; // no path operand
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            assertSingleError(linked, "Malformed .include");
        }

        @Test
        @DisplayName("diagnostic is anchored at the include directive token")
        void diagnosticPosition(@TempDir final Path dir) throws IOException {
            final var masterPath = dir.resolve("main.s");
            final var source = """
                    .data
                    .include "missing.s"
                    """;
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            final var err = linked.diagnostics().stream()
                    .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
                    .findFirst().orElseThrow();
            assertEquals(1, err.pos().line(), "include is on the second line");
            assertEquals(0, err.pos().column(), ".include starts at column 0");
        }
    }

    @Nested
    @DisplayName("nested includes")
    final class Nested3 {

        @Test
        @DisplayName("A -> B -> C resolves all three units")
        void chain(@TempDir final Path dir) throws IOException {
            final var c = dir.resolve("c.s");
            final var b = dir.resolve("b.s");
            final var a = dir.resolve("a.s");
            Files.writeString(c, ".text\nnop\n");
            Files.writeString(b, ".include \"c.s\"\n");
            Files.writeString(a, ".include \"b.s\"\n");

            final var linked = new Linker().link(UUID.randomUUID(), a, Files.readString(a), BufferProvider.NONE);

            assertNoErrors(linked);
            assertEquals(3, linked.units().size());
        }
    }

    @Nested
    @DisplayName("repeated includes")
    final class Repeated {

        @Test
        @DisplayName("same file included twice is one unit, two splice points")
        void includedTwice(@TempDir final Path dir) throws IOException {
            final var shared = dir.resolve("shared.s");
            // Single-line content (no trailing newline) so each inclusion contributes
            // exactly one entry to lineSourceIds, making the splice count obvious.
            Files.writeString(shared, "nop");

            final var masterPath = dir.resolve("main.s");
            final var source = """
                    .include "shared.s"
                    .include "shared.s"
                    """;
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            assertNoErrors(linked);
            assertEquals(2, linked.units().size(), "master + one shared unit");

            final var sharedId = linked.units().get(1).id();
            final var sharedLineCount = linked.lineSourceIds().stream().filter(sharedId::equals).count();
            assertEquals(2L, sharedLineCount, "shared file (one line) spliced twice");
        }
    }

    @Nested
    @DisplayName("buffer provider")
    final class Buffers {

        @Test
        @DisplayName("live buffer overrides on-disk content")
        void liveBufferWins(@TempDir final Path dir) throws IOException {
            final var utils = dir.resolve("utils.s");
            Files.writeString(utils, "; stale on-disk content\n");
            final var canonical = utils.toRealPath();

            final var liveContent = ".text\nnop\n";
            final Map<Path, String> buffers = new HashMap<>();
            buffers.put(canonical, liveContent);

            final var masterPath = dir.resolve("main.s");
            final var source = ".include \"utils.s\"\n";
            Files.writeString(masterPath, source);

            final var linked = new Linker().link(UUID.randomUUID(), masterPath, source,
                    p -> Optional.ofNullable(buffers.get(p)));

            assertNoErrors(linked);
            final var includedUnit = linked.units().get(1);
            assertEquals(liveContent, includedUnit.source());
        }
    }

    @Nested
    @DisplayName("LinkedProgram.singleUnit")
    final class SingleUnitFactory {

        @Test
        @DisplayName("wraps a TokenizedProgram with matching sourceIds")
        void wrapsTokenized() {
            final var masterId = UUID.randomUUID();
            final var linked = new Linker().link(masterId, null, "addi r1, r0, 1\n", BufferProvider.NONE);

            final var tokenized = linked.units().getFirst().tokenized();
            final var wrapped = LinkedProgram.singleUnit(tokenized);

            assertEquals(tokenized.id(), wrapped.masterId());
            assertEquals(1, wrapped.units().size());
            assertEquals(wrapped.linesByUnit().size(), wrapped.lineSourceIds().size());
            assertTrue(wrapped.lineSourceIds().stream().allMatch(tokenized.id()::equals));
        }
    }

    @Nested
    @DisplayName("SourceUnit identity")
    final class UnitIdentity {

        @Test
        @DisplayName("included file UUID is deterministic across runs")
        void deterministicUuid(@TempDir final Path dir) throws IOException {
            final var utils = dir.resolve("utils.s");
            Files.writeString(utils, ".text\nnop\n");

            final var masterPath = dir.resolve("main.s");
            final var source = ".include \"utils.s\"\n";
            Files.writeString(masterPath, source);

            final var firstRun = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);
            final var secondRun = new Linker().link(UUID.randomUUID(), masterPath, source, BufferProvider.NONE);

            // Different master ids, same included file id
            assertNotEquals(firstRun.masterId(), secondRun.masterId());
            assertEquals(firstRun.units().get(1).id(), secondRun.units().get(1).id());
        }

        @Test
        @DisplayName("master with no path keeps null path on its SourceUnit")
        void masterWithoutPathHasNullUnitPath() {
            final var linked = new Linker().link(UUID.randomUUID(), null, "nop\n", BufferProvider.NONE);

            final var masterUnit = linked.units().getFirst();
            assertNotNull(masterUnit);
            assertNull(masterUnit.path());
        }
    }

    private static void assertNoErrors(final LinkedProgram linked) {
        final var errors = linked.diagnostics().stream()
                .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
                .toList();
        assertTrue(errors.isEmpty(), () -> "expected no errors, got: " + errors);
    }

    private static void assertSingleError(final LinkedProgram linked, final String messageFragment) {
        final var errors = linked.diagnostics().stream()
                .filter(d -> d.severity() == Diagnostic.Severity.ERROR)
                .toList();
        assertEquals(1, errors.size(), () -> "expected exactly one error, got: " + errors);
        final var msg = errors.getFirst().message();
        assertTrue(msg.contains(messageFragment),
                () -> "expected error to contain '" + messageFragment + "', got: " + msg);
    }
}
