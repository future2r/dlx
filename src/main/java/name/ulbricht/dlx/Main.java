package name.ulbricht.dlx;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javafx.application.Application;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.service.Services;
import name.ulbricht.dlx.ui.DlxApplication;

/// The main class of the application, serving as the entry point.
final class Main {

    /// The main method, which launches the JavaFX application.
    ///
    /// @param args The command-line arguments passed to the application.
    static void main(final String... args) {

        // Configure logging from bundled properties
        configureLogging();

        // Launch the JavaFX application
        Application.launch(DlxApplication.class, args);
    }

    private static void configureLogging() {
        // Load logging configuration from the bundled properties file
        try (var is = Main.class.getResourceAsStream("logging.properties")) {
            if (is != null)
                LogManager.getLogManager().readConfiguration(is);
        } catch (final Exception _) {
            // Fall back to JVM defaults
        }

        // Apply the log level from the user preferences
        applyLogLevel(Services.userPreferences().getLogLevel());

        // Register for future changes of the log level
        Services.userPreferences().addPreferenceChangeListener(UserPreferences.LOG_LEVEL_PROPERTY, Main::applyLogLevel);
    }

    /// Applies the current log level preference to the 'java.util.logging' root
    /// logger.
    /// 
    /// @param level The log level to apply.
    public static void applyLogLevel(final System.Logger.Level level) {
        final var utilLevel = convertLevel(level);
        final var rootLogger = Logger.getLogger("");
        rootLogger.setLevel(utilLevel);
        for (final var handler : rootLogger.getHandlers())
            handler.setLevel(utilLevel);
    }

    /// Converts a [System.Logger.Level] to a [java.util.logging.Level].
    /// 
    /// @param level The [System.Logger.Level] to convert.
    /// @return The corresponding [java.util.logging.Level].
    private static Level convertLevel(final System.Logger.Level level) {
        return switch (level) {
            case OFF -> Level.OFF;
            case ERROR -> Level.SEVERE;
            case WARNING -> Level.WARNING;
            case INFO -> Level.INFO;
            case DEBUG -> Level.FINE;
            case TRACE -> Level.FINER;
            case ALL -> Level.ALL;
        };
    }
}
