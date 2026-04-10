package name.ulbricht.dlx.ui.view.problems;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import name.ulbricht.dlx.ui.event.TextPositionEvent;

/// Controller for the problems view.
public final class ProblemsController {

    private final ObservableList<SourceOrigin> sourceOrigins;
    private final ListChangeListener<SourceOrigin> sourceOriginsListener;

    @FXML
    private Parent problemsRoot;

    @FXML
    private ProblemsViewModel viewModel;

    @FXML
    private TreeView<ProblemItem> problemsTreeView;

    private final ObjectProperty<EventHandler<TextPositionEvent>> onTextPosition = new SimpleObjectProperty<>();

    /// Creates a new problems controller instance.
    ///
    /// @param sourceOrigins the observable list of source origins to track
    ProblemsController(final ObservableList<SourceOrigin> sourceOrigins) {
        this.sourceOrigins = requireNonNull(sourceOrigins);
        this.sourceOriginsListener = this::sourceOriginsChanged;
    }

    @FXML
    private void initialize() {
        this.problemsTreeView.setRoot(this.viewModel.getRoot());

        // Add all existing source origins and listen for future changes
        for (final var source : this.sourceOrigins) {
            this.viewModel.addSource(source);
        }

        this.sourceOrigins.addListener(this.sourceOriginsListener);
    }

    /// {@return the root node of the problems view}
    Parent getRoot() {
        return this.problemsRoot;
    }

    /// {@return the view model associated with this controller}
    ProblemsViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.sourceOrigins.removeListener(this.sourceOriginsListener);
        this.viewModel.dispose();
    }

    private void sourceOriginsChanged(final ListChangeListener.Change<? extends SourceOrigin> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                for (final var source : change.getRemoved()) {
                    this.viewModel.removeSource(source);
                }
            }
            if (change.wasAdded()) {
                for (final var source : change.getAddedSubList()) {
                    this.viewModel.addSource(source);
                }
            }
        }
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
        if (event.getSource() instanceof final TreeCell<?> cell
                && cell.getTreeItem() instanceof final TreeItem<?> treeItem
                && treeItem.getValue() instanceof final DiagnosticItem item) {

            final var position = item.textPosition();
            final var handler = getOnTextPosition();

            if (position != null && handler != null)
                handler.handle(new TextPositionEvent(position, item.sourceOrigin().id()));
        }
    }
}
