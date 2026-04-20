package name.ulbricht.dlx.ui.view.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import name.ulbricht.dlx.simulator.MemoryAccessListener;

@DisplayName("Memory view model")
final class MemoryViewModelTest {

    // Two full 16-byte rows — small enough for fast tests, big enough to test row
    // spanning.
    private static final int MEM_SIZE = 32;

    private Executor direct;
    private CPU cpu;
    private MemoryViewModel viewModel;

    @BeforeEach
    void setUp() {
        this.direct = Runnable::run;
        this.cpu = new CPU(MEM_SIZE);
        this.viewModel = new MemoryViewModel(this.direct);
    }

    @Nested
    @DisplayName("Construction")
    final class Construction {

        @Test
        @DisplayName("null uiExecutor is rejected")
        void nullUiExecutorRejected() {
            assertThrows(NullPointerException.class, () -> new MemoryViewModel(null));
        }

        @Test
        @DisplayName("rows list is initially empty")
        void rowsInitiallyEmpty() {
            assertTrue(MemoryViewModelTest.this.viewModel.getRows().isEmpty());
        }
    }

    @Nested
    @DisplayName("Processor handling")
    final class ProcessorHandling {

        @Test
        @DisplayName("setting a processor creates one row per 16 bytes")
        void setProcessorCreatesRows() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);

