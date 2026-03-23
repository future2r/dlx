package name.ulbricht.dlx.ui.view.editor;

import static java.util.Objects.requireNonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/// View model for the editor view.
public final class EditorViewModel {

    private final ReadOnlyObjectWrapper<Path> file = new ReadOnlyObjectWrapper<>();

    private final StringProperty source = new SimpleStringProperty();

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();

    /// Creates a new editor view model instance.
    public EditorViewModel() {
    }

    /// {@return a read-only property representing the currently loaded file, or
    /// `null` if no file is loaded}
    public ReadOnlyObjectProperty<Path> fileProperty() {
        return this.file.getReadOnlyProperty();
    }

    /// {@return the currently loaded file, or `null` if no file is loaded}
    public Path getFile() {
        return fileProperty().get();
    }

    /// {@return a property representing the source code}
    public StringProperty sourceProperty() {
        return this.source;
    }

    /// {@return the source code}
    public String getSource() {
        return sourceProperty().get();
    }

    /// Sets the source code.
    /// 
    /// @param source the new source code
    void setSource(final String source) {
        this.source.set(source);
    }

    /// {@return a read-only property indicating whether the current file has
    /// unsaved changes}
    public ReadOnlyBooleanProperty dirtyProperty() {
        return this.dirty.getReadOnlyProperty();
    }

    /// {@return whether the current file has unsaved changes}
    public boolean isDirty() {
        return dirtyProperty().get();
    }

    /// Creates a new file with example source code.
    void newFile() throws IOException {
        final var fileName = "example.dlx";
        final String example;
        try (var in = getClass().getResourceAsStream(fileName)) {
            if (in == null)
                throw new FileNotFoundException(fileName);
            example = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        this.setSource(example);
        this.dirty.set(false);
        this.file.set(null);
    }

    /// Loads the content of the specified file into the editor.
    /// 
    /// @param file the file to load
    void loadFile(final Path file) throws IOException {
        requireNonNull(file);

        this.setSource(Files.readString(file));
        this.dirty.set(false);
        this.file.set(file);
    }
}
