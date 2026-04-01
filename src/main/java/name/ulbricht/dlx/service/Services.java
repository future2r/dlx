package name.ulbricht.dlx.service;

import name.ulbricht.dlx.config.UserPreferences;

/// Provides shared application service instances.
public final class Services {

    private static UserPreferences userPreferences;
    private static LogStore logStore;

    /// {@return the shared user preferences instance}
    public static UserPreferences userPreferences() {
        if (userPreferences == null)
            userPreferences = new UserPreferences();
        return userPreferences;
    }

    /// {@return the shared log store instance}
    public static LogStore logStore() {
        if (logStore == null)
            logStore = new LogStore();
        return logStore;
    }

    /// Private constructor to prevent instantiation.
    private Services() {
    }
}
