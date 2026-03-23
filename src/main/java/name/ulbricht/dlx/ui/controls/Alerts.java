package name.ulbricht.dlx.ui.controls;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
        alert.setContentText(msg);

        return alert;
    }

    private Alerts() {
        // Private constructor to prevent instantiation.
    }
}
