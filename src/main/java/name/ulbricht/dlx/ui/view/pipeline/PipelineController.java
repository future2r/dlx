package name.ulbricht.dlx.ui.view.pipeline;

import static java.util.Objects.requireNonNull;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import name.ulbricht.dlx.simulator.CPU;

/// Controller for the pipeline view.
public final class PipelineController {

    private final ObservableValue<CPU> activeProcessor;

    @FXML
    private Parent pipelineRoot;

    @FXML
    private PipelineViewModel viewModel;

    /// Creates a new pipeline controller instance.
    ///
    /// @param activeProcessor the observable value providing the currently
    ///                        active processor
    public PipelineController(final ObservableValue<CPU> activeProcessor) {
        this.activeProcessor = requireNonNull(activeProcessor);
    }

    @FXML
    private void initialize() {
        this.viewModel.processorProperty().bind(this.activeProcessor);
    }

    Parent getRoot() {
        return this.pipelineRoot;
    }

    /// {@return the view model associated with this controller}
    PipelineViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.viewModel.processorProperty().unbind();
    }
}
