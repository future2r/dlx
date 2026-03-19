package name.ulbricht.dlx.ui.view.editor;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.stage.WindowEvent;

/// Controller for the editor view.
public final class EditorController {

    @FXML
    private Parent editorRoot;

    @FXML
    private TextArea sourceTextArea;

    /// Creates a new editor controller instance.
    public EditorController() {
    }

    @FXML
    private void initialize() {

    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        Platform.runLater(this.sourceTextArea::requestFocus);
    }
}
