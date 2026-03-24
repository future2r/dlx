package name.ulbricht.dlx.ui.view.main;

import java.lang.module.ModuleDescriptor.Version;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.ui.DlxApplication;
import name.ulbricht.dlx.ui.controls.Alerts;
import name.ulbricht.dlx.ui.event.TextPositionEvent;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.ui.view.editor.EditorView;
import name.ulbricht.dlx.ui.view.editor.EditorViewModel;
import name.ulbricht.dlx.ui.view.memory.MemoryView;
import name.ulbricht.dlx.ui.view.outline.OutlineView;
import name.ulbricht.dlx.ui.view.problems.ProblemsView;
import name.ulbricht.dlx.ui.view.registers.RegistersView;

/// Controller for the main application view.
public final class MainController {

    @FXML
    private Window window;
    @FXML
    private Parent mainRoot;

    @FXML
    private TabPane leftTabPane;
    @FXML
    private TabPane editorsTabPane;
    @FXML
    private TabPane rightTabPane;
    @FXML
    private TabPane bottomTabPane;

    private final MainViewModel viewModel;

    private ProblemsView problemsView;
    private OutlineView outlineView;

    /// Creates a new main controller instance.
    public MainController() {
        this.viewModel = new MainViewModel();
    }

    @FXML
    private void initialize() {
        // Update the outline with the current editor view model
        this.viewModel.currentEditorViewModelProperty().subscribe(this::currentEditorViewModelChanged);

        // React on changes of the current editor tab.
        this.editorsTabPane.getSelectionModel().selectedItemProperty().subscribe(this::currentEditorTabChanged);
    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        this.window = (Window) event.getSource();

        // Open the default views
        Platform.runLater(this::openDefaultViews);

        // Open an empty editor
        Platform.runLater(this::openNewEditor);
    }

    /// Handles the window close request event.
    /// 
    /// @param event the window event
    public void windowCloseRequest(final WindowEvent event) {
        // TODO Check if we can close now
    }

    @FXML
    private void handleNew() {
        openNewEditor();
    }

    @FXML
    private void handleOpen() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleSave() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleSaveAs() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleExit() {
        if (this.window instanceof final Stage stage)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void handleUndo() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleRedo() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleCut() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handleCopy() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
    }

    @FXML
    private void handlePaste() {
        Alerts.info(this.window, "No implemented yet.").showAndWait();
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
        showAbout();
    }

    private void openDefaultViews() {
        // Outline View
        this.outlineView = OutlineView.load();
        this.outlineView.setOnTextPosition(this::showTextPosition);
        openLeftView(this.outlineView);

        // Registers View
        final var registersView = RegistersView.load();
        registersView.getViewModel().processorProperty().bind(this.viewModel.processorProperty());
        openRightView(registersView);

        // Memory View
        openRightView(MemoryView.load());

        // Problems View
        this.problemsView = ProblemsView.load();
        this.problemsView.setOnTextPosition(this::showTextPosition);
        openBottomView(this.problemsView);
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
        final var view = EditorView.load();
        final var tab = createViewTab(view);

        this.editorsTabPane.getTabs().add(tab);
        this.editorsTabPane.getSelectionModel().select(tab);
    }

    private void currentEditorTabChanged(final Tab selectedTab) {
        if (selectedTab != null &&
                selectedTab.getUserData() instanceof final EditorView editorView) {
            this.viewModel.updateCurrentEditorViewModel(editorView.getViewModel());
        } else {
            this.viewModel.updateCurrentEditorViewModel(null);
        }
    }

    private void currentEditorViewModelChanged(final EditorViewModel oldEditorViewModel,
            final EditorViewModel newEditorViewModel) {
        // Unbind from the old editor
        if (oldEditorViewModel != null) {
            if (this.outlineView != null) {
                this.outlineView.getViewModel().parsedProgramProperty().unbind();
                this.outlineView.getViewModel().setParsedProgram(null);
            }
            if (this.problemsView != null) {
                this.problemsView.getViewModel().parsedProgramProperty().unbind();
                this.problemsView.getViewModel().setParsedProgram(null);
            }
        }

        if (newEditorViewModel != null) {
            if (this.outlineView != null) {
                // Bind the outline view to the new editor's parsed program
                this.outlineView.getViewModel().parsedProgramProperty()
                        .bind(newEditorViewModel.parsedProgramProperty());
            }
            if (this.problemsView != null) {
                // Bind the problems view to the new editor's parsed program
                this.problemsView.getViewModel().parsedProgramProperty()
                        .bind(newEditorViewModel.parsedProgramProperty());
            }
        }
    }

    private void showTextPosition(final TextPositionEvent event) {
        getCurrentEditorView().ifPresent(editorView -> editorView.showTextPosition(event.getTextPosition()));
    }

    private Optional<EditorView> getCurrentEditorView() {
        final var selectedTab = this.editorsTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getUserData() instanceof final EditorView editorView)
            return Optional.of(editorView);
        return Optional.empty();
    }

    private void showAbout() {
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
}
