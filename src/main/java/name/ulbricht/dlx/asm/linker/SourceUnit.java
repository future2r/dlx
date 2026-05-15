package name.ulbricht.dlx.asm.linker;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.util.UUID;

import name.ulbricht.dlx.asm.lexer.TokenizedProgram;

/// One resolved source file inside a [LinkedProgram].
///
/// A [LinkedProgram] always contains the master unit as its first entry plus
/// one entry per distinct included file. The same physical file included more
/// than once along different paths is represented by a single [SourceUnit];
/// its tokens are spliced into the linked stream once per inclusion.
///
/// @param id        stable identifier of the unit; used as
///                  [name.ulbricht.dlx.asm.Diagnostic#sourceId] for every
///                  diagnostic that originates from this file
/// @param path      the file's canonical path, or `null` if this is the master
///                  unit and the master file has not been saved yet
/// @param source    the source text the unit was tokenised from (live buffer
///                  content if available, otherwise the on-disk content)
/// @param tokenized the lexer output for this unit
public record SourceUnit(UUID id, Path path, String source, TokenizedProgram tokenized) {

    /// Validates the record components. `path` is allowed to be `null` for an
    /// unsaved master; all other components are required.
    public SourceUnit {
        requireNonNull(id, "id must not be null");
        requireNonNull(source, "source must not be null");
        requireNonNull(tokenized, "tokenized must not be null");
    }
}
