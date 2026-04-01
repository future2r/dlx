package name.ulbricht.dlx.ui.stage;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import name.ulbricht.dlx.config.WindowState;

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

    /// Gets the current window state of the given stage.
    /// 
    /// @param stage the stage to get the window state from, must not be `null`
    /// @return the current window state of the given stage
    public static WindowState getWindowState(final Stage stage) {
        requireNonNull(stage);

        if (stage.isMaximized())
            return new WindowState(true, Double.NaN, Double.NaN, Double.NaN, Double.NaN);

        return new WindowState(false, stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    /// Restores the window state of the given stage to the given window state.
    /// 
    /// @param stage       the stage to restore the window state for, must not be
    ///                    `null`
    /// @param windowState the window state to restore, must not be `null`
    public static void restoreWindowState(final Stage stage, final WindowState windowState) {
        requireNonNull(stage);
        requireNonNull(windowState);

        if (windowState.maximized()) {
            stage.setMaximized(true);
        } else {
            final var rect = new Rectangle2D(windowState.x(), windowState.y(), windowState.width(),
                    windowState.height());
            final var screens = Screen.getScreensForRectangle(rect);
            if (screens.isEmpty())
                return;

            final var screenBounds = screens.getFirst().getVisualBounds();
            stage.setX(rect.getMinX());
            stage.setY(rect.getMinY());
            stage.setWidth(Math.min(rect.getWidth(), screenBounds.getWidth() - rect.getMinX()));
            stage.setHeight(Math.min(rect.getHeight(), screenBounds.getHeight() - rect.getMinY()));
        }
    }

    /// Private constructor to prevent instantiation.
    private Stages() {
    }
}
