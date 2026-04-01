package name.ulbricht.dlx.service;

import name.ulbricht.dlx.config.UserPreferences;

/// Provides shared application service instances.
public final class Services {

    private static UserPreferences userPreferences;

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        if (userPreferences == null)
            userPreferences = new UserPreferences();
        return userPreferences;
    }

    /// Private constructor to prevent instantiation.
    private Services() {
    }
}
