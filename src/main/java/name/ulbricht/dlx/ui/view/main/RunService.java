package name.ulbricht.dlx.ui.view.main;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import name.ulbricht.dlx.simulator.CPU;

/// Service to run the processor in a background thread.
public final class RunService extends Service<Void> {

    private final ObjectProperty<CPU> processor = new SimpleObjectProperty<>();
    private final BooleanProperty debug = new SimpleBooleanProperty();

    /// Creates a new run service instance.
    public RunService() {
    }

    /// {@return a property representing the processor to run}
    public ObjectProperty<CPU> processorProperty() {
        return this.processor;
    }

    /// {@return the processor to run}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// Sets the processor to run.
    /// 
    /// @param processor the processor to run; must not be `null`
    public void setProcessor(final CPU processor) {
        processorProperty().set(processor);
    }

    /// {@return a property representing whether to run in debug mode}
    public BooleanProperty debugProperty() {
        return this.debug;
    }

    /// {@return whether to run in debug mode}
    public boolean isDebug() {
        return debugProperty().get();
    }

    /// Sets whether to run in debug mode.
    /// 
    /// @param debug whether to run in debug mode
    public void setDebug(final boolean debug) {
        debugProperty().set(debug);
    }

    @Override
    protected Task<Void> createTask() {

        // Make defensive copies
        final var currentProcessor = RunService.this.getProcessor();
        final var currentDebug = RunService.this.isDebug();

        return new Task<>() {

            @Override
            protected Void call() throws Exception {

                if (currentDebug)
                    currentProcessor.step();
                else
                    currentProcessor.run();

                return null;
            }
        };
    }
}
