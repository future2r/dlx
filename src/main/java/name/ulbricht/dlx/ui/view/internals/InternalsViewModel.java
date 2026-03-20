package name.ulbricht.dlx.ui.view.internals;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.MemoryChangeListener;
import name.ulbricht.dlx.simulator.RegisterChangeListener;

/// View model for the internals view, providing access to the processor state.
public final class InternalsViewModel implements RegisterChangeListener, MemoryChangeListener {

    private final ObjectProperty<CPU> processor = new SimpleObjectProperty<>();

    private final ObservableList<RegisterItem> modifiableRegisters = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<RegisterItem> registers = new ReadOnlyListWrapper<>(FXCollections
            .unmodifiableObservableList(modifiableRegisters));

    /// Creates a new internals view model instance.
    public InternalsViewModel() {
        this.processor.subscribe(this::processorChanged);
    }

    /// {@return a property representing the current processor}
    public ObjectProperty<CPU> processorProperty() {
        return this.processor;
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// Sets the current processor.
    /// 
    /// @param processor the processor to set
    public void setProcessor(final CPU processor) {
        processorProperty().set(processor);
    }

    /// {@return a property representing the list of register items}
    public ReadOnlyListProperty<RegisterItem> registersProperty() {
        return this.registers.getReadOnlyProperty();
    }

    /// {@return the list of register items}
    public ObservableList<RegisterItem> getRegisters() {
        return registersProperty().get();
    }

    private void processorChanged(final CPU oldProcessor, final CPU newProcessor) {
        if (oldProcessor != null) {
            oldProcessor.getRegisters().removeChangeListener(this);
            oldProcessor.getMemory().removeChangeListener(this);
        }

        this.modifiableRegisters.clear();
        final var registerValues = getProcessor().getRegisters().snapshot();
        for (var i = 0; i < registerValues.length; i++)
            this.modifiableRegisters.add(new RegisterItem(i));

        if (newProcessor != null) {
            newProcessor.getRegisters().addChangeListener(this);
            newProcessor.getMemory().addChangeListener(this);
        }
    }

    @Override
    public void changed(final RegisterChange change) {
        // TODO Auto-generated method stub
    }

    @Override
    public void changed(final MemoryChange change) {
        // TODO Auto-generated method stub
    }
}
