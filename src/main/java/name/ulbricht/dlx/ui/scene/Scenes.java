package name.ulbricht.dlx.ui.scene;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.stream.Stream;

import javafx.scene.Scene;

/// Scene utilities.
public final class Scenes {

    /// Applies the given theme to the scene.
    /// 
    /// @param scene the scene to apply the theme to, must not be `null`
    /// @param theme the theme to apply, must not be `null`
    public static void applyTheme(final Scene scene, final Theme theme) {
        requireNonNull(scene);
        requireNonNull(theme);

        // Remove any old theme stylesheets
        scene.getStylesheets().removeAll(Stream.of(Theme.values())
                .map(Theme::getStylesheet)
                .filter(Objects::nonNull)
                .toList());

        // Add the new theme stylesheet
        final var stylesheet = theme.getStylesheet();
        if (stylesheet != null)
            scene.getStylesheets().add(stylesheet);
    }

    /// Private constructor to prevent instantiation.
    private Scenes() {
    }
}
