package name.ulbricht.dlx.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
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
    public void start(final Stage primaryStage) {
        MainView.show(primaryStage);

        // Notify the preloader that the application is shown
        Platform.runLater(() -> notifyPreloader(new AppShownNotification()));
    }
}
