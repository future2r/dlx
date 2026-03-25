package name.ulbricht.dlx.ui.scene;

/// Theme options for the user interface. 
public enum Theme {

    /// Automatically select the theme based on the system settings.
    AUTO("/name/ulbricht/dlx/ui/css/auto.css"),

    /// Use a light color scheme.
    LIGHT("/name/ulbricht/dlx/ui/css/light.css"),

    /// Use a dark color scheme.
    DARK("/name/ulbricht/dlx/ui/css/dark.css");

    private final String stylesheet;

    Theme(final String cssResourceName) {
        this.stylesheet = cssResourceName != null ? getClass().getResource(cssResourceName).toExternalForm() : null;
    }

    /// {@return the stylesheet, or `null` if not applicable}
    public String getStylesheet() {
        return this.stylesheet;
    }
}
