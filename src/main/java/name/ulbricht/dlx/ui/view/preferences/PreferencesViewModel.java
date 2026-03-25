package name.ulbricht.dlx.ui.view.preferences;

import static java.util.Objects.requireNonNull;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.ui.scene.Theme;

/// View model for the application preferences.
public final class PreferencesViewModel {

    private final ReadOnlyListWrapper<Theme> themes = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(Theme.values())));
    private final ObjectProperty<Theme> selectedTheme = new SimpleObjectProperty<>();

    private final UserPreferences userPreferences;

    /// Creates a new instance.
    /// 
    /// @param userPreferences the user preferences to edit, must not be `null`
    public PreferencesViewModel(@NamedArg("userPreferences") final UserPreferences userPreferences) {
        this.userPreferences = requireNonNull(userPreferences);

        this.selectedTheme.set(this.userPreferences.getTheme());
    }

    /// {@return a read-only property for the available themes}
    public ReadOnlyListProperty<Theme> themesProperty() {
        return this.themes.getReadOnlyProperty();
    }

    /// {@return the list of available themes}
    public ObservableList<Theme> getThemes() {
        return this.themes.get();
    }

    /// {@return a property for the selected theme}
    public ObjectProperty<Theme> selectedThemeProperty() {
        return this.selectedTheme;
    }

    /// {@return the currently selected theme}
    public Theme getSelectedTheme() {
        return this.selectedTheme.get();
    }

    /// Sets the selected theme.
    ///
    /// @param theme the theme to select
    public void setSelectedTheme(final Theme theme) {
        this.selectedTheme.set(theme);
    }

    void savePreferences() {
        this.userPreferences.putTheme(getSelectedTheme());
    }
}
