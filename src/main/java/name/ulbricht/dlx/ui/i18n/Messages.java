package name.ulbricht.dlx.ui.i18n;

import java.util.MissingResourceException;
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
        return BUNDLE.getString(key);
    }
}
