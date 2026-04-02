package name.ulbricht.dlx.service;

import name.ulbricht.dlx.config.UserPreferences;

/// Provides shared application service instances.
public final class Services {

    private static UserPreferences userPreferences;
    private static Logging logging;

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        if (userPreferences == null)
            userPreferences = new UserPreferences();
        return userPreferences;
    }

    /// {@return the shared logging service instance}
    public static Logging logging() {
        if (logging == null)
            logging = new Logging();
        return logging;
    }

    /// Private constructor to prevent instantiation.
    private Services() {
    }
}
