package name.ulbricht.dlx.ui.view.main;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.ui.view.editor.EditorViewModel;

/// View model for the main application view.
public final class MainViewModel {

    private final ReadOnlyObjectWrapper<CPU> processor = new ReadOnlyObjectWrapper<>();

    private final ReadOnlyObjectWrapper<EditorViewModel> currentEditorViewModel = new ReadOnlyObjectWrapper<>();

    /// Creates a new main view model instance.
    public MainViewModel() {
        this.processor.set(new CPU());
    }

    /// {@return a read-only property representing the current processor}
    public ReadOnlyObjectProperty<CPU> processorProperty() {
        return this.processor.getReadOnlyProperty();
    }

    /// {@return the current processor}
    public CPU getProcessor() {
        return processorProperty().get();
    }

    /// {@return a read-only property representing the current editor view model}
    public ReadOnlyObjectProperty<EditorViewModel> currentEditorViewModelProperty() {
        return this.currentEditorViewModel.getReadOnlyProperty();
    }

    /// {@return the current editor view model}
    public EditorViewModel getCurrentEditorViewModel() {
        return currentEditorViewModelProperty().get();
    }

    void updateCurrentEditorViewModel(final EditorViewModel editorViewModel) {
        this.currentEditorViewModel.set(editorViewModel);
    }

    void run(final byte[] data, final int entryPoint) {
        // TODO Consider using a JavaFX service
        Thread.ofVirtual().start(() -> {
            this.processor.get().loadProgram(data, entryPoint);
            this.processor.get().run();
        });
    }

    void reset() {
        this.processor.set(new CPU());
    }

}
