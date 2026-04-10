package name.ulbricht.dlx.ui.view.main;

import static java.util.Objects.requireNonNull;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.ViewServices;
import name.ulbricht.dlx.ui.view.Views;

/// The main view of the application, defined in an FXML file.
public final class MainView implements View<Parent, MainViewModel> {

    /// The window identifier used for persisting the main window state.
    static final String STAGE_ID = "main";

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

        // Create the scene
        final var scene = new Scene(view.getRoot());

        // Configure the stage
        if (Platform.isSupported(ConditionalFeature.UNIFIED_WINDOW)) {
            stage.initStyle(StageStyle.UNIFIED);
            scene.setFill(Color.TRANSPARENT);
        }
        stage.setScene(scene);
        Stages.initStageIcons(stage);
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.titleProperty().bind(view.titleProperty());

        ViewServices.userPreferences().getStageState(STAGE_ID)
                .ifPresentOrElse(state -> Platform.runLater(() -> Stages.restoreStageState(stage, state)),
                        () -> stage.centerOnScreen());

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
