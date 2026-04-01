package name.ulbricht.dlx.ui.view.main;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.concurrent.Executor;

import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.ProcessingListener;

/// View model for the main application view.
public final class MainViewModel implements ProcessingListener {

    private final ReadOnlyObjectWrapper<CPU> processor = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyObjectWrapper<UUID> programId = new ReadOnlyObjectWrapper<>();
    private final ReadOnlyLongWrapper cycles = new ReadOnlyLongWrapper();
    private final ReadOnlyIntegerWrapper programCounter = new ReadOnlyIntegerWrapper();
    private final ReadOnlyBooleanWrapper halted = new ReadOnlyBooleanWrapper();

    private final Executor uiExecutor;
    private final UserPreferences userPreferences;

    /// Creates a new main view model instance.
    ///
    /// @param uiExecutor      the executor to use for UI updates, must not be `null`
    /// @param userPreferences the user preferences, must not be `null`
    public MainViewModel(
            @NamedArg("uiExecutor") final Executor uiExecutor,
            @NamedArg("userPreferences") final UserPreferences userPreferences) {
        this.uiExecutor = requireNonNull(uiExecutor);
        this.userPreferences = requireNonNull(userPreferences);

        this.processor.subscribe(this::processorChanged);
        this.processor.set(createProcessor());
    }

    /// {@return a read-only property representing the current processor}
    public ReadOnlyObjectProperty<CPU> processorProperty() {
        return this.processor.getReadOnlyProperty();
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// {@return a read-only property representing the cycles}
    public ReadOnlyLongProperty cyclesProperty() {
        return this.cycles.getReadOnlyProperty();
    }

    /// {@return the cycles}
    public long getCycles() {
        return cyclesProperty().get();
    }

    /// {@return a read-only property representing the current program counter}
    public ReadOnlyIntegerProperty programCounterProperty() {
        return this.programCounter.getReadOnlyProperty();
    }

    /// {@return the current program counter}
    public int getProgramCounter() {
        return programCounterProperty().get();
    }

    /// {@return a read-only property indicating whether the CPU is halted}
    public ReadOnlyBooleanProperty haltedProperty() {
        return this.halted.getReadOnlyProperty();
    }

    /// {@return whether the CPU is halted}
    public boolean isHalted() {
        return haltedProperty().get();
    }

    private void processorChanged(final CPU oldProcessor, final CPU newProcessor) {
        if (oldProcessor != null) {
            oldProcessor.removeProcessingListener(this);
        }

        this.programId.set(null);
        this.cycles.set(0);
        this.programCounter.set(0);
        this.halted.set(false);

        if (newProcessor != null) {
            newProcessor.addProcessingListener(this);
            this.cycles.set(newProcessor.getCycles());
            this.programCounter.set(newProcessor.getProgramCounter());
            this.halted.set(newProcessor.isHalted());
        } else {
            this.cycles.set(0);
            this.programCounter.set(0);
            this.halted.set(false);
        }
    }

    /// {@return a read-only property representing the current program ID}
    public ReadOnlyObjectProperty<UUID> programIdProperty() {
        return this.programId.getReadOnlyProperty();
    }

    /// {@return the current program ID}
    public UUID getProgramId() {
        return programIdProperty().get();
    }

    private CPU createProcessor() {
        final var cpu = new CPU(this.userPreferences.getMemorySize().sizeInBytes());
        cpu.setStageDuration(this.userPreferences.getProcessorSpeed().duration());
        cpu.addProcessingListener(this);
        return cpu;
    }

    void loadProgram(final CompiledProgram compiledProgram) {
        requireNonNull(compiledProgram);

        this.processor.get().loadProgram(compiledProgram.program(), compiledProgram.entryPoint());
        this.programId.set(compiledProgram.id());
    }

    void reset() {
        this.programId.set(null);
        this.processor.set(createProcessor());
    }

    @Override
    public void processing(final ProcessStep step) {
        // Events may originate from the CPU's virtual thread.
        this.uiExecutor.execute(() -> handleProcessing(step));
    }

    private void handleProcessing(final ProcessStep step) {
        this.cycles.set(step.cycles());
        this.programCounter.set(step.programCounter());
        this.halted.set(step.halted());
    }
}