            assertEquals(MEM_SIZE / MemoryRow.BYTES_PER_ROW,
                    MemoryViewModelTest.this.viewModel.getRows().size());
        }

        @Test
        @DisplayName("rows have correct base addresses")
        void rowsHaveCorrectBaseAddresses() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);

            final var rows = MemoryViewModelTest.this.viewModel.getRows();
            for (var i = 0; i < rows.size(); i++) {
                assertEquals(i * MemoryRow.BYTES_PER_ROW, rows.get(i).baseAddress());
            }
        }

        @Test
        @DisplayName("setting null processor clears the rows list")
        void settingNullProcessorClearsRows() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);
            MemoryViewModelTest.this.viewModel.setProcessor(null);

            assertTrue(MemoryViewModelTest.this.viewModel.getRows().isEmpty());
        }

        @Test
        @DisplayName("swapping processor repopulates rows from the new processor")
        void swappingProcessorRepopulatesRows() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);
            MemoryViewModelTest.this.viewModel.setProcessor(new CPU(MEM_SIZE));

            assertEquals(MEM_SIZE / MemoryRow.BYTES_PER_ROW,
                    MemoryViewModelTest.this.viewModel.getRows().size());
        }

        @Test
        @DisplayName("after swapping processor, access highlights are cleared")
        void swappingProcessorClearsAccessHighlights() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 0, new byte[1]));

            MemoryViewModelTest.this.viewModel.setProcessor(new CPU(MEM_SIZE));

            assertNull(MemoryViewModelTest.this.viewModel.getRows().get(0).getByteAccess(0));
        }
    }

    @Nested
    @DisplayName("Memory access")
    final class MemoryAccess {

        @BeforeEach
        void setUpProcessor() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);
        }

        @Test
        @DisplayName("READ access marks bytes as READ")
        void readAccessMarksBytesAsRead() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 4, new byte[2]));

            final var row = MemoryViewModelTest.this.viewModel.getRows().get(0);
            assertEquals(Access.READ, row.getByteAccess(4));
            assertEquals(Access.READ, row.getByteAccess(5));
        }

        @Test
        @DisplayName("READ access does not change shadow data")
        void readAccessDoesNotChangeShadow() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 2, new byte[] { 99 }));

            assertEquals(0, MemoryViewModelTest.this.viewModel.getRows().get(0).getByteValue(2));
        }

        @Test
        @DisplayName("WRITE access updates shadow data")
        void writeAccessUpdatesShadow() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 3, new byte[] { 42 }));

            assertEquals(42, MemoryViewModelTest.this.viewModel.getRows().get(0).getByteValue(3));
        }

        @Test
        @DisplayName("WRITE access marks bytes as WRITE")
        void writeAccessMarksBytesAsWrite() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 3, new byte[] { 42 }));

            assertEquals(Access.WRITE,
                    MemoryViewModelTest.this.viewModel.getRows().get(0).getByteAccess(3));
        }

        @Test
        @DisplayName("multi-byte WRITE updates every byte in the range")
        void multiByteWriteUpdatesRange() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 6, new byte[] { 1, 2, 3, 4 }));

            final var row = MemoryViewModelTest.this.viewModel.getRows().get(0);
            assertEquals(1, row.getByteValue(6));
            assertEquals(2, row.getByteValue(7));
            assertEquals(3, row.getByteValue(8));
            assertEquals(4, row.getByteValue(9));
        }

        @Test
        @DisplayName("WRITE spanning two rows updates both rows")
        void writeSpanningRowsUpdatesBothRows() {
            // Addresses 14–15 are row 0, cols 14–15; addresses 16–17 are row 1, cols 0–1.
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 14, new byte[] { 10, 20, 30, 40 }));

            final var row0 = MemoryViewModelTest.this.viewModel.getRows().get(0);
            final var row1 = MemoryViewModelTest.this.viewModel.getRows().get(1);
            assertEquals(10, row0.getByteValue(14));
            assertEquals(20, row0.getByteValue(15));
            assertEquals(30, row1.getByteValue(0));
            assertEquals(40, row1.getByteValue(1));
        }

        @Test
        @DisplayName("access exceeding memory boundary is safely truncated")
        void accessBeyondBoundaryIsTruncated() {
            // Write 4 bytes starting at address 30; only bytes 30 and 31 fit.
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 30, new byte[] { 1, 2, 3, 4 }));

            final var row1 = MemoryViewModelTest.this.viewModel.getRows().get(1);
            assertEquals(1, row1.getByteValue(14));
            assertEquals(2, row1.getByteValue(15));
        }

        @Test
        @DisplayName("access event toggles the refresh flag")
        void accessTogglesRefreshFlag() {
            final var before = MemoryViewModelTest.this.viewModel.getRefreshFlag();

            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 0, new byte[1]));

            assertTrue(before != MemoryViewModelTest.this.viewModel.getRefreshFlag());
        }
    }

    @Nested
    @DisplayName("Cycle handling")
    final class CycleHandling {

        @BeforeEach
        void setUpProcessor() {
            MemoryViewModelTest.this.viewModel.setProcessor(MemoryViewModelTest.this.cpu);
        }

        private CycleListener.Cycle cycleOf(final CycleListener.CycleState state) {
            return new CycleListener.Cycle(state, 0, 0, false,
                    MemoryViewModelTest.this.cpu.getPipelineSnapshot());
        }

        @Test
        @DisplayName("cycle START clears all access highlights")
        void cycleStartClearsAccessHighlights() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 4, new byte[1]));
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.WRITE, 20, new byte[] { 1 }));

            MemoryViewModelTest.this.viewModel.onCycle(cycleOf(CycleListener.CycleState.START));

            assertNull(MemoryViewModelTest.this.viewModel.getRows().get(0).getByteAccess(4));
            assertNull(MemoryViewModelTest.this.viewModel.getRows().get(1).getByteAccess(4));
        }

        @Test
        @DisplayName("cycle START toggles the refresh flag")
        void cycleStartTogglesRefreshFlag() {
            final var before = MemoryViewModelTest.this.viewModel.getRefreshFlag();

            MemoryViewModelTest.this.viewModel.onCycle(cycleOf(CycleListener.CycleState.START));

            assertTrue(before != MemoryViewModelTest.this.viewModel.getRefreshFlag());
        }

        @Test
        @DisplayName("cycle END does not clear access highlights")
        void cycleEndDoesNotClearAccessHighlights() {
            MemoryViewModelTest.this.viewModel.memoryAccessed(
                    new MemoryAccessListener.MemoryAccess(Access.READ, 5, new byte[1]));

            MemoryViewModelTest.this.viewModel.onCycle(cycleOf(CycleListener.CycleState.END));

            assertEquals(Access.READ,
                    MemoryViewModelTest.this.viewModel.getRows().get(0).getByteAccess(5));
        }
    }
}
