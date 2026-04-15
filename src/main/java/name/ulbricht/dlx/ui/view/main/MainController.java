package name.ulbricht.dlx.ui.view.main;

import java.io.File;
import java.io.IOException;
import java.lang.module.ModuleDescriptor.Version;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.console.ConsoleView;
import name.ulbricht.dlx.ui.view.editor.EditorView;
import name.ulbricht.dlx.ui.view.log.LogView;
import name.ulbricht.dlx.ui.view.memory.MemoryView;
import name.ulbricht.dlx.ui.view.outline.OutlineView;
import name.ulbricht.dlx.ui.view.pipeline.PipelineView;
import name.ulbricht.dlx.ui.view.preferences.PreferencesView;
import name.ulbricht.dlx.ui.view.problems.ProblemsView;
import name.ulbricht.dlx.ui.view.problems.SourceOrigin;
import name.ulbricht.dlx.ui.view.reference.ReferenceView;
import name.ulbricht.dlx.ui.view.registers.RegistersView;

/// Controller for the main application view.
public final class MainController {

    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();

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
    private TabPane primarySideBarTabPane;
    @FXML
    private TabPane editorsTabPane;
    @FXML
    private TabPane secondarySideBarTabPane;
    @FXML
    private TabPane panelTabPane;

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

    private final ReadOnlyObjectWrapper<EditorView> activeEditorView = new ReadOnlyObjectWrapper<>();
    private final ObservableList<SourceOrigin> sourceOrigins = FXCollections.observableArrayList();

    /// Creates a new main controller instance.
    public MainController() {
    }

    ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    Parent getRoot() {
        return this.mainRoot;
    }

    @FXML
    private void initialize() {
        configureFileChoosers();
        configureOpenRecentMenu();

        // Prevent closing the last tab
        this.primarySideBarTabPane.getTabs().subscribe(() -> toggleTabClosingPolicy(this.primarySideBarTabPane));
        this.secondarySideBarTabPane.getTabs().subscribe(() -> toggleTabClosingPolicy(this.secondarySideBarTabPane));
        this.panelTabPane.getTabs().subscribe(() -> toggleTabClosingPolicy(this.panelTabPane));

        // React on changes of the active editor
        this.activeEditorView.subscribe(this::activeEditorChanged);

        // Bind the selected editor tab to the active editor view property
        this.activeEditorView.bind(this.editorsTabPane.getSelectionModel().selectedItemProperty()
                .map(tab -> tab.getUserData() instanceof final EditorView editorView ? editorView : null));

        // Sync source origins list with editor tabs
        syncSourceOrigins();
        this.editorsTabPane.getTabs().addListener(
                (ListChangeListener<Tab>) this::editorTabsChanged);

        configureBuildActions();
        configureProcessorActions();
        configureStatusBar();

        openDefaultViews();
    }

