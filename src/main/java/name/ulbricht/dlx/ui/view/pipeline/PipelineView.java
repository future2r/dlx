package name.ulbricht.dlx.ui.view.pipeline;

import static java.util.Objects.requireNonNull;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for the DLX pipeline state.
public final class PipelineView implements View<Parent, PipelineViewModel> {

    /// Loads the pipeline view from the FXML file.
    ///
    /// @param activeProcessor the observable value providing the currently active
    ///                        processor
    /// @return the configured pipeline view with the loaded content
    public static PipelineView load(final ObservableValue<CPU> activeProcessor) {
        return new PipelineView(Views.loadController(PipelineView.class,
                controllerClass -> controllerClass == PipelineController.class
                        ? new PipelineController(activeProcessor)
                        : null));
    }

    private final PipelineController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("pipeline.title"));

    private PipelineView(final PipelineController controller) {
        this.controller = requireNonNull(controller);
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public PipelineViewModel getViewModel() {
        return this.controller.getViewModel();
    }

    @Override
    public void dispose() {
        this.controller.dispose();
    }
}
