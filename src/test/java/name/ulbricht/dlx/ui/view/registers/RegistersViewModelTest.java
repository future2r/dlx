package name.ulbricht.dlx.ui.view.registers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.simulator.Access;
import name.ulbricht.dlx.simulator.CPU;
import name.ulbricht.dlx.simulator.CycleListener;
import name.ulbricht.dlx.simulator.RegisterAccessListener;

@DisplayName("Registers view model")
final class RegistersViewModelTest {

    private Executor direct;
    private CPU cpu;
    private RegistersViewModel viewModel;

    @BeforeEach
    void setUp() {
        this.direct = Runnable::run;
        this.cpu = new CPU();
        this.viewModel = new RegistersViewModel(this.direct);
    }

    @Nested
    @DisplayName("Construction")
    final class Construction {

        @Test
        @DisplayName("null uiExecutor is rejected")
        void nullUiExecutorRejected() {
            assertThrows(NullPointerException.class, () -> new RegistersViewModel(null));
        }

        @Test
        @DisplayName("registers list is initially empty")
        void registersInitiallyEmpty() {
            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().isEmpty());
        }
    }

    @Nested
    @DisplayName("Processor handling")
    final class ProcessorHandling {

        @Test
        @DisplayName("setting a processor populates 32 register items")
        void setProcessorPopulatesRegisters() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);

            assertEquals(32, RegistersViewModelTest.this.viewModel.getRegisters().size());
        }

        @Test
        @DisplayName("register items have correct indices")
        void registerItemsHaveCorrectIndices() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);

            final var registers = RegistersViewModelTest.this.viewModel.getRegisters();
            for (var i = 0; i < 32; i++) {
                assertEquals(i, registers.get(i).getIndex());
            }
        }

        @Test
        @DisplayName("register items initially have zero values")
        void registerItemsHaveInitialZeroValues() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);

            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().stream()
                    .allMatch(item -> item.getValue() == 0));
        }

        @Test
        @DisplayName("register items initially have no access")
        void registerItemsHaveNoInitialAccess() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);

            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().stream()
                    .allMatch(item -> item.getAccess() == null));
        }

        @Test
        @DisplayName("setting null processor clears the registers list")
        void settingNullProcessorClearsRegisters() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);
            RegistersViewModelTest.this.viewModel.setProcessor(null);

            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().isEmpty());
        }

        @Test
        @DisplayName("swapping processor repopulates the registers list from the new processor")
        void swappingProcessorRepopulatesRegisters() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);
            RegistersViewModelTest.this.viewModel.setProcessor(new CPU());

            assertEquals(32, RegistersViewModelTest.this.viewModel.getRegisters().size());
        }

        @Test
        @DisplayName("after swapping processor, access highlights are cleared")
        void swappingProcessorClearsAccessHighlights() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.READ, 5, 0));

            RegistersViewModelTest.this.viewModel.setProcessor(new CPU());

            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().stream()
                    .allMatch(item -> item.getAccess() == null));
        }
    }

    @Nested
    @DisplayName("Register access")
    final class RegisterAccessHandling {

        @BeforeEach
        void setUpProcessor() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);
        }

        @Test
        @DisplayName("READ access marks the item's access as READ")
        void readAccessMarksItemAsRead() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.READ, 5, 0));

            assertEquals(Access.READ,
                    RegistersViewModelTest.this.viewModel.getRegisters().get(5).getAccess());
        }

        @Test
        @DisplayName("READ access does not change the item's value")
        void readAccessDoesNotChangeValue() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.READ, 7, 0));

            assertEquals(0, RegistersViewModelTest.this.viewModel.getRegisters().get(7).getValue());
        }

        @Test
        @DisplayName("WRITE access updates the item's value")
        void writeAccessUpdatesValue() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.WRITE, 3, 42));

            assertEquals(42, RegistersViewModelTest.this.viewModel.getRegisters().get(3).getValue());
        }

        @Test
        @DisplayName("WRITE access marks the item's access as WRITE")
        void writeAccessMarksItemAsWrite() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.WRITE, 3, 42));

            assertEquals(Access.WRITE,
                    RegistersViewModelTest.this.viewModel.getRegisters().get(3).getAccess());
        }
    }

    @Nested
    @DisplayName("Cycle handling")
    final class CycleHandling {

        @BeforeEach
        void setUpProcessor() {
            RegistersViewModelTest.this.viewModel.setProcessor(RegistersViewModelTest.this.cpu);
        }

        private CycleListener.Cycle cycleOf(final CycleListener.CycleState state) {
            return new CycleListener.Cycle(state, 0, 0, false,
                    RegistersViewModelTest.this.cpu.getPipelineSnapshot());
        }

        @Test
        @DisplayName("cycle START clears all register access highlights")
        void cycleStartClearsAccessHighlights() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.READ, 5, 0));
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.WRITE, 3, 42));

            RegistersViewModelTest.this.viewModel.onCycle(cycleOf(CycleListener.CycleState.START));

            assertTrue(RegistersViewModelTest.this.viewModel.getRegisters().stream()
                    .allMatch(item -> item.getAccess() == null));
        }

        @Test
        @DisplayName("cycle END does not clear access highlights")
        void cycleEndDoesNotClearAccessHighlights() {
            RegistersViewModelTest.this.viewModel.registerAccessed(
                    new RegisterAccessListener.RegisterAccess(Access.READ, 5, 0));

            RegistersViewModelTest.this.viewModel.onCycle(cycleOf(CycleListener.CycleState.END));

            assertEquals(Access.READ,
                    RegistersViewModelTest.this.viewModel.getRegisters().get(5).getAccess());
        }
    }
}
