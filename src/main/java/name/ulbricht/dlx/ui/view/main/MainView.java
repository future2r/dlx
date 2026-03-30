package name.ulbricht.dlx.ui.view.main;

import java.io.IOException;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.view.ViewResources;

/// The main view of the application, defined in an FXML file.
public final class MainView {

    /// The window identifier used for persisting the main window state.
    static final String WINDOW_ID = "main";

    private MainView() {
    }

    /// Loads the main view, configures the given stage, and shows it.
    ///
    /// @param stage the primary stage to set up and show, must not be `null`
    public static void show(final Stage stage) {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = MainView.class.getResource("MainView.fxml");
        final var fxmlLoader = new javafx.fxml.FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load MainView FXML", ex);
        }

        // Configure the stage
        stage.setScene(new Scene(fxmlLoader.getRoot()));
        Stages.initStageIcons(stage);

        // Restore the saved window state
        ViewResources.userPreferences().getWindowState(WINDOW_ID)
                .ifPresent(ws -> Stages.restoreWindowState(stage, ws));

        // Forward window events to the controller
        final var controller = fxmlLoader.<MainController>getController();
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, controller::windowShown);
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, controller::windowCloseRequest);

        // Show the stage
        stage.show();
    }
}
