package name.ulbricht.dlx.ui.view;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.i18n.Messages;

/// Shared utility for loading FXML-based views.
///
/// The FXML file is expected to reside in the same package as the given view
/// class and follow the naming convention `{SimpleClassName}.fxml`.
public final class Views {

    /// Loads the FXML file associated with the given view class and returns the
    /// controller created by the loader.
    ///
    /// @param <C>       the controller type
    /// @param viewClass the view class used to locate the FXML resource
    /// @return the controller instance
    public static <C> C loadController(final Class<?> viewClass) {
        return loadController(viewClass, null);
    }

    /// Loads the FXML file associated with the given view class using a custom
    /// controller factory and returns the controller created by the loader.
    ///
    /// @param <C>               the controller type
    /// @param viewClass         the view class used to locate the FXML resource
    /// @param controllerFactory optional factory for creating controllers, or `null`
    ///                          to use the default
    /// @return the controller instance
    public static <C> C loadController(final Class<?> viewClass,
            final Callback<Class<?>, Object> controllerFactory) {

        final var fxmlName = viewClass.getSimpleName() + ".fxml";
        final var fxmlLocation = viewClass.getResource(fxmlName);
        final var fxmlLoader = controllerFactory != null
                ? new FXMLLoader(fxmlLocation, Messages.BUNDLE, null, controllerFactory)
                : new FXMLLoader(fxmlLocation, Messages.BUNDLE);

        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load " + fxmlName, ex);
        }

        return fxmlLoader.getController();
    }

    /// Private constructor — this class is not instantiable.
    private Views() {
    }
}
