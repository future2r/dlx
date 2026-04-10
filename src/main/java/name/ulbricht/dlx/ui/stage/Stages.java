package name.ulbricht.dlx.ui.stage;

import static java.util.Objects.requireNonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import name.ulbricht.dlx.config.StageState;
import name.ulbricht.dlx.ui.view.View;

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
    /// @param <R>   the result type of the dialog
    /// @param owner the owner window of the dialog, may be `null`
    /// @param view  the view to set as the dialog pane, must not be `null`
    /// @return a new dialog
    public static <R> Dialog<R> createDialog(final Window owner, final View<? extends DialogPane, ?> view) {
        requireNonNull(view);

        // Create and configure the dialog
        final var dialog = new Dialog<R>();
        Optional.ofNullable(owner).ifPresent(dialog::initOwner);
        dialog.titleProperty().bind(view.titleProperty());

        // Set the dialog pane to the dialog
        dialog.setDialogPane(view.getRoot());

        return dialog;
    }

    /// Retrieves the current state of the given stage.
    /// 
    /// @param stage the stage to retrieve the state from, must not be `null`
    /// @return the current state of the stage
    public static StageState getStageState(final Stage stage) {
        requireNonNull(stage);

        if (stage.isMaximized())
            return new StageState(true, 0, 0, 0, 0);

        return new StageState(false, (int) stage.getX(), (int) stage.getY(), (int) stage.getWidth(),
                (int) stage.getHeight());
    }

    /// Restores the state of the given stage.
    /// 
    /// @param stage the stage to restore the state for, must not be `null`
    /// @param state the state to restore, must not be `null`
    public static void restoreStageState(final Stage stage, final StageState state) {
        requireNonNull(stage);
        requireNonNull(state);

        if (state.maximized())
            stage.setMaximized(true);
        else {
            // Set the stage position only if it is on a valid screen, otherwise use the
            // default position
            final var x = state.x();
            final var y = state.y();
            Screen.getScreensForRectangle(x, y, 1, 1).stream().findFirst().ifPresent(_ -> {
                stage.setX(x);
                stage.setY(y);
            });

            final var width = state.width();
            final var height = state.height();
            if (width != StageState.UNDEFINED)
                stage.setWidth(width);
            if (height != StageState.UNDEFINED)
                stage.setHeight(height);
        }
    }

    /// Private constructor to prevent instantiation.
    private Stages() {
    }
}
