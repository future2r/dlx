package name.ulbricht.dlx.ui.scene.control;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/// Utility class for creating and managing alert dialogs in the application.
public final class Alerts {

    /// Creates an information alert with the specified owner and message.
    /// 
    /// @param owner the owner window of the alert
    /// @param msg   the message to display in the alert
    /// @return the created Alert instance
    public static Alert info(final Window owner, final String msg) {
        final var alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        return alert;
    }

    /// Creates an error alert with the specified owner and message.
    ///
    /// @param owner the owner window of the alert
    /// @param msg   the message to display in the alert
    /// @return the configured alert, ready to be shown
    public static Alert error(final Window owner, final String msg) {
        final var alert = new Alert(AlertType.ERROR);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.setContentText(msg);

        return alert;
    }

    /// Creates a confirmation alert with Yes, No, and Cancel buttons.
    ///
    /// @param owner   the owner window of the alert
    /// @param message the content message
    /// @return the created Alert instance
    public static Alert confirmation(final Window owner, final String message) {
        final var alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        return alert;
    }

    private Alerts() {
        // Private constructor to prevent instantiation.
    }
}
