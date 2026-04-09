package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.Subscription;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;
import name.ulbricht.dlx.ui.view.editor.EditorView;
import name.ulbricht.dlx.util.TextPosition;

/// View for displaying the problems detected in the loaded DLX program.
public final class ProblemsView implements View<Parent, ProblemsViewModel> {

    /// Loads the problems view from the FXML file.
    ///
    /// @param activeEditorView the observable value providing the currently active
    ///                         editor view
    /// @return The configured problems view with the loaded content.
    public static ProblemsView load(final ObservableValue<EditorView> activeEditorView) {
        return new ProblemsView(Views.loadController(ProblemsView.class,
                controllerClass -> controllerClass == ProblemsController.class
                        ? new ProblemsController(activeEditorView)
                        : null));
    }

    /// {@return a cell factory for the source column that creates cells displaying
    /// the source of a problem}
    public static Callback<TableColumn<ProblemItem, Diagnostic.Stage>, TableCell<ProblemItem, Diagnostic.Stage>> sourceCellFactory() {
        return _ -> new SourceTableCell();
    }

    /// {@return a cell factory for the text position column that creates cells
    /// displaying the text position of a problem}
    public static Callback<TableColumn<ProblemItem, TextPosition>, TableCell<ProblemItem, TextPosition>> textPositionCellFactory() {
        return _ -> new TextPositionTableCell();
    }

    private final ProblemsController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("problems.title"));
    private final Subscription titleSubscription;

    private ProblemsView(final ProblemsController controller) {
        this.controller = requireNonNull(controller);

        // Update the title when the problem count changes.
        this.titleSubscription = this.controller.getViewModel().problemsProperty().sizeProperty()
                .subscribe(this::updateTitle);
    }

    private void updateTitle(final Number size) {
        final var count = size.intValue();
        if (count > 0) {
            this.title.set(Messages.getString("problems.title.pattern").formatted(Integer.valueOf(count)));
        } else {
            this.title.set(Messages.getString("problems.title"));
        }
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
