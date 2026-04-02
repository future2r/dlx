package name.ulbricht.dlx;

import javafx.application.Application;
import name.ulbricht.dlx.service.Logging;
import name.ulbricht.dlx.ui.DlxApplication;

/// The main class of the application, serving as the entry point.
final class Main {

    /// The main method, which launches the JavaFX application.
    ///
    /// @param args The command-line arguments passed to the application.
    static void main(final String... args) {

        // Initialize logging system
        Logging.initLogging();

        // Launch the JavaFX application
        Application.launch(DlxApplication.class, args);
    }
}
