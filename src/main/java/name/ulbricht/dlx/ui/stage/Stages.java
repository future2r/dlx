package name.ulbricht.dlx.ui.stage;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/// Stage utilities.
public final class Stages {

    private static List<Image> stageIcons;

    /// Initializes the icons of the given stage with the application icons.
    /// 
    /// @param stage the stage to initialize the icons for, must not be `null`
    public static void initStageIcons(final Stage stage) {
        requireNonNull(stage);

        if (stageIcons == null)
            stageIcons = IntStream.of(16, 32, 48, 64, 128, 256)
                    .mapToObj("/name/ulbricht/dlx/ui/image/application_%d.png"::formatted)
                    .map(Stages.class::getResource)
                    .map(URL::toString)
                    .map(Image::new)
                    .toList();

        stage.getIcons().addAll(stageIcons);
    }

    /// Creates a new dialog.
    ///
    /// @param <R>        the result type of the dialog
    /// @param owner      the owner window of the dialog, may be `null`
    /// @param title      the dialog title, may be `null`
    /// @param dialogPane the dialog pane to be shown in the dialog, must not be
    ///                   `null`
    /// @return a new dialog
    public static <R> Dialog<R> createDialog(final Window owner, final String title, final DialogPane dialogPane) {
        requireNonNull(dialogPane);

        // Create and configure the dialog
        final var dialog = new Dialog<R>();
        Optional.ofNullable(owner).ifPresent(dialog::initOwner);
        Optional.ofNullable(title).ifPresent(dialog::setTitle);

        // Set the dialog pane to the dialog
        dialog.setDialogPane(dialogPane);

        return dialog;
    }

    /// Restores the window state of the given stage from a previously saved
    /// [WindowState]. The position and size are validated against the currently
    /// available screens. If the saved bounds do not overlap any screen, the restore
    /// is skipped and JavaFX defaults apply. Width and height are clamped so that
    /// the window does not exceed the visual bounds of the target screen.
    ///
    /// @param stage       the stage to restore, must not be `null`
    /// @param windowState the saved window state, must not be `null`
    public static void restoreWindowState(final Stage stage, final WindowState windowState) {
        requireNonNull(stage);
        requireNonNull(windowState);

        final var bounds = windowState.bounds();

        // Check if the saved bounds overlap any current screen
        final var screens = Screen.getScreensForRectangle(bounds);
        if (screens.isEmpty())
            return;

        // Clamp dimensions to the visual bounds of the target screen
        final var screenBounds = screens.getFirst().getVisualBounds();
        final var width = Math.min(bounds.getWidth(), screenBounds.getWidth());
        final var height = Math.min(bounds.getHeight(), screenBounds.getHeight());

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(width);
        stage.setHeight(height);

        if (windowState.maximized())
            stage.setMaximized(true);
    }

    /// Sets up listeners on the given stage that track position and size changes
    /// and save the window state when the window is hiding. The non-maximized bounds
    /// are tracked so that the normal window geometry is preserved even when the
    /// window is closed in a maximized state.
    ///
    /// @param stage  the stage to track, must not be `null`
    /// @param onSave called with the current [WindowState] when the window is
    ///               hiding, must not be `null`
    public static void initWindowStatePersistence(final Stage stage, final Consumer<WindowState> onSave) {
        requireNonNull(stage);
        requireNonNull(onSave);

        final var bounds = new SimpleObjectProperty<>(
                new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));

        final Runnable updateBounds = () -> {
            if (!stage.isMaximized() && stage.isShowing())
                bounds.set(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
        };

        stage.xProperty().subscribe(_ -> updateBounds.run());
        stage.yProperty().subscribe(_ -> updateBounds.run());
        stage.widthProperty().subscribe(_ -> updateBounds.run());
        stage.heightProperty().subscribe(_ -> updateBounds.run());

        stage.addEventHandler(WindowEvent.WINDOW_HIDING,
                _ -> onSave.accept(new WindowState(bounds.get(), stage.isMaximized())));
    }

    /// Private constructor to prevent instantiation .
    private Stages() {
    }
}
