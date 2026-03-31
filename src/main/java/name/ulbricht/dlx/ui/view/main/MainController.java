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
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.config.UserPreferences;
import name.ulbricht.dlx.io.SourceFile;
import name.ulbricht.dlx.ui.DlxApplication;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.scene.Theme;
import name.ulbricht.dlx.ui.scene.ThemeManager;
import name.ulbricht.dlx.ui.scene.control.Alerts;
import name.ulbricht.dlx.ui.stage.Stages;
import name.ulbricht.dlx.ui.util.FormatUtil;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.ui.view.ViewResources;
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
    private RunService runService;

    @FXML
    private Menu openRecentMenu;

    @FXML
    private TabPane leftTabPane;
    @FXML
    private TabPane editorsTabPane;
    @FXML
    private TabPane rightTabPane;
    @FXML
    private TabPane bottomTabPane;

    @FXML
    private Label statusLabel;
    @FXML
    private Label cyclesLabel;
    @FXML
    private Label programCounterLabel;
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

    private final ReadOnlyBooleanWrapper canCompile = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canLoad = new ReadOnlyBooleanWrapper();

    private final ReadOnlyBooleanWrapper canRun = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canPause = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canReset = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper canStop = new ReadOnlyBooleanWrapper();

    /// Creates a new main controller instance.
    public MainController() {
    }

    @FXML
    private void initialize() {
        configureFileChoosers();
        configureOpenRecentMenu();

        // React on changes of the current editor tab.
        this.editorsTabPane.getSelectionModel().selectedItemProperty().subscribe(this::currentEditorTabChanged);

        configureBuildActions();
        configureProcessorActions();
        configureStatusBar();

        openDefaultViews();
    }

    private void configureFileChoosers() {
        // Bind the file chooser to the most recently used directory preference
        this.openFileChooser.initialDirectoryProperty()
                .bind(this.userPreferences.recentDirectoryProperty().map(Path::toFile));
        this.saveFileChooser.initialDirectoryProperty()
                .bind(this.userPreferences.recentDirectoryProperty().map(Path::toFile));

        // Set the extension filters for the file choosers
        final var extensionFilters = List.of(
                new FileChooser.ExtensionFilter(
                        Messages.getString("main.fileChooser.extension.dlx").formatted(SourceFile.FILE_EXTENSION),
                        "*" + SourceFile.FILE_EXTENSION),
                new FileChooser.ExtensionFilter(Messages.getString("main.fileChooser.extension.all"), "*.*"));
        this.openFileChooser.getExtensionFilters().addAll(extensionFilters);
        this.saveFileChooser.getExtensionFilters().addAll(extensionFilters);
    }

    private void configureOpenRecentMenu() {
        this.openRecentMenu.disableProperty()
                .bind(this.userPreferences.recentFilesProperty().emptyProperty());
        this.userPreferences.recentFilesProperty().subscribe(this::rebuildOpenRecentMenu);
        rebuildOpenRecentMenu();
    }

    private void rebuildOpenRecentMenu() {
        // Remove all items that have a Path object as user data
        this.openRecentMenu.getItems().removeIf(item -> item.getUserData() instanceof Path);
        // Add menu items for the recent files
        this.openRecentMenu.getItems().addAll(0,
                this.userPreferences.recentFilesProperty().stream()
                        .map(file -> {
                            final var item = new MenuItem(file.toString());
                            item.setUserData(file);
                            item.setOnAction(this::handleOpenRecent);
                            return item;
                        })
                        .toList());
    }

    private void configureBuildActions() {
        // Compile needs open editor
        this.canCompile.bind(this.editorsTabPane.getSelectionModel().selectedItemProperty().isNotNull());

        // Load needs compile and idle processor
        this.canLoad.bind(this.canCompile.and(this.runService.runningProperty().not()));
    }

    private void configureProcessorActions() {
        final var idle = this.runService.runningProperty().not();
        final var programLoaded = this.viewModel.programIdProperty().isNotNull();
        final var notHalted = this.viewModel.haltedProperty().not();

        // Run/Step need a loaded program, idle service, and non-halted CPU
        this.canRun.bind(programLoaded.and(idle).and(notHalted));

        // Pause/Stop need a running service
        this.canPause.bind(this.runService.runningProperty());
        this.canStop.bind(this.runService.runningProperty());

        // Reset needs an idle service
        this.canReset.bind(idle);
    }

    private void configureStatusBar() {
        this.runService.stateProperty().subscribe(state -> {
            final String text;
            final boolean running;
            switch (state) {
                case SCHEDULED, RUNNING -> {
                    text = Messages.getString("main.status.running");
                    running = true;
                }
                default -> {
                    text = Messages.getString("main.status.idle");
                    running = false;
                }
            }
            this.statusLabel.setText(text);
            this.statusLabel.pseudoClassStateChanged(PseudoClass.getPseudoClass("running"), running);
        });

        this.viewModel.cyclesProperty().subscribe(
                cycles -> this.cyclesLabel.setText(
                        Messages.getString("main.cycles.pattern").formatted(FormatUtil.decimal(cycles.longValue()))));

        this.viewModel.programCounterProperty().subscribe(
                programCounter -> this.programCounterLabel
                        .setText(Messages.getString("main.programCounter.pattern")
                                .formatted(FormatUtil.hexadecimal(programCounter.intValue()))));
    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        this.window = (Window) event.getSource();

        // Start tracking window state for persistence
        if (this.window instanceof final Stage stage)
            Stages.initWindowStatePersistence(stage,
                    ws -> ViewResources.userPreferences().putWindowState(MainView.WINDOW_ID, ws));

        // React on changes of the theme preferences
        this.userPreferences.themeProperty()
                .subscribe(this::themeUpdated);

        // Open a new editor and focus it
        Platform.runLater(() -> {
            openNewEditor();
            getActiveEditorView().ifPresent(EditorView::requestFocus);
        });
    }

    private void themeUpdated(final Theme theme) {
        Platform.runLater(() -> {
            ThemeManager.applyTheme(this.window.getScene(), theme);
            getAllEditorViews().forEach(EditorView::refreshSyntaxHighlighting);
        });
    }

    /// Handles the window close request event.
    /// 
    /// @param event the window event
    public void windowCloseRequest(final WindowEvent event) {
        if (!canCloseWindow())
            event.consume();
    }

    private boolean canCloseWindow() {
        // Check if the processor is idle
        if (this.runService.isRunning()) {
            Alerts.info(this.window, Messages.getString("main.exit.running"))
                    .showAndWait();
            return false;
        }

        // Verify that all editor views are either not dirty or the user confirmed to
        // save/discard changes
        return getAllEditorViews().stream().map(this::confirmSaveIfDirty).allMatch(Boolean.TRUE::equals);
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

    /// {@return a read-only property indicating whether compile is available}
    public ReadOnlyBooleanProperty canCompileProperty() {
        return this.canCompile.getReadOnlyProperty();
    }

    /// {@return whether compile is available}
    public boolean isCanCompile() {
        return this.canCompile.get();
    }

    /// {@return a read-only property indicating whether load is available}
    public ReadOnlyBooleanProperty canLoadProperty() {
        return this.canLoad.getReadOnlyProperty();
    }

    /// {@return whether load is available}
    public boolean isCanLoad() {
        return this.canLoad.get();
    }

    /// {@return a read-only property indicating whether run is available}
    public ReadOnlyBooleanProperty canRunProperty() {
        return this.canRun.getReadOnlyProperty();
    }

    /// {@return whether run is available}
    public boolean isCanRun() {
        return this.canRun.get();
    }

    /// {@return a read-only property indicating whether pause is available}
    public ReadOnlyBooleanProperty canPauseProperty() {
        return this.canPause.getReadOnlyProperty();
    }

    /// {@return whether pause is available}
    public boolean isCanPause() {
        return this.canPause.get();
    }

    /// {@return a read-only property indicating whether stop is available}
    public ReadOnlyBooleanProperty canStopProperty() {
        return this.canStop.getReadOnlyProperty();
    }

    /// {@return whether stop is available}
    public boolean isCanStop() {
        return this.canStop.get();
    }

    /// {@return a read-only property indicating whether reset is available}
    public ReadOnlyBooleanProperty canResetProperty() {
        return this.canReset.getReadOnlyProperty();
    }

    /// {@return whether reset is available}
    public boolean isCanReset() {
        return this.canReset.get();
    }

    @FXML
    private void handleNew() {
        openNewEditor();
    }

    @FXML
    private void handleOpen() {
        chooseOpenFile().ifPresent(this::openEditor);
    }

    private void handleOpenRecent(final ActionEvent event) {
        if (event.getSource() instanceof final MenuItem menuItem && menuItem.getUserData() instanceof final Path file)
            openEditor(file);
    }

    @FXML
    private void handleClearRecent() {
        this.userPreferences.clearRecentFiles();
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
        if (isCanCompile())
            compile();
    }

    @FXML
    private void handleCompileAndLoad() {
        if (isCanLoad())
            compile().ifPresent(this::load);
    }

    @FXML
    private void handleRun() {
        if (isCanRun())
            run(false);
    }

    @FXML
    private void handlePause() {
        if (isCanPause())
            this.runService.cancel();
    }

    @FXML
    private void handleStep() {
        if (isCanRun())
            run(true);
    }

    @FXML
    private void handleStop() {
        if (isCanStop()) {
            this.runService.cancel();
            this.viewModel.reset();
        }
    }

    @FXML
    private void handleReset() {
        if (isCanReset())
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
        final var memoryView = MemoryView.load();
        memoryView.getViewModel().processorProperty().bind(this.viewModel.processorProperty());
        openRightView(memoryView);

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

    private void openEditor(final Path file) {
        // If the file is already open, select its tab instead of opening a new one
        findEditorTab(file).ifPresentOrElse(tab -> {
            this.editorsTabPane.getSelectionModel().select(tab);
            this.userPreferences.addRecentFile(file);
        }, () -> {
            try {
                addEditorTab(EditorView.load(file));
                this.userPreferences.addRecentFile(file);
            } catch (final IOException ex) {
                this.userPreferences.removeRecentFile(file);
                Alerts.error(this.window, Messages.getString("main.open.error").formatted(ex.getMessage()))
                        .showAndWait();
            }
        });
    }

    private Optional<Tab> findEditorTab(final Path file) {
        return this.editorsTabPane.getTabs().stream()
                .filter(tab -> getEditorView(tab)
                        .map(view -> Boolean.valueOf(file.equals(view.getViewModel().getFile())))
                        .orElse(Boolean.FALSE).booleanValue())
                .findFirst();
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

        updateStageTitleBinding(newEditorView);
        updateCanSaveBinding(newEditorView);
        updateEditBindings(newEditorView);
        updateOutlineBinding(newEditorView);
        updateProblemsBinding(newEditorView);
        updateEditPositionBinding(newEditorView);
    }

    private void updateStageTitleBinding(final EditorView newEditorView) {
        if (this.window instanceof final Stage stage) {
            // Unbind from the old editor
            stage.titleProperty().unbind();
            stage.setTitle(Messages.getString("main.title"));

            // Bind to new editor
            if (newEditorView != null)
                stage.titleProperty().bind(newEditorView.nameProperty()
                        .map(name -> Messages.getString("main.titlePattern").formatted(name)));
        }
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
            problemsView.getViewModel().diagnosticsProperty().unbind();
            problemsView.getViewModel().setDiagnostics(null);
            // Bind to new editor
            if (newEditorView != null)
                problemsView.getViewModel().diagnosticsProperty()
                        .bind(newEditorView.getViewModel().diagnosticsProperty());
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

    private List<EditorView> getAllEditorViews() {
        return this.editorsTabPane.getTabs().stream()
                .map(MainController::getEditorView)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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

    private Optional<CompiledProgram> compile() {
        return getActiveEditorView().map(editorView -> {
            if (editorView.getViewModel().compile())
                return editorView.getViewModel().getCompiledProgram();

            Alerts.error(this.window, Messages.getString("main.compile.error")).showAndWait();
            return null;
        });
    }

    private void load(final CompiledProgram program) {
        this.runService.reset();
        this.viewModel.loadProgram(program);
    }

    private void run(final boolean debug) {
        this.runService.reset();
        this.runService.setDebug(debug);
        this.runService.start();
    }
}
