package name.ulbricht.dlx.ui.view.main;

import java.lang.module.ModuleDescriptor.Version;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import name.ulbricht.dlx.compiler.Programs;
import name.ulbricht.dlx.ui.DlxApplication;
import name.ulbricht.dlx.ui.control.Alerts;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.console.ConsoleController;
import name.ulbricht.dlx.ui.view.editor.EditorController;
import name.ulbricht.dlx.ui.view.internals.InternalsController;

/// Controller for the main application view.
public final class MainController {

    @FXML
    private Window window;
    @FXML
    private Parent mainRoot;

    @FXML
    private EditorController editorController;
    @FXML
    private InternalsController internalsController;
    @FXML
    private ConsoleController consoleController;

    private final MainViewModel viewModel;

    /// Creates a new main controller instance.
    public MainController() {
        this.viewModel = new MainViewModel();
    }

    @FXML
    private void initialize() {
        this.viewModel.canSaveProperty().bind(this.editorController.getViewModel().dirtyProperty());

        this.editorController.getViewModel().setSource(Programs.createExampleSource());
        this.editorController.getViewModel().setProgram(Programs.createExampleProgram());

        this.internalsController.getViewModel().processorProperty().bind(this.viewModel.processorProperty());
    }

    /// Handles the window shown event.
    /// 
    /// @param event the window event
    public void windowShown(final WindowEvent event) {
        this.window = (Window) event.getSource();
        this.editorController.windowShown(event);
    }

    /// Handles the window close request event.
    /// 
    /// @param event the window event
    public void windowCloseRequest(final WindowEvent event) {
        if (this.editorController.getViewModel().isDirty())
            event.consume();
    }

    @FXML
    private void handleNew() {
        // Not yet implemented.
    }

    @FXML
    private void handleOpen() {
        // Not yet implemented.
    }

    @FXML
    private void handleSave() {
        // Not yet implemented.
    }

    @FXML
    private void handleSaveAs() {
        // Not yet implemented.
    }

    @FXML
    private void handleExit() {
        if (this.window instanceof final Stage stage)
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @FXML
    private void handleUndo() {
        // Not yet implemented.
    }

    @FXML
    private void handleRedo() {
        // Not yet implemented.
    }

    @FXML
    private void handleCut() {
        // Not yet implemented.
    }

    @FXML
    private void handleCopy() {
        // Not yet implemented.
    }

    @FXML
    private void handlePaste() {
        // Not yet implemented.
    }

    @FXML
    private void handleCompile() {
        // Not yet implemented.
    }

    @FXML
    private void handleCompileAndRun() {
        // Not yet implemented.
    }

    @FXML
    private void handleRun() {
        // Not yet implemented.
    }

    @FXML
    private void handlePause() {
        // Not yet implemented.
    }

    @FXML
    private void handleStop() {
        // Not yet implemented.
    }

    @FXML
    private void handleStep() {
        // Not yet implemented.
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

}
