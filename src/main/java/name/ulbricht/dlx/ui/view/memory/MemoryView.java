package name.ulbricht.dlx.ui.view.memory;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for displaying the memory state of the DLX simulator.
public final class MemoryView implements ViewPart<MemoryViewModel> {

    /// Loads the memory view from the FXML file.
    /// 
    /// @return The configured memory view with the loaded content.
    public static MemoryView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = MemoryView.class.getResource("MemoryView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load MemoryView FXML", ex);
        }

        final var controller = fxmlLoader.<MemoryController>getController();

        // Create and return the view
        return new MemoryView(controller);
    }

    private final MemoryController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("memory.title"));

    private MemoryView(final MemoryController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringWrapper titleProperty() {
        return this.title;
    }

    @Override
    public Node getRoot() {
        return this.controller.getRoot();
    }
}
