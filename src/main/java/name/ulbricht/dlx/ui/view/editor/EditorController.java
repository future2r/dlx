package name.ulbricht.dlx.ui.view.editor;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.WindowEvent;

/// Controller for the editor view.
public final class EditorController {

    @FXML
    private Parent editorRoot;

    @FXML
    private EditorViewModel viewModel;

    @FXML
    private TextArea sourceTextArea;

    @FXML
    private TableView<DataItem<?>> dataTableView;
    @FXML
    private TableView<CodeItem> codeTableView;

    /// Creates a new editor controller instance.
    public EditorController() {
    }

    @FXML
    private void initialize() {
        this.sourceTextArea.textProperty().bindBidirectional(this.viewModel.sourceProperty());

        // Bind the data and code table to the view model.
        // The property types are not quite compatible, so we have to use a binding here
        // instead of just setting the items property in the FXML file.
        Bindings.bindContent(this.dataTableView.getItems(), this.viewModel.getDataItems());
        Bindings.bindContent(this.codeTableView.getItems(), this.viewModel.getCodeItems());
    }

    /// {@return the view model of this controller}
    public EditorViewModel getViewModel() {
        return this.viewModel;
    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        Platform.runLater(this.sourceTextArea::requestFocus);
    }
}
