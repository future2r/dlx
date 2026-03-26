package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.util.TextPosition;

/// View for the editor.
public final class EditorView implements ViewPart<EditorViewModel> {

    /// Loads the editor view from the FXML file.
    /// 
    /// @return The editor view with the loaded content.
    public static EditorView load() {
        try {
            return load(null);
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to create an editor for an empty source file", ex);
        }
    }

    /// Loads the editor view from the FXML file.
    /// 
    /// @param file the file to load into the editor, or null for an empty editor
    /// @return The configured editor view with the loaded content.
    /// @throws IOException if an I/O error occurs while loading the file
    public static EditorView load(final Path file) throws IOException {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = EditorView.class.getResource("EditorView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load EditorView FXML", ex);
        }

        final var controller = fxmlLoader.<EditorController>getController();

        if (file != null)
            controller.getViewModel().loadFile(file);
        else
            controller.getViewModel().newFile();

        // Create and return the view
        return new EditorView(controller);
    }

    private final EditorController controller;
    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();

    private EditorView(final EditorController controller) {
        this.controller = requireNonNull(controller);

        final var vm = this.controller.getViewModel();

        // The plain name: file name or "Untitled"
        this.name.bind(vm.fileProperty().map(file -> file.getFileName().toString())
                .orElse(Messages.getString("editor.title.untitled")));

        // The tab title: name prefixed with a dot when dirty
        this.title.bind(Bindings.createStringBinding(() -> vm.isDirty() ? "\u25CF " + this.name.get() : this.name.get(),
                this.name, vm.dirtyProperty()));

        // Use the full file path as the description
        this.description.bind(vm.fileProperty().map(Path::toString)
                .orElse(Messages.getString("editor.title.untitled")));
    }

    /// {@return a read-only property representing the plain name of the editor
    /// (file name or "Untitled"), without any dirty indicator}
    public ReadOnlyStringProperty nameProperty() {
        return this.name.getReadOnlyProperty();
    }

    /// {@return the plain name of the editor (file name or "Untitled"), without any
    /// dirty indicator}
    public String getName() {
        return nameProperty().get();
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyStringProperty descriptionProperty() {
        return this.description.getReadOnlyProperty();
    }

    @Override
    public Node getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public EditorViewModel getViewModel() {
        return this.controller.getViewModel();
    }

    /// {@return the current edit position in the editor}
    public ReadOnlyObjectProperty<TextPosition> editPositionProperty() {
        return this.controller.editPositionProperty();
    }

    /// {@return the current edit position in the editor}
    public TextPosition getEditPosition() {
        return this.controller.getEditPosition();
    }

    /// {@return a read-only property indicating whether undo is available}
    public ReadOnlyBooleanProperty undoableProperty() {
        return this.controller.undoableProperty();
    }

    /// {@return a read-only property indicating whether redo is available}
    public ReadOnlyBooleanProperty redoableProperty() {
        return this.controller.redoableProperty();
    }

    /// {@return a read-only property indicating whether the editor has a
    /// non-empty selection}
    public ReadOnlyBooleanProperty hasSelectionProperty() {
        return this.controller.hasSelectionProperty();
    }

    /// Undoes the last edit operation.
    public void undo() {
        this.controller.undo();
    }

    /// Redoes the last undone edit operation.
    public void redo() {
        this.controller.redo();
    }

    /// Cuts the current selection to the clipboard.
    public void cut() {
        this.controller.cut();
    }

    /// Copies the current selection to the clipboard.
    public void copy() {
        this.controller.copy();
    }

    /// Pastes the clipboard content at the current caret position.
    public void paste() {
        this.controller.paste();
    }

    /// Requests keyboard focus for the editor.
    public void requestFocus() {
        this.controller.requestFocus();
    }

    /// Shows the specified edit position in the editor.
    ///
    /// @param position the edit position to show
    public void showEditPosition(final TextPosition position) {
        this.controller.showEditPosition(position);
    }

    /// Refreshes the syntax highlighting in the editor.
    public void refreshSyntaxHighlighting() {
        this.controller.refreshSyntaxHighlighting();
    }
}
