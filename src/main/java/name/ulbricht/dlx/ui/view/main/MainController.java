package name.ulbricht.dlx.ui.view.main;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.io.SourceFile;
import name.ulbricht.dlx.ui.DlxApplication;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.Scenes;
import name.ulbricht.dlx.ui.scene.control.Alerts;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.ui.view.editor.EditorView;
import name.ulbricht.dlx.ui.view.memory.MemoryView;
import name.ulbricht.dlx.ui.view.outline.OutlineView;
import name.ulbricht.dlx.ui.view.preferences.PreferencesView;
import name.ulbricht.dlx.ui.view.problems.ProblemsView;
import name.ulbricht.dlx.ui.view.registers.RegistersView;

/// Controller for the main application view.
public final class MainController {

    @FXML
    private Parent mainRoot;

    @FXML
    private UserPreferences userPreferences;
    @FXML
    private FileChooser openFileChooser;
    @FXML
    private FileChooser saveFileChooser;
    @FXML
    private MainViewModel viewModel;

    @FXML
    private TabPane leftTabPane;
    @FXML
    private TabPane editorsTabPane;
    @FXML
    private TabPane rightTabPane;
    @FXML
    private TabPane bottomTabPane;

    @FXML
    private Label editPositionLabel;

    @FXML
    private Window window;

    private final ReadOnlyBooleanWrapper canSave = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canUndo = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canRedo = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canCut = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canCopy = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canPaste = new ReadOnlyBooleanWrapper();

    /// Creates a new main controller instance.
    public MainController() {
    }

    @FXML
    private void initialize() {
        // Bind the file chooser to the most recently used directory preference
        this.openFileChooser.initialDirectoryProperty()
                .bind(this.userPreferences.mostRecentlyUsedDirectoryProperty().map(Path::toFile));
        this.saveFileChooser.initialDirectoryProperty()
                .bind(this.userPreferences.mostRecentlyUsedDirectoryProperty().map(Path::toFile));

        // Set the extension filters for the file choosers
        final var extensionFilters = List.of(
                new FileChooser.ExtensionFilter(
                        Messages.getString("main.fileChooser.extension.dlx").formatted(SourceFile.FILE_EXTENSION),
                        "*" + SourceFile.FILE_EXTENSION),
                new FileChooser.ExtensionFilter(Messages.getString("main.fileChooser.extension.all"), "*.*"));
        this.openFileChooser.getExtensionFilters().addAll(extensionFilters);
        this.saveFileChooser.getExtensionFilters().addAll(extensionFilters);

        // React on changes of the current editor tab.
        this.editorsTabPane.getSelectionModel().selectedItemProperty().subscribe(this::currentEditorTabChanged);

        openDefaultViews();
        openNewEditor();
    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        this.window = (Window) event.getSource();

        // React on changes of the theme preferences
        this.userPreferences.themeProperty()
                .subscribe(theme -> Platform.runLater(() -> Scenes.applyTheme(this.window.getScene(), theme)));

        // Focus the editor (if there is any)
        Platform.runLater(() -> getActiveEditorView().ifPresent(EditorView::requestFocus));
    }

    /// Handles the window close request event.
    /// 
    /// @param event the window event
    public void windowCloseRequest(final WindowEvent event) {
        for (final var tab : this.editorsTabPane.getTabs()) {
            final var editor = getEditorView(tab);
            if (editor.isPresent() && !confirmSaveIfDirty(editor.get())) {
                event.consume();
                return;
            }
        }
    }

    /// {@return a read-only property indicating whether the active editor can
    /// be saved}
    public ReadOnlyBooleanProperty canSaveProperty() {
        return this.canSave.getReadOnlyProperty();
    }

    /// {@return whether the active editor can be saved}
    public boolean isCanSave() {
        return this.canSave.get();
    }

    /// {@return a read-only property indicating whether undo is available}
    public ReadOnlyBooleanProperty canUndoProperty() {
        return this.canUndo.getReadOnlyProperty();
    }

    /// {@return whether undo is available}
    public boolean isCanUndo() {
        return this.canUndo.get();
    }

