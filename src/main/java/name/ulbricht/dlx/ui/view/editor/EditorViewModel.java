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
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/// View model for the editor view.
public final class EditorViewModel {

    private final ReadOnlyObjectWrapper<Path> file = new ReadOnlyObjectWrapper<>();

    private final StringProperty modifiableSource = new SimpleStringProperty();
    private final ReadOnlyStringWrapper source = new ReadOnlyStringWrapper();

    private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();

    /// Creates a new editor view model instance.
    public EditorViewModel() {
        this.source.bind(this.modifiableSource);
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

    StringProperty modifiableSourceProperty() {
        return this.modifiableSource;
    }

    /// {@return a read-only property representing the source code}
    public ReadOnlyStringProperty sourceProperty() {
        return this.source.getReadOnlyProperty();
    }

    /// {@return the source code}
    public String getSource() {
        return sourceProperty().get();
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

    void newFile() throws IOException {
        this.modifiableSource.set(loadExample());
        this.dirty.set(false);
        this.file.set(null);
    }

    void loadFile(final Path file) throws IOException {
        requireNonNull(file);

        this.modifiableSource.set(Files.readString(file));
        this.dirty.set(false);
        this.file.set(file);
    }

    private String loadExample() throws IOException {
        final var fileName = "example.dlx";
        try (var in = getClass().getResourceAsStream(fileName)) {
            if (in == null)
                throw new FileNotFoundException(fileName);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
