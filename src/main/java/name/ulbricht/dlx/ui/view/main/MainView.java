package name.ulbricht.dlx.ui.view.main;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import name.ulbricht.dlx.ui.i18n.Messages;

/// The main view of the application, defined in an FXML file.
public final class MainView {

    private MainView() {
    }

    /// Loads the main view from the FXML file.
    /// 
    /// @return The configured FXML loader with the loaded content.
    public static FXMLLoader load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = MainView.class.getResource("MainView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load MainView FXML", ex);
        }

        // Return the configured FXML loader with the loaded content
        return fxmlLoader;
    }
}
