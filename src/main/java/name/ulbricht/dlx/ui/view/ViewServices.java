package name.ulbricht.dlx.ui.view;

import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.service.Services;

/// Provides shared resources required by views. Factory methods can be
/// referenced from FXML via `fx:factory`.
public final class ViewServices {

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        return Services.userPreferences();
    }

    /// Private constructor to prevent instantiation.
    private ViewServices() {
    }
}
