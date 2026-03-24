package name.ulbricht.dlx.ui.view.registers;

import java.util.Objects;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import name.ulbricht.dlx.simulator.Access;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.MemoryAccessListener;
import name.ulbricht.dlx.simulator.RegisterAccessListener;

/// View model for the internals view, providing access to the processor state.
public final class RegistersViewModel implements RegisterAccessListener, MemoryAccessListener {

    private final ObjectProperty<CPU> processor = new SimpleObjectProperty<>();

    private final ObservableList<RegisterItem> modifiableRegisters = FXCollections.observableArrayList();
    private final ReadOnlyListWrapper<RegisterItem> registers = new ReadOnlyListWrapper<>(FXCollections
            .unmodifiableObservableList(this.modifiableRegisters));

    /// Creates a new internals view model instance.
    public RegistersViewModel() {
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
            oldProcessor.getRegisters().removeAccessListener(this);
            oldProcessor.getMemory().removeAccessListener(this);
        }

        this.modifiableRegisters.clear();
        final var registerValues = getProcessor().getRegisters().snapshot();
        for (var i = 0; i < registerValues.length; i++)
            this.modifiableRegisters.add(new RegisterItem(i));

        if (newProcessor != null) {
            newProcessor.getRegisters().addAccessListener(this);
            newProcessor.getMemory().addAccessListener(this);
        }
    }

    @Override
    public void registerAccessed(final RegisterAccess access) {
        // This event may be originated from a different thread
        Platform.runLater(() -> updateRegisterAccess(access));
    }

    private void updateRegisterAccess(final RegisterAccess access) {
        final var index = access.index();
        final var item = getRegisters().get(index);
        switch (access.type()) {
            case READ -> item.updateAccess(Access.READ);
            case WRITE -> {
                item.updateValue(access.value());
                item.updateAccess(Access.WRITE);
            }
            default -> throw new IllegalStateException(Objects.toString(access.type()));
        }
    }

    @Override
    public void memoryAccessed(final MemoryAccess access) {
        // TODO Auto-generated method stub
    }
}
