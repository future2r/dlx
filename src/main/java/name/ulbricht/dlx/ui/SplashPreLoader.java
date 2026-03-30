package name.ulbricht.dlx.ui;

import static java.util.Objects.requireNonNull;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.stage.Stages;

/// Shows the splash screen using the preloader feature. The fully specified
/// name of this class must be set to the system property `javafx.preloader` in
/// order to get found.
public final class SplashPreLoader extends Preloader {

    private static Stage preloaderStage;

    /// Creates a new instance.
    public SplashPreLoader() {
    }

    @Override
    public void start(final Stage stage) throws Exception {
        requireNonNull(stage);

        // Create the scene programmatically, this is faster than loading from FXML
        final var imageUrl = SplashPreLoader.class.getResource("/name/ulbricht/dlx/ui/image/splash.png");
        final var imageView = new ImageView(new Image(imageUrl.toString()));
        final var root = new StackPane(imageView);

        // Create the scene
        final var scene = new Scene(root);
        scene.setFill(null);

        // Configure the stage
        stage.initStyle(StageStyle.UNDECORATED);
        Stages.initStageIcons(stage);
        stage.setTitle(Messages.getString("splash.title"));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);

        // Show the stage
        stage.show();

        // Save the stage reference for later hiding
        preloaderStage = stage;
    }

    @Override
    public void handleApplicationNotification(final PreloaderNotification info) {
        if (info instanceof final AppShownNotification _) {
            preloaderStage.hide();
        }
    }
}
