package name.ulbricht.dlx.ui.view.log;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.logging.Level;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.util.Subscription;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;
import name.ulbricht.dlx.ui.view.Views;

/// View for displaying application logs.
public final class LogView implements View<Parent, LogViewModel> {

    /// Loads the log view from FXML.
    ///
    /// @return loaded log view
    public static LogView load() {
        return new LogView(Views.loadController(LogView.class));
    }

    /// {@return a row factory for log table rows}
    public static Callback<TableView<LogEntry>, TableRow<LogEntry>> logTableRowFactory() {
        return _ -> new LogTableRow();
    }

    /// {@return a cell factory for the time column that creates cells displaying
    /// the time of a log entry}
    public static Callback<TableColumn<LogEntry, LocalDateTime>, TableCell<LogEntry, LocalDateTime>> timeCellFactory() {
        return _ -> new TimeTableCell();
    }

    /// {@return a cell factory for the level column that creates cells displaying
    /// the level of a log entry}
    public static Callback<TableColumn<LogEntry, Level>, TableCell<LogEntry, Level>> levelCellFactory() {
        return _ -> new LevelTableCell();
    }

    private final LogController controller;
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper(Messages.getString("log.title"));
    private final Subscription titleSubscription;

    private LogView(final LogController controller) {
        this.controller = requireNonNull(controller);
        this.titleSubscription = this.controller.getViewModel().entriesProperty().sizeProperty()
                .subscribe(this::updateTitle);
    }

    private void updateTitle(final Number size) {
        final var count = size.intValue();
        if (count > 0) {
            this.title.set(Messages.getString("log.title.pattern").formatted(Integer.valueOf(count)));
        } else {
            this.title.set(Messages.getString("log.title"));
        }
    }

    @Override
    public ReadOnlyStringProperty titleProperty() {
        return this.title.getReadOnlyProperty();
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public void dispose() {
        this.titleSubscription.unsubscribe();
        this.controller.getViewModel().dispose();
    }
}
