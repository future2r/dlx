package name.ulbricht.dlx.ui.view;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;

/// Describes a view part that can be displayed in the user interface.
/// 
/// @param <V> the type of the view model associated with this view part
public interface ViewPart<V> {

    /// {@return the read-only property for the title} This title is usually used in
    /// some kind of caption.
    ReadOnlyStringProperty titleProperty();

    /// {@return the title of the view part} The default implementation returns the
    /// current value of [#titleProperty()].
    default String getTitle() {
        return titleProperty().get();
    }

    /// {@return the read-only property for the description} This description is
    /// usually used in some kind of tooltip. The default impleementation returns the
    /// same property as [#titleProperty()], so the description is the same as the
    /// title by default.
    default ReadOnlyStringProperty descriptionProperty() {
        return titleProperty();
    }

    /// {@return the description of the view part} The default implementation
    /// returns the current value of [#descriptionProperty()].
    default String getDescription() {
        return descriptionProperty().get();
    }

    /// {@return the root node of the view part} This is the main content of the
    /// view part that will be displayed in the user interface.
    Node getRoot();

    /// {@return the view model associated with this view part} The default
    /// implementation returns `null`, so view parts that do not have a view model
    /// can safely ignore this method.
    default V getViewModel() {
        return null;
    }
}
