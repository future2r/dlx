package name.ulbricht.dlx.ui.view.problems;

import javafx.fxml.FXML;
import javafx.scene.Parent;

/// Controller for the problems view.
public final class ProblemsController {

    @FXML
    private Parent problemsRoot;

    @FXML
    private ProblemsViewModel viewModel;

    /// Creates a new problems controller instance.
    public ProblemsController() {
    }

    /// {@return the root node of the problems view}
    Parent getRoot() {
        return problemsRoot;
    }
}
