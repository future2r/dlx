package name.ulbricht.dlx.ui.view.console;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextArea;
import javafx.util.Subscription;

/// Controller for the Console view.
public final class ConsoleController {

    private Subscription textSubscription = Subscription.EMPTY;

    @FXML
    private Parent consoleRoot;

    @FXML
    private ConsoleViewModel viewModel;

    @FXML
    private TextArea consoleTextArea;

    /// Creates a new console controller instance.
    public ConsoleController() {
    }

    @FXML
    private void initialize() {
        this.textSubscription = this.viewModel.textProperty().subscribe(this::updateText);
        updateText(this.viewModel.getText());
    }

    private void updateText(final String text) {
        this.consoleTextArea.setText(text);
        this.consoleTextArea.positionCaret(text.length());
    }

    /// {@return root node of this view}
    Parent getRoot() {
        return this.consoleRoot;
    }

    /// {@return view model associated with this view}
    ConsoleViewModel getViewModel() {
        return this.viewModel;
    }

    void dispose() {
        this.textSubscription.unsubscribe();
    }

    @FXML
    private void handleClear() {
        this.viewModel.clear();
    }
}
