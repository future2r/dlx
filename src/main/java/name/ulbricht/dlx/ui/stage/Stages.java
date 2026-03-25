package name.ulbricht.dlx.ui.stage;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

/// Stage utilities.
public final class Stages {

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

    /// Private constructor to prevent instantiation .
    private Stages() {
    }
}
