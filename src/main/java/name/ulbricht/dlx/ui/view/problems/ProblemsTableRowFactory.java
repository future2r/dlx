package name.ulbricht.dlx.ui.view.problems;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

/// Factory for creating table rows in the problems view.
public final class ProblemsTableRowFactory implements Callback<TableView<ProblemItem>, TableRow<ProblemItem>> {

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    /// Creates a new instance.
    public ProblemsTableRowFactory() {
    }

    /// {@return the action handler property for table rows}
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    /// {@return the action handler for table rows}
    public EventHandler<ActionEvent> getOnAction() {
        return this.onAction.get();
    }

    /// Sets the action handler for table rows.
    /// 
    /// @param handler the action handler to set
    public void setOnAction(final EventHandler<ActionEvent> handler) {
        this.onAction.set(handler);
    }

    @Override
    public TableRow<ProblemItem> call(final TableView<ProblemItem> param) {
        final var row = new TableRow<ProblemItem>();

        final var handler = this.getOnAction();
        if (handler != null) {
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handler.handle(new ActionEvent(row, event.getTarget()));
                }
            });
        }

        return row;
    }
}
