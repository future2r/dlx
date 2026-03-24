package name.ulbricht.dlx.ui.view.outline;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

/// Factory for creating tree table rows in the outline view.
public final class OutlineTreeTableRowFactory
        implements Callback<TreeTableView<OutlineItem>, TreeTableRow<OutlineItem>> {

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    /// Creates a new instance.
    public OutlineTreeTableRowFactory() {
    }

    /// {@return the action handler property for tree table rows}
    public ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    /// {@return the action handler for tree table rows}
    public EventHandler<ActionEvent> getOnAction() {
        return this.onAction.get();
    }

    /// Sets the action handler for tree table rows.
    /// 
    /// @param handler the action handler to set
    public void setOnAction(final EventHandler<ActionEvent> handler) {
        this.onAction.set(handler);
    }

    @Override
    public TreeTableRow<OutlineItem> call(final TreeTableView<OutlineItem> param) {
        final var row = new TreeTableRow<OutlineItem>();

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
