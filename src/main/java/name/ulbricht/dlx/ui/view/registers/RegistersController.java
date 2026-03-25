package name.ulbricht.dlx.ui.view.registers;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;

/// Controller for the internals view.
public final class RegistersController {

    @FXML
    private Parent registersRoot;

    @FXML
    private RegistersViewModel viewModel;

    @FXML
    private TableView<RegisterItem> registersTable;

    /// Creates a new registers controller instance.
    public RegistersController() {
    }

    @FXML
    private void initialize() {
        // Bind the registers table to the view model's registers list.
        // The property types are not quite compatible, so we have to use a binding here
        // instead of just setting the items property in the FXML file.
        Bindings.bindContent(this.registersTable.getItems(), this.viewModel.getRegisters());
    }

    Parent getRoot() {
        return this.registersRoot;
    }

    /// {@return the view model associated with this controller}
    RegistersViewModel getViewModel() {
        return this.viewModel;
    }
}
