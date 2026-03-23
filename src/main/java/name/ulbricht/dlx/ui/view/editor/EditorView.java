package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for the editor.
public final class EditorView implements ViewPart {

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
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper description = new ReadOnlyStringWrapper();

    private EditorView(final EditorController controller) {
        this.controller = requireNonNull(controller);

        // Use the file name as the title
        this.title.bind(this.controller.getViewModel().fileProperty().map(file -> file.getFileName().toString())
                .orElse(Messages.getString("editor.title.untitled")));

        // Use the full file path as the description
        this.description.bind(this.controller.getViewModel().fileProperty().map(Path::toString)
                .orElse(Messages.getString("editor.title.untitled")));
    }

    @Override
    public ReadOnlyStringWrapper titleProperty() {
        return this.title;
    }

    @Override
    public ReadOnlyStringWrapper descriptionProperty() {
        return this.description;
    }

    @Override
    public Node getRoot() {
        return this.controller.getRoot();
    }
}
