package name.ulbricht.dlx.ui.view;

import name.ulbricht.dlx.config.UserPreferences;

/// Provides shared resources required by views. Factory methods can be
/// referenced from FXML via `fx:factory`.
public final class ViewResources {

    private static UserPreferences userPreferences;

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        if (userPreferences == null)
            userPreferences = new UserPreferences();
        return userPreferences;
    }

    /// Private constructor to prevent instantiation.
    private ViewResources() {
    }
}
