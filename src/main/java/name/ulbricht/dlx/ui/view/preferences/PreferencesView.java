package name.ulbricht.dlx.ui.view.preferences;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.Theme;
import name.ulbricht.dlx.ui.stage.Stages;

/// View for the application preferences.
public final class PreferencesView {

    /// Creates a new preferences dialog.
    ///
    /// @param owner the owner window of the dialog, may be `null`
    /// @return a new preferences dialog
    public static Dialog<Boolean> dialog(final Window owner) {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = PreferencesView.class.getResource("PreferencesView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load PreferencesView FXML", ex);
        }

        final var view = fxmlLoader.<DialogPane>getRoot();

        // Create the dialog
        final var dialog = Stages.<Boolean>createDialog(owner, Messages.getString("preferences.title"), view);

        // React on dialog events
        dialog.setResultConverter(button -> button == ButtonType.OK ? Boolean.TRUE : null);

        return dialog;
    }

    /// {@return the user preferences}
    public static UserPreferences userPreferences() {
        return UserPreferences.getInstance();
    }

    /// {@return a string converter for themes}
    public static StringConverter<Theme> themeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final Theme theme) {
                return Messages.getString("theme." + theme.name().toLowerCase());
            }

            @Override
            public Theme fromString(final String string) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /// Private constructor to prevent instantiation.
    private PreferencesView() {
    }
}