    private static void toggleTabClosingPolicy(final TabPane tabPane) {
        tabPane.setTabClosingPolicy(
                tabPane.getTabs().size() > 1 ? TabPane.TabClosingPolicy.SELECTED_TAB
                        : TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    private void configureFileChoosers() {
        // Set initial directory from preference
        updateFileChooserDirectories();
        this.userPreferences.addPreferenceChangeListener(UserPreferences.RECENT_DIRECTORY_PROPERTY,
                _ -> Platform.runLater(this::updateFileChooserDirectories));
        this.userPreferences.addPreferenceChangeListener(UserPreferences.RECENT_FILES_PROPERTY,
                _ -> Platform.runLater(this::updateOpenRecentMenu));
        this.userPreferences.addPreferenceChangeListener(UserPreferences.THEME_PROPERTY,
                (final Theme theme) -> Platform.runLater(() -> themeUpdated(theme)));

        // Set the extension filters for the file choosers
        final var extensionFilters = List.of(
                createExtensionFilter("main.fileChooser.extension.asm", SourceFile.FILE_EXTENSIONS),
                createExtensionFilter("main.fileChooser.extension.all", List.of(".*")));
        this.openFileChooser.getExtensionFilters().addAll(extensionFilters);
        this.saveFileChooser.getExtensionFilters().addAll(extensionFilters);
    }

    private void updateFileChooserDirectories() {
        final var dir = this.userPreferences.getRecentDirectory().toFile();
        this.openFileChooser.setInitialDirectory(dir);
        this.saveFileChooser.setInitialDirectory(dir);
    }

    private static FileChooser.ExtensionFilter createExtensionFilter(final String key, final List<String> extensions) {
        return new FileChooser.ExtensionFilter(Messages.getString(key),
                extensions.stream().map(ext -> "*" + ext).toList());
    }

    private void configureOpenRecentMenu() {
        updateOpenRecentMenu();
    }

    private void updateOpenRecentMenu() {
        final var files = this.userPreferences.getRecentFiles();
        this.openRecentMenu.setDisable(files.isEmpty());
        // Remove all items that have a Path object as user data
        this.openRecentMenu.getItems().removeIf(item -> item.getUserData() instanceof Path);
        // Add menu items for the recent files
        this.openRecentMenu.getItems().addAll(0,
                files.stream()
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
    void windowShown(final WindowEvent event) {
        this.window = (Window) event.getSource();

        // Apply the current theme
        themeUpdated(this.userPreferences.getTheme());

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
    void windowCloseRequest(final WindowEvent event) {
        if (!canCloseWindow())
            event.consume();
    }

    void windowHiding(final WindowEvent event) {
        if (event.getSource() instanceof final Stage stage) {
            this.userPreferences.putStageState(MainView.STAGE_ID, Stages.getStageState(stage));
        }
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
    private void handleShowOutline() {
        showView(OutlineView.class, this::createOutlineView);
    }

    @FXML
    private void handleShowReference() {
        showView(ReferenceView.class, ReferenceView::load);
    }

    @FXML
    private void handleShowRegisters() {
        showView(RegistersView.class, this::createRegistersView);
    }

    @FXML
    private void handleShowMemory() {
        showView(MemoryView.class, this::createMemoryView);
    }

    @FXML
    private void handleShowPipeline() {
        showView(PipelineView.class, this::createPipelineView);
    }

    @FXML
    private void handleShowProblems() {
        showView(ProblemsView.class, this::createProblemsView);
    }

    @FXML
    private void handleShowLog() {
        showView(LogView.class, this::createLogView);
    }

    @FXML
    private void handleShowConsole() {
        showView(ConsoleView.class, this::createConsoleView);
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
        showView(ReferenceView.class, ReferenceView::load);
        showView(OutlineView.class, this::createOutlineView);
        showView(PipelineView.class, this::createPipelineView);
        showView(RegistersView.class, this::createRegistersView);
        showView(MemoryView.class, this::createMemoryView);
        showView(ProblemsView.class, this::createProblemsView);
        showView(LogView.class, this::createLogView);
        showView(ConsoleView.class, this::createConsoleView);
    }

    private OutlineView createOutlineView() {
        final var view = OutlineView.load(this.activeEditorView);
        view.setOnTextPosition(this::showTextPosition);
        return view;
    }

    private ProblemsView createProblemsView() {
        final var view = ProblemsView.load(this.sourceOrigins);
        view.setOnTextPosition(this::showTextPosition);
        return view;
    }

    private RegistersView createRegistersView() {
        final var view = RegistersView.load(this.viewModel.processorProperty());
        return view;
    }

    private MemoryView createMemoryView() {
        final var view = MemoryView.load(this.viewModel.processorProperty());
        return view;
    }

    private PipelineView createPipelineView() {
        final var view = PipelineView.load(this.viewModel.processorProperty());
        return view;
    }

    private LogView createLogView() {
        final var view = LogView.load();
        return view;
    }

    private ConsoleView createConsoleView() {
        final var view = ConsoleView.load();
        return view;
    }

    private void showView(final Class<? extends View<?, ?>> viewClass,
            final Supplier<? extends View<?, ?>> viewSupplier) {
        // Try to find the view in any of the view tab panes
        for (final var tabPane : List.of(this.primarySideBarTabPane, this.secondarySideBarTabPane, this.panelTabPane)) {
            for (final var tab : tabPane.getTabs()) {
                final var userData = tab.getUserData();
                if (viewClass.isInstance(userData)) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }
        }

        // Create and show the view
        final var view = viewSupplier.get();
        final var tab = createViewTab(view);
        final var tabPane = switch (view) {
            case final OutlineView _,final ReferenceView _ -> this.primarySideBarTabPane;
            case final RegistersView _,final MemoryView _,final PipelineView _ -> this.secondarySideBarTabPane;
            case final ProblemsView _,final LogView _,final ConsoleView _ -> this.panelTabPane;
            default -> this.primarySideBarTabPane;
        };
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private Tab createViewTab(final View<?, ?> view) {
        final var tab = new Tab();
        tab.setUserData(view);
        tab.setContent(view.getRoot());
        tab.setOnCloseRequest(this::viewCloseRequest);

        tab.textProperty().bind(view.titleProperty());

        final var tooltip = new Tooltip();
        tooltip.textProperty().bind(view.descriptionProperty());
        tab.setTooltip(tooltip);

        return tab;
    }

    private void viewCloseRequest(final Event event) {
        if (event.getSource() instanceof final Tab tab && tab.getUserData() instanceof final View<?, ?> view) {
            view.dispose();
        }
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

    private void activeEditorChanged(final EditorView newEditorView) {
        updateTitleBinding(newEditorView);
        updateCanSaveBinding(newEditorView);
        updateEditBindings(newEditorView);

        updateEditPositionBinding(newEditorView);
    }

    private void updateTitleBinding(final EditorView newEditorView) {
        // Unbind from the old editor
        this.title.unbind();
        this.title.set(Messages.getString("main.title"));

        // Bind to new editor
        if (newEditorView != null)
            this.title.bind(newEditorView.nameProperty()
                    .map(name -> Messages.getString("main.titlePattern").formatted(name)));
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
        return Optional.ofNullable(this.activeEditorView.get());
    }

    private void showTextPosition(final TextPositionEvent event) {
        final var sourceId = event.getSourceId();
        if (sourceId != null) {
            findEditorTabBySourceId(sourceId).ifPresent(tab -> {
                this.editorsTabPane.getSelectionModel().select(tab);
                getEditorView(tab).ifPresent(
                        editorView -> editorView.showEditPosition(event.getTextPosition()));
            });
        } else {
            getActiveEditorView().ifPresent(
                    editorView -> editorView.showEditPosition(event.getTextPosition()));
        }
    }

    private Optional<Tab> findEditorTabBySourceId(final UUID sourceId) {
        return this.editorsTabPane.getTabs().stream()
                .filter(tab -> getEditorView(tab)
                        .map(view -> Boolean.valueOf(sourceId.equals(view.getViewModel().id())))
                        .orElse(Boolean.FALSE).booleanValue())
                .findFirst();
    }

    private void syncSourceOrigins() {
        this.sourceOrigins.setAll(
                this.editorsTabPane.getTabs().stream()
                        .map(MainController::getEditorView)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(EditorView::getViewModel)
                        .map(SourceOrigin.class::cast)
                        .toList());
    }

    private void editorTabsChanged(final ListChangeListener.Change<? extends Tab> change) {
        while (change.next()) {
            if (change.wasRemoved()) {
                for (final var tab : change.getRemoved()) {
                    getEditorView(tab).ifPresent(
                            editorView -> this.sourceOrigins.remove(editorView.getViewModel()));
                }
            }
            if (change.wasAdded()) {
                for (final var tab : change.getAddedSubList()) {
                    getEditorView(tab).ifPresent(
                            editorView -> this.sourceOrigins.add(editorView.getViewModel()));
                }
            }
        }
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
