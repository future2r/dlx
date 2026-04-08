package name.ulbricht.dlx.ui.view.registers;

import static java.util.Objects.requireNonNull;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import name.ulbricht.dlx.simulator.CPU;

/// Controller for the internals view.
public final class RegistersController {

    private final ObservableValue<CPU> activeProcessor;

    @FXML
    private Parent registersRoot;

    @FXML
    private RegistersViewModel viewModel;

    @FXML
    private TableView<RegisterItem> registersTable;

    /// Creates a new registers controller instance.
    /// 
    /// @param activeProcessor the observable value providing the currently
    ///                        active processor
    public RegistersController(final ObservableValue<CPU> activeProcessor) {
        this.activeProcessor = requireNonNull(activeProcessor);
    }

    @FXML
    private void initialize() {
        this.viewModel.processorProperty().bind(this.activeProcessor);
    }

    Parent getRoot() {
        return this.registersRoot;
    }

    /// {@return the view model associated with this controller}
    RegistersViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.viewModel.processorProperty().unbind();
    }
}
