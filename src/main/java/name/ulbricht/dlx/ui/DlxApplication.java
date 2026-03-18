package name.ulbricht.dlx.ui;

import java.net.URL;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.main.MainView;

/// The main application class, responsible for launching the
/// JavaFX application.
public final class DlxApplication extends Application {

    /// The entry point of the JavaFX application, which sets up the main stage
    /// and scene.
    @Override
    public void start(final Stage primaryStage) throws Exception {
        // Load the main view
        final var root = MainView.load();

        // Create the scene with the loaded main view
        final var scene = new Scene(root);

        // Configure the primary stage
        primaryStage.setScene(scene);
        primaryStage.setTitle(Messages.getString("mainStage.title"));
        primaryStage.getIcons().addAll(
                IntStream.of(16, 32, 48, 64, 128, 256)
                        .mapToObj(size -> "/name/ulbricht/dlx/ui/image/application_%d.png".formatted(size))
                        .map(getClass()::getResource)
                        .map(URL::toString)
                        .map(Image::new)
                        .toList());

        // Show the primary stage
        primaryStage.show();
    }
}
