package name.ulbricht.dlx.ui.view.main;

import java.lang.module.ModuleDescriptor.Version;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.program.Programs;
import name.ulbricht.dlx.ui.DlxApplication;
import name.ulbricht.dlx.ui.controls.Alerts;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.ViewPart;
import name.ulbricht.dlx.ui.view.editor.EditorView;
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

    /// Creates a new main controller instance.
    public MainController() {
        this.viewModel = new MainViewModel();
    }

    @FXML
    private void initialize() {
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
        final var program = Programs.createExampleProgram();
        if (program != null)
            this.viewModel.run(program);
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
        openLeftView(OutlineView.load());

        // Registers View
        final var registersView = RegistersView.load();
        registersView.getViewModel().processorProperty().bind(this.viewModel.processorProperty());
        openRightView(registersView);

        // Memory View
        openRightView(MemoryView.load());

        // Problems View
        openBottomView(ProblemsView.load());
    }

    private void openLeftView(final ViewPart viewPart) {
        final var tab = createViewTab(viewPart);

        this.leftTabPane.getTabs().add(tab);
    }

    private void openRightView(final ViewPart viewPart) {
        final var tab = createViewTab(viewPart);

        this.rightTabPane.getTabs().add(tab);
    }

    private void openBottomView(final ViewPart viewPart) {
        final var tab = createViewTab(viewPart);

        this.bottomTabPane.getTabs().add(tab);
    }

    private Tab createViewTab(final ViewPart viewPart) {
        final var tab = new Tab();
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