    /// {@return a read-only property indicating whether redo is available}
    public ReadOnlyBooleanProperty canRedoProperty() {
        return this.canRedo.getReadOnlyProperty();
    }

    /// {@return whether redo is available}
    public boolean isCanRedo() {
        return this.canRedo.get();
    }

    /// {@return a read-only property indicating whether cut is available}
    public ReadOnlyBooleanProperty canCutProperty() {
        return this.canCut.getReadOnlyProperty();
    }

    /// {@return whether cut is available}
    public boolean isCanCut() {
        return this.canCut.get();
    }

    /// {@return a read-only property indicating whether copy is available}
    public ReadOnlyBooleanProperty canCopyProperty() {
        return this.canCopy.getReadOnlyProperty();
    }

    /// {@return whether copy is available}
    public boolean isCanCopy() {
        return this.canCopy.get();
    }

    /// {@return a read-only property indicating whether paste is available}
    public ReadOnlyBooleanProperty canPasteProperty() {
        return this.canPaste.getReadOnlyProperty();
    }

    /// {@return whether paste is available}
    public boolean isCanPaste() {
        return this.canPaste.get();
    }

    @FXML
    private void handleNew() {
        openNewEditor();
    }

    @FXML
    private void handleOpen() {
        chooseOpenFile().ifPresent(file -> {
            try {
                openEditor(file);
            } catch (final IOException e) {
                Alerts.error(this.window, "Failed to open file: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    private void handleSave() {
        getActiveEditorView().ifPresent(this::saveEditor);
    }

    @FXML
    private void handleSaveAs() {
        getActiveEditorView().ifPresent(this::saveEditorAs);
    }

    @FXML
    private void handlePreferences() {
        PreferencesView.dialog(this.window).showAndWait();
    }

    @FXML
    private void handleExit() {
        if (this.window instanceof final Stage stage)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void handleUndo() {
        getActiveEditorView().ifPresent(EditorView::undo);
    }

    @FXML
    private void handleRedo() {
        getActiveEditorView().ifPresent(EditorView::redo);
    }

    @FXML
    private void handleCut() {
        getActiveEditorView().ifPresent(EditorView::cut);
    }

    @FXML
    private void handleCopy() {
        getActiveEditorView().ifPresent(EditorView::copy);
    }

    @FXML
    private void handlePaste() {
        getActiveEditorView().ifPresent(EditorView::paste);
    }

    @FXML
    private void handleCompile() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleCompileAndRun() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleRun() {
        // TODO Check if we can run now

        // For now, get a dummy program
        final var data = new byte[] {
                0, 0, 0, 10, // op1 .word 10
                0, 0, 0, 32, // op2 .word 32
                0, 0, 0, 0, // res .word 0
                42, // b .byte 42
                16, 104, // h .half 4200
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // .buffer 20
                72, 101, 108, 108, 111, // msg .ascii "Hello"
                87, 111, 114, 108, 100, 0, // name .asciiz "World"
                -116, 1, 0, 0, // main : lw r1, op1(r0)
                -116, 2, 0, 4, // lw r2, op2(r0)
                0, 34, 24, 32, // add r3, r1, r2
                -84, 3, 0, 8, // sw res(r0), r3
                -4, 0, 0, 0 // halt
        };

        this.viewModel.run(data, 46);
    }

    @FXML
    private void handlePause() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleStop() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleStep() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleReset() {
        // TODO Check if we should reset now
        this.viewModel.reset();
    }

    @FXML
    private void handleAbout() {
        final var applicationVersion = DlxApplication.class.getModule().getDescriptor().version().map(Version::toString)
                .orElse("");
        final var javaVersion = System.getProperty("java.version");
        final var javafxVersion = System.getProperty("javafx.runtime.version");
        final var os = System.getProperty("os.name") + " " + System.getProperty("os.version");

        final var msg = Messages.getString("main.about.message").formatted(applicationVersion,
                javaVersion, javafxVersion, os);

        final var alert = Alerts.info(this.window, msg);
        alert.setTitle(Messages.getString("main.about.title"));
        alert.setHeaderText(Messages.getString("main.about.header"));
        alert.showAndWait();
    }

    private void openDefaultViews() {
        // Outline View
        final var outlineView = OutlineView.load();
        outlineView.setOnTextPosition(this::showTextPosition);
        openLeftView(outlineView);

        // Registers View
        final var registersView = RegistersView.load();
        registersView.getViewModel().processorProperty().bind(this.viewModel.processorProperty());
        openRightView(registersView);

        // Memory View
        openRightView(MemoryView.load());

        // Problems View
        final var problemsView = ProblemsView.load();
        problemsView.setOnTextPosition(this::showTextPosition);
        openBottomView(problemsView);
    }

    private void openLeftView(final ViewPart<?> viewPart) {
        final var tab = createViewTab(viewPart);

        this.leftTabPane.getTabs().add(tab);
    }

    private void openRightView(final ViewPart<?> viewPart) {
        final var tab = createViewTab(viewPart);

        this.rightTabPane.getTabs().add(tab);
    }

    private void openBottomView(final ViewPart<?> viewPart) {
        final var tab = createViewTab(viewPart);

        this.bottomTabPane.getTabs().add(tab);
    }

    private static Tab createViewTab(final ViewPart<?> viewPart) {
        final var tab = new Tab();
        tab.setUserData(viewPart);
        tab.setContent(viewPart.getRoot());

        tab.textProperty().bind(viewPart.titleProperty());

        final var tooltip = new Tooltip();
        tooltip.textProperty().bind(viewPart.descriptionProperty());
        tab.setTooltip(tooltip);

        return tab;
    }

    private void openNewEditor() {
        addEditorTab(EditorView.load());
    }

    private void openEditor(final Path file) throws IOException {
        addEditorTab(EditorView.load(file));
    }

    private void addEditorTab(final EditorView editorView) {
        final var tab = createViewTab(editorView);
        tab.setOnCloseRequest(event -> {
            if (!confirmSaveIfDirty(editorView))
                event.consume();
        });

        this.editorsTabPane.getTabs().add(tab);
        this.editorsTabPane.getSelectionModel().select(tab);
    }

    private void currentEditorTabChanged(final Tab newEditorTab) {
        final var newEditorView = getEditorView(newEditorTab).orElse(null);

        updateCanSaveBinding(newEditorView);
        updateEditBindings(newEditorView);
        updateOutlineBinding(newEditorView);
        updateProblemsBinding(newEditorView);
        updateEditPositionBinding(newEditorView);
    }

    private void updateCanSaveBinding(final EditorView newEditorView) {
        this.canSave.unbind();
        this.canSave.set(false);
        if (newEditorView != null)
            this.canSave.bind(newEditorView.getViewModel().dirtyProperty());
    }

    private void updateEditBindings(final EditorView newEditorView) {
        this.canUndo.unbind();
        this.canUndo.set(false);
        this.canRedo.unbind();
        this.canRedo.set(false);
        this.canCut.unbind();
        this.canCut.set(false);
        this.canCopy.unbind();
        this.canCopy.set(false);
        this.canPaste.set(newEditorView != null);

        if (newEditorView != null) {
            this.canUndo.bind(newEditorView.undoableProperty());
            this.canRedo.bind(newEditorView.redoableProperty());
            this.canCut.bind(newEditorView.hasSelectionProperty());
            this.canCopy.bind(newEditorView.hasSelectionProperty());
        }
    }

    private void updateOutlineBinding(final EditorView newEditorView) {
        getOutlineView().ifPresent(outlineView -> {
            // Unbind from the old editor
            outlineView.getViewModel().parsedProgramProperty().unbind();
            outlineView.getViewModel().setParsedProgram(null);
            // Bind to new editor
            if (newEditorView != null)
                outlineView.getViewModel().parsedProgramProperty()
                        .bind(newEditorView.getViewModel().parsedProgramProperty());
        });
    }

    private void updateProblemsBinding(final EditorView newEditorView) {
        getProblemsView().ifPresent(problemsView -> {
            // Unbind from the old editor
            problemsView.getViewModel().parsedProgramProperty().unbind();
            problemsView.getViewModel().setParsedProgram(null);
            // Bind to new editor
            if (newEditorView != null)
                problemsView.getViewModel().parsedProgramProperty()
                        .bind(newEditorView.getViewModel().parsedProgramProperty());
        });
    }

    private void updateEditPositionBinding(final EditorView newEditorView) {
        // Unbind from the old editor
        this.editPositionLabel.textProperty().unbind();
        this.editPositionLabel.setText("");
        // Bind to new editor
        if (newEditorView != null)
            this.editPositionLabel.textProperty().bind(newEditorView.editPositionProperty()
                    .map(pos -> Messages.getString("main.editPosition.pattern").formatted(
                            Integer.valueOf(pos.displayLine()),
                            Integer.valueOf(pos.displayColumn()))));
    }

    private static Optional<EditorView> getEditorView(final Tab tab) {
        if (tab != null && tab.getUserData() instanceof final EditorView editorView)
            return Optional.of(editorView);
        return Optional.empty();
    }

    private Optional<EditorView> getActiveEditorView() {
        return getEditorView(this.editorsTabPane.getSelectionModel().getSelectedItem());
    }

    private Optional<OutlineView> getOutlineView() {
        return this.leftTabPane.getTabs().stream()
                .map(Tab::getUserData)
                .filter(OutlineView.class::isInstance)
                .map(OutlineView.class::cast)
                .findFirst();
    }

    private Optional<ProblemsView> getProblemsView() {
        return this.bottomTabPane.getTabs().stream()
                .map(Tab::getUserData)
                .filter(ProblemsView.class::isInstance)
                .map(ProblemsView.class::cast)
                .findFirst();
    }

    private void showTextPosition(final TextPositionEvent event) {
        getActiveEditorView().ifPresent(editorView -> editorView.showEditPosition(event.getTextPosition()));
    }

    /// Checks if the editor is dirty and, if so, asks the user whether to save.
    /// Returns `true` if the caller may proceed (saved or discarded), `false` if the
    /// user cancelled.
    private boolean confirmSaveIfDirty(final EditorView editor) {
        if (!editor.getViewModel().isDirty())
            return true;

        final var message = Messages.getString("main.save.confirm").formatted(editor.getName());

        final var result = Alerts.confirmation(this.window, message).showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.CANCEL)
            return false;
        if (result == ButtonType.NO)
            return true;
        return saveEditor(editor);
    }

    /// Saves the editor. If the editor has no file yet, prompts for a file name.
    ///
    /// @return `true` if the file was saved, `false` if the user cancelled or an
    ///         error occurred
    private boolean saveEditor(final EditorView editor) {
        final var file = editor.getViewModel().getFile();
        if (file == null)
            return saveEditorAs(editor);
        return saveEditorToFile(editor, file);
    }

    /// Prompts for a file name and saves the editor.
    ///
    /// @return `true` if the file was saved, `false` if the user cancelled or an
    ///         error occurred
    private boolean saveEditorAs(final EditorView editor) {
        final var chosen = chooseSaveFile();
        if (chosen.isEmpty())
            return false;
        return saveEditorToFile(editor, chosen.get());
    }

    private boolean saveEditorToFile(final EditorView editor, final Path file) {
        try {
            editor.getViewModel().saveFile(file);
            return true;
        } catch (final IOException ex) {
            Alerts.error(this.window, Messages.getString("main.save.error").formatted(ex.getMessage())).showAndWait();
            return false;
        }
    }

    private Optional<Path> chooseOpenFile() {
        final var selectedFile = Optional.ofNullable(this.openFileChooser.showOpenDialog(this.window))
                .map(File::toPath);

        selectedFile.ifPresent(file -> this.userPreferences.putMostRecentlyUsedDirectory(file.getParent()));

        return selectedFile;
    }

    private Optional<Path> chooseSaveFile() {
        final var selectedFile = Optional.ofNullable(this.saveFileChooser.showSaveDialog(this.window))
                .map(File::toPath);

        selectedFile.ifPresent(file -> this.userPreferences.putMostRecentlyUsedDirectory(file.getParent()));

        return selectedFile;
    }
}
