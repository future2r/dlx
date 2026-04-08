package name.ulbricht.dlx.ui.view.log;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.logging.Level;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import name.ulbricht.dlx.ui.i18n.Messages;
import name.ulbricht.dlx.ui.view.View;

/// View for displaying application logs.
public final class LogView implements View<Parent, LogViewModel> {

    /// Loads the log view from FXML.
    ///
    /// @return loaded log view
    public static LogView load() {

        final var resources = Messages.BUNDLE;
        final var fxmlLocation = LogView.class.getResource("LogView.fxml");
        final var fxmlLoader = new FXMLLoader(fxmlLocation, resources);

        try {
            fxmlLoader.load();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load LogView FXML", ex);
        }

        final var controller = fxmlLoader.<LogController>getController();
        return new LogView(controller);
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

    private LogView(final LogController controller) {
        this.controller = requireNonNull(controller);
        this.controller.getViewModel().entriesProperty().sizeProperty().subscribe(this::updateTitle);
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
    public ReadOnlyStringWrapper titleProperty() {
        return this.title;
    }

    @Override
    public Parent getRoot() {
        return this.controller.getRoot();
    }

    @Override
    public void dispose() {
        this.controller.getViewModel().dispose();
    }
}
