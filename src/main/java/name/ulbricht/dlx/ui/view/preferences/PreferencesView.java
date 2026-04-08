package name.ulbricht.dlx.ui.view.preferences;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import javafx.util.StringConverter;
import name.ulbricht.dlx.config.MemorySize;
import name.ulbricht.dlx.config.ProcessorSpeed;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.Theme;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.util.FormatUtil;
import name.ulbricht.dlx.ui.view.View;

/// View for the application preferences.
public final class PreferencesView implements View<DialogPane, PreferencesViewModel> {

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

        final var controller = fxmlLoader.<PreferencesController>getController();
        final var view = new PreferencesView(controller);

        // Create the dialog
        final var dialog = Stages.<Boolean>createDialog(owner, view);

        // React on dialog events
        dialog.setResultConverter(button -> button == ButtonType.OK ? Boolean.TRUE : null);

        return dialog;
    }

    /// {@return a string converter for processor speed presets}
    public static StringConverter<ProcessorSpeed> processorSpeedConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final ProcessorSpeed speed) {
                final var pattern = Messages.getString("processorSpeed." + speed.name().toLowerCase());
                return pattern.formatted(formatDuration(speed));
            }

            @Override
            public ProcessorSpeed fromString(final String string) {
                throw new UnsupportedOperationException();
            }

            private static String formatDuration(final ProcessorSpeed speed) {
                final var millis = speed.duration().toMillis();
                if (millis >= 1000 && millis % 1000 == 0) {
                    return (millis / 1000) + " s";
                }
                return millis + " ms";
            }
        };
    }

    /// {@return a string converter for memory size presets}
    public static StringConverter<MemorySize> memorySizeConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final MemorySize size) {
                final var pattern = Messages.getString("memorySize." + size.name().toLowerCase());
                return pattern.formatted(FormatUtil.formatBytes(size.sizeInBytes()));
            }

            @Override
            public MemorySize fromString(final String string) {
                throw new UnsupportedOperationException();
            }
        };
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

    /// {@return a string converter for log levels}
    public static StringConverter<System.Logger.Level> logLevelConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(final System.Logger.Level level) {
                return Messages.getString("logLevel." + level.name().toLowerCase());
            }

            @Override
            public System.Logger.Level fromString(final String string) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /// {@return a button type for restoring default preferences}
    public static ButtonType restoreDefaultsButtonType() {
        return new ButtonType(Messages.getString("preferences.restoreDefaults.text"), ButtonBar.ButtonData.LEFT);
    }

    private final PreferencesController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("preferences.title"));

    /// Private constructor to prevent instantiation.
    private PreferencesView(final PreferencesController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public DialogPane getRoot() {
        return this.controller.getRoot();
    }
}
