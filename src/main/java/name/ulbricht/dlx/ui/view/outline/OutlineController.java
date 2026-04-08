package name.ulbricht.dlx.ui.view.outline;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.view.editor.EditorView;

/// Controller for the outline view.
public final class OutlineController {

    private final ObservableValue<EditorView> activeEditorView;

    @FXML
    private Parent outlineRoot;

    @FXML
    private OutlineViewModel viewModel;

    @FXML
    private TreeTableView<OutlineItem> structureTreeTableView;
    private final TreeItem<OutlineItem> dataRoot;
    private final TreeItem<OutlineItem> codeRoot;

    private final ObjectProperty<EventHandler<TextPositionEvent>> onTextPosition = new SimpleObjectProperty<>();

    /// Creates a new outline controller instance.
    /// 
    /// @param activeEditorView the observable value providing the currently active
    ///                         editor view
    OutlineController(final ObservableValue<EditorView> activeEditorView) {
        this.activeEditorView = requireNonNull(activeEditorView);

        this.dataRoot = new TreeItem<>(OutlineItem.dataSection());
        this.codeRoot = new TreeItem<>(OutlineItem.codeSection());
    }

    @FXML
    private void initialize() {
        // The section roots are always expanded
        this.dataRoot.setExpanded(true);
        this.codeRoot.setExpanded(true);

        // create the invisible root for the sections
        final var root = new TreeItem<OutlineItem>();
        root.getChildren().addAll(List.of(this.dataRoot, this.codeRoot));
        this.structureTreeTableView.setRoot(root);

        // Update the tree on changes to the compiled program
        this.viewModel.parsedProgramProperty().subscribe(this::parsedProgramChanged);

        // React on changes of the active editor view
        this.activeEditorView.subscribe(this::activeEditorChanged);
    }

    /// {@return the root node of the outline view}
    Parent getRoot() {
        return this.outlineRoot;
    }

    /// {@return the view model of the outline view}
    OutlineViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        activeEditorChanged(null);
    }

    private void activeEditorChanged(final EditorView newEditorView) {
        // Unbind from the old editor
        this.viewModel.parsedProgramProperty().unbind();
        this.viewModel.setParsedProgram(null);

        // Bind to the new editor
        if (newEditorView != null)
            this.viewModel.parsedProgramProperty().bind(newEditorView.getViewModel().parsedProgramProperty());
    }

    private void parsedProgramChanged(final ParsedProgram newParsedProgram) {
        // Remove the old items
        this.dataRoot.getChildren().clear();
        this.codeRoot.getChildren().clear();

        if (newParsedProgram != null) {

            // Update the data section
            this.dataRoot.getChildren().setAll(newParsedProgram.data().stream()
                    .map(OutlineItem::data)
                    .map(TreeItem::new)
                    .toList());

            // Update the code section
            this.codeRoot.getChildren().setAll(newParsedProgram.code().stream()
                    .map(OutlineItem::instruction)
                    .map(TreeItem::new)
                    .toList());
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
        if (event.getSource() instanceof final TreeTableRow<?> row
                && row.getTreeItem() instanceof final TreeItem<?> treeItem
                && treeItem.getValue() instanceof final OutlineItem item) {

            final var position = item.getTextPosition();
            final var handler = getOnTextPosition();

            if (position != null && handler != null)
                handler.handle(new TextPositionEvent(item.getTextPosition()));
        }
    }
}
