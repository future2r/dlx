package name.ulbricht.dlx.ui.view.main;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.view.View;

/// The main view of the application, defined in an FXML file.
public final class MainView implements View<Parent, MainController> {

    /// The window identifier used for persisting the main window state.
    static final String WINDOW_ID = "main";

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

        final var controller = fxmlLoader.<MainController>getController();
        final var view = new MainView(controller);

        // Use the provided stage
        Stages.useStage(stage, view);

        // Forward window events to the controller
        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, controller::windowShown);
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, controller::windowCloseRequest);
        stage.addEventHandler(WindowEvent.WINDOW_HIDING, controller::windowHiding);

        // Show the stage
        stage.show();
    }

    private final MainController controller;

    private MainView(final MainController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.controller.titleProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }
}
