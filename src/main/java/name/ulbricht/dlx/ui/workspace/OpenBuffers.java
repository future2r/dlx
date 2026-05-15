package name.ulbricht.dlx.ui.workspace;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javafx.util.Subscription;
import name.ulbricht.dlx.asm.linker.BufferProvider;
import name.ulbricht.dlx.ui.view.editor.EditorViewModel;

/// Workspace-wide registry of open editor view models, exposed as a
/// [BufferProvider] so the linker can prefer live editor content over the
/// on-disk version when resolving an `.include` directive.
///
/// When any tracked editor's source changes, every other editor is asked to
/// [EditorViewModel#refresh] so masters whose included files are open and
/// edited stay in sync without an explicit save.
public final class OpenBuffers implements BufferProvider {

    // LinkedHashMap so iteration order matches attach order — primarily a
    // determinism choice; OpenBuffers' behaviour does not depend on order.
    private final Map<EditorViewModel, Subscription> editors = new LinkedHashMap<>();

    /// Creates a new, empty registry.
    public OpenBuffers() {
    }

    /// Registers an editor so its live source becomes visible to the linker
    /// and edits in it trigger re-links of other editors.
    ///
    /// @param editor the editor to register
    public void add(final EditorViewModel editor) {
        requireNonNull(editor, "editor must not be null");
        if (this.editors.containsKey(editor)) {
            return;
        }
        // Runnable subscribe form fires only on invalidation, not initially —
        // adding an editor must not synchronously trigger refresh on the rest.
        this.editors.put(editor, editor.sourceProperty().subscribe(() -> broadcast(editor)));
    }

    /// Unregisters an editor. After removal, every remaining editor is asked to
    /// refresh — masters that depended on this editor's live buffer must now
    /// fall back to the on-disk content (or to a different open buffer for the
    /// same file, if there is one).
    ///
    /// @param editor the editor to remove
    public void remove(final EditorViewModel editor) {
        requireNonNull(editor, "editor must not be null");
        final var sub = this.editors.remove(editor);
        if (sub == null) {
            return;
        }
        sub.unsubscribe();
        broadcast(editor);
    }

    @Override
    public Optional<String> sourceFor(final Path canonical) {
        requireNonNull(canonical, "canonical must not be null");
        for (final var editor : this.editors.keySet()) {
            final var file = editor.getFile();
            if (file == null) {
                continue;
            }
            final Path editorCanonical;
            try {
                editorCanonical = file.toRealPath();
            } catch (final IOException _) {
                continue;
            }
            if (editorCanonical.equals(canonical)) {
                return Optional.ofNullable(editor.getSource());
            }
        }
        return Optional.empty();
    }

    /// Notifies every editor other than the originator that buffer state has
    /// changed, so any of them that include the originator can re-link.
    private void broadcast(final EditorViewModel originator) {
        for (final var editor : this.editors.keySet()) {
            if (editor != originator) {
                editor.refresh();
            }
        }
    }
}
