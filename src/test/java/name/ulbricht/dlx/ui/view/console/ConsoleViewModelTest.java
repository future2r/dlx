package name.ulbricht.dlx.ui.view.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.service.Console;

@DisplayName("Console view model")
final class ConsoleViewModelTest {

    private Console console;
    private Executor direct;
    private ConsoleViewModel viewModel;

    @BeforeEach
    void setUp() {
        this.console = new Console();
        this.direct = Runnable::run;
        this.viewModel = new ConsoleViewModel(this.direct, this.console);
    }

    @Nested
    @DisplayName("Construction")
    final class Construction {

        @Test
        @DisplayName("null uiExecutor is rejected")
        void nullUiExecutorRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ConsoleViewModel(null, ConsoleViewModelTest.this.console));
        }

        @Test
        @DisplayName("null console is rejected")
        void nullConsoleRejected() {
            assertThrows(NullPointerException.class,
                    () -> new ConsoleViewModel(ConsoleViewModelTest.this.direct, null));
        }

        @Test
        @DisplayName("empty console snapshot leaves text property empty")
        void emptySnapshotLeavesTextEmpty() {
            assertEquals("", ConsoleViewModelTest.this.viewModel.getText());
        }

        @Test
        @DisplayName("existing console content is applied on construction")
        void existingSnapshotAppliedOnConstruction() {
            final var seeded = new Console();
            seeded.append("boot\n");
            final var freshVm = new ConsoleViewModel(ConsoleViewModelTest.this.direct, seeded);
            assertEquals("boot\n", freshVm.getText());
        }
    }

    @Nested
    @DisplayName("Append handling")
    final class Appending {

        @Test
        @DisplayName("appending to the console updates the text property")
        void appendUpdatesTextProperty() {
            ConsoleViewModelTest.this.console.append("hello");
            assertEquals("hello", ConsoleViewModelTest.this.viewModel.getText());
        }

        @Test
        @DisplayName("multiple appends are concatenated")
        void multipleAppendsConcatenate() {
            ConsoleViewModelTest.this.console.append("a");
            ConsoleViewModelTest.this.console.append("b");
            ConsoleViewModelTest.this.console.append("c");
            assertEquals("abc", ConsoleViewModelTest.this.viewModel.getText());
        }

        @Test
        @DisplayName("text property listener fires on append")
        void textPropertyListenerFiresOnAppend() {
            final var changes = new ArrayList<String>();
            ConsoleViewModelTest.this.viewModel.textProperty().addListener(
                    (_, _, newValue) -> changes.add(newValue));

            ConsoleViewModelTest.this.console.append("hi");

            assertEquals(List.of("hi"), changes);
        }
    }

    @Nested
    @DisplayName("Clear handling")
    final class Clearing {

        @Test
        @DisplayName("clearing the console resets the text property")
        void clearViaConsoleResetsText() {
            ConsoleViewModelTest.this.console.append("seed");
            ConsoleViewModelTest.this.console.clear();
            assertEquals("", ConsoleViewModelTest.this.viewModel.getText());
        }

        @Test
        @DisplayName("clear() on the view model delegates to the console")
        void clearViaViewModelDelegatesToConsole() {
            ConsoleViewModelTest.this.console.append("seed");
            ConsoleViewModelTest.this.viewModel.clear();
            assertEquals("", ConsoleViewModelTest.this.viewModel.getText());
            assertEquals("", ConsoleViewModelTest.this.console.snapshot());
        }
    }

    @Nested
    @DisplayName("Disposal")
    final class Disposal {

        @Test
        @DisplayName("after dispose, later appends are ignored")
        void disposeRemovesListenerSoLaterAppendsAreIgnored() {
            ConsoleViewModelTest.this.console.append("before");
            ConsoleViewModelTest.this.viewModel.dispose();

            ConsoleViewModelTest.this.console.append("after");

            assertEquals("before", ConsoleViewModelTest.this.viewModel.getText());
        }

        @Test
        @DisplayName("after dispose, the last observed text is retained")
        void disposeRetainsLastObservedText() {
            ConsoleViewModelTest.this.console.append("keep");
            ConsoleViewModelTest.this.viewModel.dispose();
            assertEquals("keep", ConsoleViewModelTest.this.viewModel.getText());
        }
    }
}
