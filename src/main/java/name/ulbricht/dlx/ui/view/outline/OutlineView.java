package name.ulbricht.dlx.ui.view.outline;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;

/// View for displaying the outline of the loaded DLX program.
public final class OutlineView implements ViewPart<OutlineViewModel> {

    /// Loads the outline view from the FXML file.
    /// 
    /// @return The configured outline view with the loaded content.
    public static OutlineView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = OutlineView.class.getResource("OutlineView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load OutlineView FXML", ex);
        }

        final var controller = fxmlLoader.<OutlineController>getController();

        // Create and return the view
        return new OutlineView(controller);
    }

    private final OutlineController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("outline.title"));

    private OutlineView(final OutlineController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringWrapper titleProperty() {
        return this.title;
    }

    @Override
    public Node getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public OutlineViewModel getViewModel() {
        return this.controller.getViewModel();
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
