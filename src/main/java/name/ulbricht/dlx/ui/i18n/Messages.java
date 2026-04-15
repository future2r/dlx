package name.ulbricht.dlx.ui.i18n;

import static java.util.Objects.requireNonNull;

import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

/// A utility class for managing internationalization (i18n) messages in
/// the application.
public final class Messages {

    private Messages() {
    }

    /// The resource bundle for the user interface.
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("name.ulbricht.dlx.ui.i18n.messages");

    /// Retrieves a localized string from the resource bundle.
    /// 
    /// @param key The key of the string to retrieve.
    /// @return The localized string corresponding to the given key.
    /// @throws MissingResourceException If the key is not found in the
    ///                                  resource bundle.
    public static String getString(final String key) {
        requireNonNull(key);

        return BUNDLE.getString(key);
    }

    /// Retrieves a localized string from the resource bundle, returning an empty
    /// optional if the key is not found.
    /// 
    /// @param key The key of the string to retrieve.
    /// @return An optional containing the localized string corresponding to the
    ///         given key, or an empty optional if the key is not found.
    public static Optional<String> getOptionalString(final String key) {
        requireNonNull(key);

        try {
            return Optional.of(getString(key));
        } catch (final MissingResourceException _) {
            return Optional.empty();
        }
    }
}
