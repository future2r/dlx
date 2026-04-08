package name.ulbricht.dlx.ui.view;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Parent;

/// Describes a view that can be displayed in the user interface. A view is not
/// required to implement this interface, except it has a lifecycle or wants to
/// contribute to the user interface.
/// 
/// @param <R> the type of the root node of the view
/// @param <M> the type of the view model associated with this view
public interface View<R extends Parent, M> {

    /// {@return the read-only property for the title} This title is usually used in
    /// some kind of caption or window title.
    ReadOnlyStringProperty titleProperty();

    /// {@return the title of the view} The default implementation returns the
    /// current value of [#titleProperty()].
    default String getTitle() {
        return titleProperty().get();
    }

    /// {@return the read-only property for the description} This description is
    /// usually used in some kind of tooltip. The default implementation returns the
    /// same property as [#titleProperty()], so the description is the same as the
    /// title by default.
    default ReadOnlyStringProperty descriptionProperty() {
        return titleProperty();
    }

    /// {@return the description of the view} The default implementation returns the
    /// current value of [#descriptionProperty()].
    default String getDescription() {
        return descriptionProperty().get();
    }

    /// {@return the root node of the view} This is the main content of the view
    /// that will be displayed in the user interface.
    R getRoot();

    /// {@return the view model associated with this view} The default
    /// implementation returns `null`, so views that do not have a view model can
    /// safely ignore this method.
    default M getViewModel() {
        return null;
    }

    /// Disposes of the view and releases any resources it holds. The default
    /// implementation does nothing, so views that do not need to release resources
    /// can safely ignore this method.

    default void dispose() {
        // Empty default implementation
    }
}
