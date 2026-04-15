package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.util.Subscription;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for displaying the problems detected in all open DLX programs.
public final class ProblemsView implements View<Parent, ProblemsViewModel> {

    /// Loads the problems view from the FXML file.
    ///
    /// @param sourceOrigins the observable list of source origins to track
    /// @return The configured problems view with the loaded content.
    public static ProblemsView load(final ObservableList<SourceOrigin> sourceOrigins) {
        return new ProblemsView(Views.loadController(ProblemsView.class,
                controllerClass -> controllerClass == ProblemsController.class
                        ? new ProblemsController(sourceOrigins)
                        : null));
    }

    private final ProblemsController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final Subscription titleSubscription;

    private ProblemsView(final ProblemsController controller) {
        this.controller = requireNonNull(controller);
        this.title.set(createTitle(0));

        // Update the title when the total problem count changes.
        this.titleSubscription = this.controller.getViewModel().totalProblemsCountProperty()
                .subscribe(this::updateTitle);
    }

    private void updateTitle(final Number size) {
        final var count = size.intValue();
        this.title.set(createTitle(count));
    }

    private static String createTitle(final int problemCount) {
        return problemCount > 0
                ? Messages.getString("problems.title.pattern").formatted(Integer.valueOf(problemCount))
                : Messages.getString("problems.title");
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
        this.titleSubscription.unsubscribe();
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
