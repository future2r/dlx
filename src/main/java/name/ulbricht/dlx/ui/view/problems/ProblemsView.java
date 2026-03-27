package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.util.TextPosition;

/// View for displaying the problems detected in the loaded DLX program.
public final class ProblemsView implements ViewPart<ProblemsViewModel> {

    /// Loads the problems view from the FXML file.
    /// 
    /// @return The configured problems view with the loaded content.
    public static ProblemsView load() {

        // Configure the FXML loader
        final var resources = Messages.BUNDLE;
        final var fxmlLocation = ProblemsView.class.getResource("ProblemsView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        // Load the view layout
        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load ProblemsView FXML", ex);
        }

        final var controller = fxmlLoader.<ProblemsController>getController();

        // Create and return the view
        return new ProblemsView(controller);
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

    private ProblemsView(final ProblemsController controller) {
        this.controller = requireNonNull(controller);

        // Update the title when the problem count changes.
        this.controller.getViewModel().problemsProperty().sizeProperty().subscribe(this::updateTitle);
    }

    private void updateTitle(final Number size) {
        final var count = size.intValue();
        if (count > 0) {
            this.title.set(Messages.getString("problems.title.pattern").formatted(count));
        } else {
            this.title.set(Messages.getString("problems.title"));
        }
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
    public ProblemsViewModel getViewModel() {
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
