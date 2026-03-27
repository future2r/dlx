package name.ulbricht.dlx.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.view.main.MainController;
import name.ulbricht.dlx.ui.view.main.MainView;

/// The main application class, responsible for launching the
/// JavaFX application.
public final class DlxApplication extends Application {

    /// Creates a new DLX application instance.
    public DlxApplication() {
    }

    /// The entry point of the JavaFX application, which sets up the main stage
    /// and scene.
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // Load the main view
        final var fxmlLoader = MainView.load();

        // Create the scene with the loaded main view
        final var scene = new Scene(fxmlLoader.getRoot());

        // Configure the primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle(Messages.getString("primaryStage.title"));
        Stages.initStageIcons(primaryStage);

        // Forward window events to the controller
        final var controller = fxmlLoader.<MainController>getController();
        primaryStage.addEventHandler(WindowEvent.WINDOW_SHOWN, controller::windowShown);
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, controller::windowCloseRequest);

        // Show the primary stage
        primaryStage.show();

        // Notify the preloader that the application is shown
        Platform.runLater(() -> notifyPreloader(new AppShownNotification()));
    }
}
