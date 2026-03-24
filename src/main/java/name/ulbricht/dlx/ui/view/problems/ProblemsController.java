package name.ulbricht.dlx.ui.view.problems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import name.ulbricht.dlx.ui.event.TextPositionEvent;

/// Controller for the problems view.
public final class ProblemsController {

    @FXML
    private Parent problemsRoot;

    @FXML
    private ProblemsViewModel viewModel;

    @FXML
    private TableView<ProblemItem> problemsTableView;

    private final ObjectProperty<EventHandler<TextPositionEvent>> onTextPosition = new SimpleObjectProperty<>();

    /// Creates a new problems controller instance.
    public ProblemsController() {
    }

    /// {@return the root node of the problems view}
    Parent getRoot() {
        return this.problemsRoot;
    }

    /// {@return the view model associated with this controller}
    ProblemsViewModel getViewModel() {
        return this.viewModel;
    }

    ObjectProperty<EventHandler<TextPositionEvent>> onTextPositionProperty() {
        return this.onTextPosition;
    }

    EventHandler<TextPositionEvent> getOnTextPosition() {
        return this.onTextPosition.get();
    }

    void setOnTextPosition(final EventHandler<TextPositionEvent> handler) {
        this.onTextPosition.set(handler);
    }

    @FXML
    private void handleRowAction(final ActionEvent event) {
        if (event.getSource() instanceof final TableRow<?> row
                && row.getItem() instanceof final ProblemItem item) {

            final var position = item.getTextPosition();
            final var handler = getOnTextPosition();

            if (position != null && handler != null)
                handler.handle(new TextPositionEvent(item.getTextPosition()));
        }
    }
}
