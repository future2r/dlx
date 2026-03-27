package name.ulbricht.dlx.ui.view.preferences;

import static java.util.Objects.requireNonNull;

import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.config.MemorySize;
import name.ulbricht.dlx.config.ProcessorSpeed;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.ui.scene.Theme;

/// View model for the application preferences.
public final class PreferencesViewModel {

    private final ReadOnlyListWrapper<ProcessorSpeed> processorSpeeds = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(ProcessorSpeed.values())));
    private final ObjectProperty<ProcessorSpeed> selectedProcessorSpeed = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<MemorySize> memorySizes = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(MemorySize.values())));
    private final ObjectProperty<MemorySize> selectedMemorySize = new SimpleObjectProperty<>();

    private final ReadOnlyListWrapper<Theme> themes = new ReadOnlyListWrapper<>(
            FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(Theme.values())));
    private final ObjectProperty<Theme> selectedTheme = new SimpleObjectProperty<>();

    private final UserPreferences userPreferences;

    /// Creates a new instance.
    ///
    /// @param userPreferences the user preferences to edit, must not be `null`
    public PreferencesViewModel(@NamedArg("userPreferences") final UserPreferences userPreferences) {
        this.userPreferences = requireNonNull(userPreferences);

        this.selectedProcessorSpeed.set(this.userPreferences.getProcessorSpeed());
        this.selectedMemorySize.set(this.userPreferences.getMemorySize());
        this.selectedTheme.set(this.userPreferences.getTheme());
    }

    /// {@return a read-only property for the available processor speeds}
    public ReadOnlyListProperty<ProcessorSpeed> processorSpeedsProperty() {
        return this.processorSpeeds.getReadOnlyProperty();
    }

    /// {@return the list of available processor speeds}
    public ObservableList<ProcessorSpeed> getProcessorSpeeds() {
        return this.processorSpeeds.get();
    }

    /// {@return a property for the selected processor speed}
    public ObjectProperty<ProcessorSpeed> selectedProcessorSpeedProperty() {
        return this.selectedProcessorSpeed;
    }

    /// {@return the currently selected processor speed}
    public ProcessorSpeed getSelectedProcessorSpeed() {
        return this.selectedProcessorSpeed.get();
    }

    /// Sets the selected processor speed.
    ///
    /// @param processorSpeed the processor speed to select
    public void setSelectedProcessorSpeed(final ProcessorSpeed processorSpeed) {
        this.selectedProcessorSpeed.set(processorSpeed);
    }

    /// {@return a read-only property for the available memory sizes}
    public ReadOnlyListProperty<MemorySize> memorySizesProperty() {
        return this.memorySizes.getReadOnlyProperty();
    }

    /// {@return the list of available memory sizes}
    public ObservableList<MemorySize> getMemorySizes() {
        return this.memorySizes.get();
    }

    /// {@return a property for the selected memory size}
    public ObjectProperty<MemorySize> selectedMemorySizeProperty() {
        return this.selectedMemorySize;
    }

    /// {@return the currently selected memory size}
    public MemorySize getSelectedMemorySize() {
        return this.selectedMemorySize.get();
    }

    /// Sets the selected memory size.
    ///
    /// @param memorySize the memory size to select
    public void setSelectedMemorySize(final MemorySize memorySize) {
        this.selectedMemorySize.set(memorySize);
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
        this.userPreferences.putProcessorSpeed(getSelectedProcessorSpeed());
        this.userPreferences.putMemorySize(getSelectedMemorySize());
        this.userPreferences.putTheme(getSelectedTheme());
    }
}
