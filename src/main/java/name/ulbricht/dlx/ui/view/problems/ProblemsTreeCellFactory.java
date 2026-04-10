package name.ulbricht.dlx.ui.view.problems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

/// Factory for creating tree cells in the problems view.
public final class ProblemsTreeCellFactory implements Callback<TreeView<ProblemItem>, TreeCell<ProblemItem>> {

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    /// Creates a new instance.
    public ProblemsTreeCellFactory() {
    }

    /// {@return the action handler property for tree cells}
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    /// {@return the action handler for tree cells}
    public EventHandler<ActionEvent> getOnAction() {
        return this.onAction.get();
    }

    /// Sets the action handler for tree cells.
    ///
    /// @param handler the action handler to set
    public void setOnAction(final EventHandler<ActionEvent> handler) {
        this.onAction.set(handler);
    }

    @Override
    public TreeCell<ProblemItem> call(final TreeView<ProblemItem> param) {
        final var cell = new ProblemTreeCell();

        final var handler = this.getOnAction();
        if (handler != null) {
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    handler.handle(new ActionEvent(cell, event.getTarget()));
                }
            });
        }

        return cell;
    }
}
