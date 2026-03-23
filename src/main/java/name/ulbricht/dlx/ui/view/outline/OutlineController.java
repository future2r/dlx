package name.ulbricht.dlx.ui.view.outline;

import javafx.fxml.FXML;
import javafx.scene.Parent;

/// Controller for the outline view.
public final class OutlineController {

    @FXML
    private Parent outlineRoot;

    @FXML
    private OutlineViewModel viewModel;

    /// Creates a new outline controller instance.
    public OutlineController() {

    }

    /// {@return the root node of the outline view}
    Parent getRoot() {
        return outlineRoot;
    }
}
