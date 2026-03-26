package name.ulbricht.dlx.ui.scene;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.scene.Scene;

/// Manages application themes and applies them to scenes.
public final class ThemeManager {

    private static final String LIGHT_STYLESHEET = findStylesheet("light.css");
    private static final String DARK_STYLESHEET = findStylesheet("dark.css");

    private static String findStylesheet(final String cssName) {
        final var cssRoot = "/name/ulbricht/dlx/ui/css/";
        return ThemeManager.class.getResource(cssRoot + cssName).toExternalForm();
    }

    /// Applies the specified theme to the given scene.
    /// 
    /// @param scene The scene to apply the theme to.
    /// @param theme The theme to apply.
    public static void applyTheme(final Scene scene, final Theme theme) {

        // Set the stage preferences
        scene.getPreferences().setColorScheme(
                switch (theme) {
                    case AUTO -> Platform.getPreferences().getColorScheme();
                    case LIGHT -> ColorScheme.LIGHT;
                    case DARK -> ColorScheme.DARK;
                });

        // Remove all theme stylesheets
        scene.getStylesheets().removeAll(LIGHT_STYLESHEET, DARK_STYLESHEET);

        // Find the correct theme stylesheet
        final var themeStylesheet = switch (theme) {
            case AUTO ->
                scene.getPreferences().getColorScheme() == ColorScheme.DARK ? DARK_STYLESHEET : LIGHT_STYLESHEET;
            case LIGHT -> LIGHT_STYLESHEET;
            case DARK -> DARK_STYLESHEET;
        };

        // Add the theme stylesheet as the first one
        scene.getStylesheets().addFirst(themeStylesheet);

        // Remove all theme stylesheets from root (added to avoid warnings)
        scene.getRoot().getStylesheets().removeAll(LIGHT_STYLESHEET, DARK_STYLESHEET);
    }

    // Private constructor to prevent instantiation
    private ThemeManager() {
    }
}
