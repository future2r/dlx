package name.ulbricht.dlx.ui.view;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;

/// Describes a view part that can be displayed in the user interface.
public interface ViewPart {

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
}
