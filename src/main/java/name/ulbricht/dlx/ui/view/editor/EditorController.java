package name.ulbricht.dlx.ui.view.editor;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;

/// Controller for the editor view.
public final class EditorController {

    @FXML
    private Parent editorRoot;

    @FXML
    private EditorViewModel viewModel;

    @FXML
    private TextArea sourceTextArea;

    /// Creates a new editor controller instance.
    public EditorController() {
    }

    @FXML
    private void initialize() {
        this.sourceTextArea.textProperty().bindBidirectional(this.viewModel.modifiableSourceProperty());
    }

    /// {@return the view model of this controller}
    public EditorViewModel getViewModel() {
        return this.viewModel;
    }

    Parent getRoot() {
        return this.editorRoot;
    }
}
