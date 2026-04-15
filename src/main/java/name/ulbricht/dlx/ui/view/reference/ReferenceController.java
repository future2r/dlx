package name.ulbricht.dlx.ui.view.reference;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Subscription;

/// Controller for the reference view.
public final class ReferenceController {

    @FXML
    private Parent referenceRoot;

    @FXML
    private ReferenceViewModel viewModel;

    @FXML
    private TextField filterField;

    @FXML
    private TreeView<ReferenceItem> referenceTree;

    @FXML
    private TextArea detailArea;

    private Subscription treeItemsSubscription = Subscription.EMPTY;

    /// Creates a new reference controller instance.
    public ReferenceController() {
    }

    @FXML
    private void initialize() {
        this.referenceTree.setShowRoot(false);
        this.referenceTree.setRoot(new TreeItem<>());

        this.filterField.textProperty().bindBidirectional(this.viewModel.filterTextProperty());

        this.treeItemsSubscription = this.viewModel.treeItemsProperty().subscribe(this::updateTree);

        this.viewModel.selectedItemProperty().bind(this.referenceTree.getSelectionModel().selectedItemProperty());

        this.detailArea.textProperty().bind(this.viewModel.detailTextProperty());
    }

    /// {@return the root node of the reference view}
    Parent getRoot() {
        return this.referenceRoot;
    }

    /// {@return the view model of the reference view}
    ReferenceViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.treeItemsSubscription.unsubscribe();
        this.filterField.textProperty().unbindBidirectional(this.viewModel.filterTextProperty());
        this.viewModel.selectedItemProperty().unbind();
        this.detailArea.textProperty().unbind();
        this.referenceTree.getRoot().getChildren().clear();
    }

    private void updateTree(final ObservableList<? extends TreeItem<ReferenceItem>> items) {
        this.referenceTree.getRoot().getChildren().setAll(items);
    }

}
