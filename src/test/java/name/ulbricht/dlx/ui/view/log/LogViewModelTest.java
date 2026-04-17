package name.ulbricht.dlx.ui.view.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.service.Logging;

@DisplayName("Log view model")
final class LogViewModelTest {

    private static final String LOGGER_NAME = "name.ulbricht.dlx";

    private Level priorLoggerLevel;
    private boolean priorUseParentHandlers;
    private Logging logStore;
    private Executor direct;
    private LogViewModel viewModel;

    @BeforeEach
    void setUp() {
        final var logger = Logger.getLogger(LOGGER_NAME);
        this.priorLoggerLevel = logger.getLevel();
        this.priorUseParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        this.logStore = new Logging();
        this.direct = Runnable::run;
        this.viewModel = new LogViewModel(this.direct, this.logStore);
    }

    @AfterEach
    void tearDown() {
        this.viewModel.dispose();
        this.logStore.dispose();

        final var logger = Logger.getLogger(LOGGER_NAME);
        logger.setLevel(this.priorLoggerLevel);
        logger.setUseParentHandlers(this.priorUseParentHandlers);
    }

    private static LogRecord emit(final Level level, final String message) {
        final var record = new LogRecord(level, message);
        Logger.getLogger(LOGGER_NAME).log(record);
        return record;
    }

    @Nested
    @DisplayName("Construction")
    final class Construction {

        @Test
        @DisplayName("null uiExecutor is rejected")
        void nullUiExecutorRejected() {
            assertThrows(NullPointerException.class,
                    () -> new LogViewModel(null, LogViewModelTest.this.logStore));
        }

        @Test
        @DisplayName("null logStore is rejected")
        void nullLogStoreRejected() {
            assertThrows(NullPointerException.class,
                    () -> new LogViewModel(LogViewModelTest.this.direct, null));
        }

        @Test
        @DisplayName("empty log store leaves entries empty")
        void emptyStoreLeavesEntriesEmpty() {
            assertTrue(LogViewModelTest.this.viewModel.getEntries().isEmpty());
        }

        @Test
        @DisplayName("existing log records are applied on construction")
        void existingRecordsAppliedOnConstruction() {
            final var record = LogViewModelTest.emit(Level.INFO, "boot");

            final var freshVm = new LogViewModel(LogViewModelTest.this.direct, LogViewModelTest.this.logStore);
            try {
                final var entries = freshVm.getEntries();
                assertEquals(1, entries.size());
                assertSame(record, entries.get(0).getLogRecord());
            } finally {
                freshVm.dispose();
            }
        }
    }

    @Nested
    @DisplayName("Record handling")
    final class RecordHandling {

        @Test
        @DisplayName("adding a record appends a log entry")
        void addedRecordAppearsInEntries() {
            final var record = LogViewModelTest.emit(Level.INFO, "hello");

            final var entries = LogViewModelTest.this.viewModel.getEntries();
            assertEquals(1, entries.size());
            assertSame(record, entries.get(0).getLogRecord());
            assertEquals("hello", entries.get(0).getMessage());
            assertEquals(Level.INFO, entries.get(0).getLevel());
        }

        @Test
        @DisplayName("multiple added records are accumulated in order")
        void multipleRecordsAccumulate() {
            LogViewModelTest.emit(Level.INFO, "one");
            LogViewModelTest.emit(Level.WARNING, "two");
            LogViewModelTest.emit(Level.SEVERE, "three");

            final var entries = LogViewModelTest.this.viewModel.getEntries();
            assertEquals(3, entries.size());
            assertEquals("one", entries.get(0).getMessage());
            assertEquals("two", entries.get(1).getMessage());
            assertEquals("three", entries.get(2).getMessage());
        }

        @Test
        @DisplayName("records below an increased logger level are filtered out")
        void recordsBelowLoggerLevelAreFiltered() {
            Logger.getLogger(LOGGER_NAME).setLevel(Level.WARNING);

            LogViewModelTest.emit(Level.FINE, "debug");
            LogViewModelTest.emit(Level.WARNING, "warn");

            final var entries = LogViewModelTest.this.viewModel.getEntries();
            assertEquals(1, entries.size());
            assertEquals("warn", entries.get(0).getMessage());
        }
    }

    @Nested
    @DisplayName("Clear handling")
    final class Clearing {

        @Test
        @DisplayName("clearing the store removes all entries")
        void clearViaStoreResetsEntries() {
            LogViewModelTest.emit(Level.INFO, "a");
            LogViewModelTest.emit(Level.INFO, "b");

            LogViewModelTest.this.logStore.clear();

            assertTrue(LogViewModelTest.this.viewModel.getEntries().isEmpty());
        }

        @Test
        @DisplayName("clear() on the view model delegates to the store")
        void clearViaViewModelDelegatesToStore() {
            LogViewModelTest.emit(Level.INFO, "seed");

            LogViewModelTest.this.viewModel.clear();

            assertTrue(LogViewModelTest.this.viewModel.getEntries().isEmpty());
            assertTrue(LogViewModelTest.this.logStore.snapshot().isEmpty());
        }
    }

    @Nested
    @DisplayName("Auto-scroll")
    final class AutoScroll {

        @Test
        @DisplayName("defaults to true")
        void defaultsToTrue() {
            assertTrue(LogViewModelTest.this.viewModel.isAutoScroll());
            assertTrue(LogViewModelTest.this.viewModel.autoScrollProperty().get());
        }

        @Test
        @DisplayName("setAutoScroll updates the property")
        void setterUpdatesProperty() {
            LogViewModelTest.this.viewModel.setAutoScroll(false);

            assertFalse(LogViewModelTest.this.viewModel.isAutoScroll());
            assertFalse(LogViewModelTest.this.viewModel.autoScrollProperty().get());
        }

        @Test
        @DisplayName("property is mutable")
        void propertyIsMutable() {
            LogViewModelTest.this.viewModel.autoScrollProperty().set(false);

            assertFalse(LogViewModelTest.this.viewModel.isAutoScroll());
        }
    }

    @Nested
    @DisplayName("Disposal")
    final class Disposal {

        @Test
        @DisplayName("after dispose, later records are ignored")
        void disposeRemovesListenerSoLaterChangesAreIgnored() {
            LogViewModelTest.emit(Level.INFO, "before");
            LogViewModelTest.this.viewModel.dispose();

            LogViewModelTest.emit(Level.INFO, "after");

            final var entries = LogViewModelTest.this.viewModel.getEntries();
            assertEquals(1, entries.size());
            assertEquals("before", entries.get(0).getMessage());
        }

        @Test
        @DisplayName("dispose is idempotent")
        void disposeIsIdempotent() {
            LogViewModelTest.this.viewModel.dispose();
            LogViewModelTest.this.viewModel.dispose();
        }
    }
}
