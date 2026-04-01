package name.ulbricht.dlx.ui.view.log;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;

/// Controller for the Log view.
public final class LogController {

    @FXML
    private Parent logRoot;

    @FXML
    private LogViewModel viewModel;

    @FXML
    private TableView<LogEntry> logTableView;

    @FXML
    private ToggleButton autoScrollToggleButton;

    /// Creates a new log controller instance.
    public LogController() {
    }

    @FXML
    private void initialize() {

        this.autoScrollToggleButton.selectedProperty().bindBidirectional(this.viewModel.autoScrollProperty());

        this.viewModel.entriesProperty().addListener((ListChangeListener<LogEntry>) change -> {
            var added = false;
            while (change.next())
                added |= change.wasAdded();

            if (added && this.viewModel.isAutoScroll()) {
                final int lastIndex = this.viewModel.getEntries().size() - 1;
                if (lastIndex >= 0)
                    this.logTableView.scrollTo(lastIndex);
            }
        });
    }

    /// {@return root node of this view}
    Parent getRoot() {
        return this.logRoot;
    }

    /// {@return view model associated with this view}
    LogViewModel getViewModel() {
        return this.viewModel;
    }

    @FXML
    private void handleClear() {
        this.viewModel.clear();
    }
}
