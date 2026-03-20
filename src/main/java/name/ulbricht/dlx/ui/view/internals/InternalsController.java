package name.ulbricht.dlx.ui.view.internals;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;

/// Controller for the internals view.
public final class InternalsController {

    @FXML
    private Parent internalsRoot;

    @FXML
    private InternalsViewModel viewModel;

    @FXML
    private TableView<RegisterItem> registersTable;

    /// Creates a new internals controller instance.
    public InternalsController() {
    }

    @FXML
    private void initialize() {
        // Bind the registers table to the view model's registers list.
        // The property types ar not quite compatible, so we have to use a binding here
        // instead of just setting the items property in the FXML file.
        Bindings.bindContent(this.registersTable.getItems(), this.viewModel.getRegisters());
    }

    /// {@return the view model associated with this controller}
    public InternalsViewModel getViewModel() {
        return this.viewModel;
    }
}
