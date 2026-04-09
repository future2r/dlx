package name.ulbricht.dlx.ui.view.main;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.ViewServices;
import name.ulbricht.dlx.ui.view.Views;

/// The main view of the application, defined in an FXML file.
public final class MainView implements View<Parent, MainViewModel> {

    /// The window identifier used for persisting the main window state.
    static final String WINDOW_ID = "main";

    /// Loads the main view from the associated FXML file.
    ///
    /// @return the configured main view
    private static MainView load() {
        return new MainView(Views.loadController(MainView.class));
    }

    /// Loads the main view, configures the given stage, and shows it.
    ///
    /// @param stage the primary stage to set up and show, must not be `null`
    public static void show(final Stage stage) {

        final var view = load();
        final var controller = view.controller;
        final var windowState = ViewServices.userPreferences().getWindowState(WINDOW_ID);

        // Create the scene
        final var scene = windowState.isPresent() && !windowState.get().maximized()
                ? new Scene(view.getRoot(), windowState.get().width(), windowState.get().height())
                : new Scene(view.getRoot());

        // Configure the stage
        stage.setScene(scene);
        Stages.initStageIcons(stage);
        stage.titleProperty().bind(view.titleProperty());

        windowState.ifPresent(state -> {
            if (state.maximized())
                stage.setMaximized(true);
            else {
                stage.setX(state.x());
                stage.setY(state.y());
            }
        });

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
