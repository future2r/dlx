package name.ulbricht.dlx.ui.view.memory;

import javafx.fxml.FXML;
import javafx.scene.Parent;

/// Controller for the memory view.
public final class MemoryController {

    @FXML
    private Parent memoryRoot;

    @FXML
    private MemoryViewModel viewModel;

    /// Creates a new memory controller instance.
    public MemoryController() {
    }

    /// {@return the root node of the memory view}
    Parent getRoot() {
        return this.memoryRoot;
    }
}
