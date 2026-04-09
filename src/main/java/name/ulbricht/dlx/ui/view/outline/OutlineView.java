package name.ulbricht.dlx.ui.view.outline;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;
import name.ulbricht.dlx.ui.view.editor.EditorView;

/// View for displaying the outline of the loaded DLX program.
public final class OutlineView implements View<Parent, OutlineViewModel> {

    /// Loads the outline view from the FXML file.
    ///
    /// @param activeEditorView the observable value providing the currently active
    ///                         editor view
    /// @return The configured outline view with the loaded content.
    public static OutlineView load(final ObservableValue<EditorView> activeEditorView) {
        return new OutlineView(Views.loadController(OutlineView.class,
                controllerClass -> controllerClass == OutlineController.class
                        ? new OutlineController(activeEditorView)
                        : null));
    }

    private final OutlineController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("outline.title"));

    private OutlineView(final OutlineController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public void dispose() {
        this.controller.dispose();
    }

    /// {@return the event handler property for text position events triggered by
    /// this view}
    public ObjectProperty<EventHandler<TextPositionEvent>> onTextPositionProperty() {
        return this.controller.onTextPositionProperty();
    }

    /// {@return the event handler for text position events triggered by this view}
    public EventHandler<TextPositionEvent> getOnTextPosition() {
        return this.controller.getOnTextPosition();
    }

    /// Sets the event handler for text position events triggered by this view.
    /// 
    /// @param handler the event handler to set
    public void setOnTextPosition(final EventHandler<TextPositionEvent> handler) {
        this.controller.setOnTextPosition(handler);
    }
}
