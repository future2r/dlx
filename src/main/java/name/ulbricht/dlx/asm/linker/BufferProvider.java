package name.ulbricht.dlx.asm.linker;

import java.nio.file.Path;
import java.util.Optional;

/// Resolves the live source text for an included file, allowing the linker to
/// prefer the in-memory buffer of an open dirty editor over the on-disk
/// content.
///
/// Implementations live in the UI layer; the linker depends only on this
/// interface so the `asm.*` packages remain free of UI dependencies. The
/// canonical [#NONE] instance disables buffer lookup entirely and is used in
/// tests and headless contexts.
@FunctionalInterface
public interface BufferProvider {

    /// A provider that never has a live buffer. The linker falls back to disk
    /// for every include.
    BufferProvider NONE = _ -> Optional.empty();

    /// Returns the live source text for the file at the given canonical path,
    /// if any.
    ///
    /// @param canonical the canonical (real, normalised) path of the file the
    ///                  linker is about to load
    /// @return the live buffer content, or empty if no open editor has the
    ///         file or it has no unsaved changes
    Optional<String> sourceFor(Path canonical);
}
