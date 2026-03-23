package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for displaying the problems detected in the loaded DLX program.
public final class ProblemsView implements ViewPart {

    /// Loads the problems view from the FXML file.
    /// 
    /// @return The configured problems view with the loaded content.
    public static ProblemsView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = ProblemsView.class.getResource("ProblemsView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load ProblemsView FXML", ex);
        }

        final var controller = fxmlLoader.<ProblemsController>getController();

        // Create and return the view
        return new ProblemsView(controller);
    }

    private final ProblemsController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("problems.title"));

    private ProblemsView(final ProblemsController controller) {
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
