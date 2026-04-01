package name.ulbricht.dlx.ui.view;

import java.util.concurrent.Executor;

import javafx.application.Platform;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.service.LogStore;
import name.ulbricht.dlx.service.Services;

/// Provides shared resources required by views. Factory methods can be
/// referenced from FXML via `fx:factory`.
public final class ViewServices {

    private static final Executor uiExecutor = Platform::runLater;

    /// {@return an executor that executes tasks on the JavaFX application thread}
    public static Executor uiExecutor() {
        return uiExecutor;
    }

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        return Services.userPreferences();
    }

    /// {@return the shared log store instance}
    public static LogStore logStore() {
        return Services.logStore();
    }

    /// Private constructor to prevent instantiation.
    private ViewServices() {
    }
}
