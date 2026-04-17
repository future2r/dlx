package name.ulbricht.dlx.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import name.ulbricht.dlx.asm.compiler.CompiledProgram;
import name.ulbricht.dlx.asm.compiler.Compiler;
import name.ulbricht.dlx.asm.lexer.Lexer;
import name.ulbricht.dlx.asm.lexer.LexerMode;
import name.ulbricht.dlx.asm.parser.Parser;

/// Verifies the cycle-by-cycle pipeline state for a small example program.
///
/// The program under test:
/// ```asm
///         .data
/// a:      .word 10
/// b:      .word 32
/// res:    .word 0
///         .text
/// main:   lw r1, a(r0)
///         lw r2, b(r0)
///         add r3, r1, r2
///         sw res(r0), r3
///         trap 0
/// ```
///
/// Memory layout (code-first):
/// - `0x00`: `0x8C010014` (LW R1, 20(R0))
/// - `0x04`: `0x8C020018` (LW R2, 24(R0))
/// - `0x08`: `0x00221820` (ADD R3, R1, R2)
/// - `0x0C`: `0xAC03001C` (SW 28(R0), R3)
/// - `0x10`: `0xFC000000` (TRAP #0x00)
/// - `0x14`: `0x0000000A` (a = 10)
/// - `0x18`: `0x00000020` (b = 32)
/// - `0x1C`: `0x00000000` (res = 0)
@DisplayName("Pipeline")
final class PipelineTest {

    /// Instruction words for reference in assertions.
    private static final int LW_R1 = 0x8C010014;
    private static final int LW_R2 = 0x8C020018;
    private static final int ADD_R3 = 0x00221820;
    private static final int SW_RES = 0xAC03001C;
    private static final int TRAP_0 = 0xFC000000;
    private static final int NOP = 0x00000000;

    private CPU cpu;

    @BeforeEach
    void setUp() {
        final var compiled = compile("""
                .data
                a:   .word 10
                b:   .word 32
                res: .word 0
                .text
                lw r1, a(r0)
                lw r2, b(r0)
                add r3, r1, r2
                sw res(r0), r3
                trap 0""");
        assertFalse(compiled.hasErrors(),
                "Expected no errors but got: " + compiled.diagnostics());

        this.cpu = new CPU();
        this.cpu.loadProgram(compiled.program());
    }

    private CPU.PipelineSnapshot step() throws InterruptedException {
        this.cpu.step();
        return this.cpu.getPipelineSnapshot();
    }

    @Nested
    @DisplayName("Pipeline filling")
    class PipelineFilling {

        @Test
        @DisplayName("Initial state: all latches are bubbles")
        void initialState() {
            final var snap = PipelineTest.this.cpu.getPipelineSnapshot();

            assertSame(IfIdLatch.BUBBLE, snap.ifId());
            assertSame(IdExLatch.BUBBLE, snap.idEx());
            assertEquals(ExMemLatch.BUBBLE, snap.exMem());
            assertEquals(MemWbLatch.BUBBLE, snap.memWb());

            assertEquals(0x00, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(0, PipelineTest.this.cpu.getCycles());
            assertFalse(PipelineTest.this.cpu.isHalted());
        }

        @Test
        @DisplayName("Cycle 1: IF fetches LW R1")
        void cycle1() throws InterruptedException {
            final var snap = step();

            // IF/ID: LW R1, 20(R0) fetched from address 0x00
            assertEquals(0x00, snap.ifId().pc());
            assertEquals(LW_R1, snap.ifId().instructionWord());

            // All other latches still bubbles
            assertSame(IdExLatch.BUBBLE, snap.idEx());
            assertEquals(ExMemLatch.BUBBLE, snap.exMem());
            assertEquals(MemWbLatch.BUBBLE, snap.memWb());

            assertEquals(0x04, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(1, PipelineTest.this.cpu.getCycles());
        }

        @Test
        @DisplayName("Cycle 2: LW R1 decoded in ID, IF fetches LW R2")
        void cycle2() throws InterruptedException {
            step(); // cycle 1
            final var snap = step();

            // IF/ID: LW R2, 24(R0)
            assertEquals(0x04, snap.ifId().pc());
            assertEquals(LW_R2, snap.ifId().instructionWord());

            // ID/EX: LW R1 decoded — rs1=R0, rd=R1, immediate=20 (a at 0x14)
            final var idEx = snap.idEx();
            assertEquals(0x00, idEx.pc());
            assertTrue(idEx.ctrl().memory().memRead(), "LW R1 is a load");
            assertTrue(idEx.ctrl().regWrite(), "LW R1 writes a register");
            assertTrue(idEx.ctrl().memToReg(), "LW R1 selects memory data");
            assertEquals(0, idEx.rs1());
            assertEquals(0, idEx.rs1Val());
            assertEquals(1, idEx.rd());
            assertEquals(20, idEx.immediate());

            assertEquals(ExMemLatch.BUBBLE, snap.exMem());
            assertEquals(MemWbLatch.BUBBLE, snap.memWb());

            assertEquals(0x08, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(2, PipelineTest.this.cpu.getCycles());
        }

        @Test
        @DisplayName("Cycle 3: LW R1 in EX, LW R2 in ID, IF fetches ADD")
        void cycle3() throws InterruptedException {
            step(); // cycle 1
            step(); // cycle 2
            final var snap = step();

            // IF/ID: ADD R3, R1, R2
            assertEquals(0x08, snap.ifId().pc());
            assertEquals(ADD_R3, snap.ifId().instructionWord());

            // ID/EX: LW R2 decoded — rs1=R0, rd=R2, immediate=24 (b at 0x18)
            final var idEx = snap.idEx();
            assertEquals(0x04, idEx.pc());
            assertTrue(idEx.ctrl().memory().memRead());
            assertEquals(0, idEx.rs1());
            assertEquals(2, idEx.rd());
            assertEquals(24, idEx.immediate());

            // EX/MEM: LW R1 executed — effective address = R0 + 20 = 20
            final var exMem = snap.exMem();
            assertTrue(exMem.ctrl().memory().memRead());
            assertEquals(0x00000014, exMem.aluResult());
            assertEquals(1, exMem.rd());

            assertEquals(MemWbLatch.BUBBLE, snap.memWb());

            assertEquals(0x0C, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(3, PipelineTest.this.cpu.getCycles());
        }
    }

    @Nested
    @DisplayName("Load-use hazard stall")
    class LoadUseHazard {

        @Test
        @DisplayName("Cycle 4: stall — ADD depends on R2 from LW R2 in EX")
        void cycle4_stall() throws InterruptedException {
            step(); // cycle 1
            step(); // cycle 2
            step(); // cycle 3
            final var snap = step();

            // IF/ID: ADD R3, R1, R2 — frozen, same as cycle 3
            assertEquals(0x08, snap.ifId().pc());
            assertEquals(ADD_R3, snap.ifId().instructionWord());

            // ID/EX: bubble injected by stall
            assertSame(IdExLatch.BUBBLE, snap.idEx());

            // EX/MEM: LW R2 executed — effective address = R0 + 24 = 24
            final var exMem = snap.exMem();
            assertTrue(exMem.ctrl().memory().memRead());
            assertEquals(0x00000018, exMem.aluResult());
            assertEquals(2, exMem.rd());

            // MEM/WB: LW R1 completed memory read — loaded value 10
            final var memWb = snap.memWb();
            assertTrue(memWb.ctrl().memory().memRead());
            assertTrue(memWb.ctrl().memToReg());
            assertEquals(0x00000014, memWb.aluResult());
            assertEquals(10, memWb.memData());
            assertEquals(1, memWb.rd());

            // PC did not advance (stall)
            assertEquals(0x0C, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(4, PipelineTest.this.cpu.getCycles());
        }

        @Test
        @DisplayName("Cycle 5: stall resolved — ADD enters ID, WB writes R1=10")
        void cycle5_stallResolved() throws InterruptedException {
            step(); // cycle 1
            step(); // cycle 2
            step(); // cycle 3
            step(); // cycle 4 (stall)
            final var snap = step();

            // IF/ID: SW 28(R0), R3
            assertEquals(0x0C, snap.ifId().pc());
            assertEquals(SW_RES, snap.ifId().instructionWord());

            // ID/EX: ADD decoded — rs1=R1, rs2=R2, rd=R3
            // R1 was just written by WB (=10), R2 still 0 (in MEM, not yet written)
            final var idEx = snap.idEx();
            assertEquals(0x08, idEx.pc());
            assertFalse(idEx.ctrl().memory().memRead(), "ADD is not a load");
            assertTrue(idEx.ctrl().regWrite(), "ADD writes a register");
            assertFalse(idEx.ctrl().memToReg(), "ADD uses ALU result");
            assertEquals(1, idEx.rs1());
            assertEquals(10, idEx.rs1Val());
            assertEquals(2, idEx.rs2());
            assertEquals(0, idEx.rs2Val()); // R2 not yet written back
            assertEquals(3, idEx.rd());
            assertEquals(0, idEx.immediate());

            // EX/MEM: bubble (from stall injection in cycle 4)
            assertEquals(ExMemLatch.BUBBLE, snap.exMem());

            // MEM/WB: LW R2 completed memory read — loaded value 32
            final var memWb = snap.memWb();
            assertTrue(memWb.ctrl().memory().memRead());
            assertEquals(0x00000018, memWb.aluResult());
            assertEquals(32, memWb.memData());
            assertEquals(2, memWb.rd());

            // R1 was written by WB this cycle
            assertEquals(10, PipelineTest.this.cpu.getRegisters().read(1));

            assertEquals(0x10, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(5, PipelineTest.this.cpu.getCycles());
        }
    }

    @Nested
    @DisplayName("Forwarding and execution")
    class ForwardingAndExecution {

        @Test
        @DisplayName("Cycle 6: ADD executes with R2 forwarded from MEM/WB, result = 42")
        void cycle6_addExecutes() throws InterruptedException {
            step(); // cycle 1
            step(); // cycle 2
            step(); // cycle 3
            step(); // cycle 4 (stall)
            step(); // cycle 5
            final var snap = step();

            // IF/ID: TRAP #0x00
            assertEquals(0x10, snap.ifId().pc());
            assertEquals(TRAP_0, snap.ifId().instructionWord());

            // ID/EX: SW decoded — rs1=R0 (base), rs2=R3 (data), rd=R0 (no writeback)
            final var idEx = snap.idEx();
            assertEquals(0x0C, idEx.pc());
            assertTrue(idEx.ctrl().memory().memWrite(), "SW is a store");
            assertFalse(idEx.ctrl().regWrite(), "SW does not write a register");
            assertEquals(0, idEx.rs1());
            assertEquals(3, idEx.rs2());
            assertEquals(0, idEx.rd());
            assertEquals(28, idEx.immediate());

            // EX/MEM: ADD executed — 10 + 32 = 42 (R2 was forwarded from MEM/WB)
            final var exMem = snap.exMem();
            assertTrue(exMem.ctrl().regWrite());
            assertFalse(exMem.ctrl().memory().memRead());
            assertFalse(exMem.ctrl().memory().memWrite());
            assertEquals(42, exMem.aluResult());
            assertEquals(3, exMem.rd());

            // MEM/WB: bubble (from the stall bubble that flowed through EX)
            assertEquals(MemWbLatch.BUBBLE, snap.memWb());

            // R2 was written by WB this cycle
            assertEquals(32, PipelineTest.this.cpu.getRegisters().read(2));

            assertEquals(0x14, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(6, PipelineTest.this.cpu.getCycles());
        }

        @Test
        @DisplayName("Cycle 7: SW executes with R3 forwarded from EX/MEM, TRAP enters ID")
        void cycle7_swExecutes() throws InterruptedException {
            step(); // cycle 1
            step(); // cycle 2
            step(); // cycle 3
            step(); // cycle 4 (stall)
            step(); // cycle 5
            step(); // cycle 6
            final var snap = step();

            // IF/ID: data word a=10 fetched from 0x14 (first data address, code-first layout)
            // trapFlush is not yet active — TRAP is still in ID, not EX
            assertEquals(0x14, snap.ifId().pc());
            assertEquals(0x0000000A, snap.ifId().instructionWord());

            // ID/EX: TRAP decoded
            final var idEx = snap.idEx();
            assertEquals(0x10, idEx.pc());
            assertTrue(idEx.ctrl().trap(), "instruction is a trap");
            assertEquals(0, idEx.immediate());

            // EX/MEM: SW executed — effective address = R0 + 28 = 28,
            // store data = 42 (forwarded from EX/MEM for R3)
            final var exMem = snap.exMem();
            assertTrue(exMem.ctrl().memory().memWrite(), "SW writes memory");
            assertFalse(exMem.ctrl().regWrite(), "SW does not write a register");
            assertEquals(28, exMem.aluResult());
            assertEquals(42, exMem.rs2Val());
            assertEquals(0, exMem.rd());

            // MEM/WB: ADD result passes through MEM (no memory access)
            final var memWb = snap.memWb();
            assertTrue(memWb.ctrl().regWrite());
            assertFalse(memWb.ctrl().memory().memRead());
            assertEquals(42, memWb.aluResult());
            assertEquals(3, memWb.rd());

            assertEquals(0x18, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(7, PipelineTest.this.cpu.getCycles());
        }
    }

    @Nested
    @DisplayName("Trap flush and pipeline drain")
    class TrapFlushAndDrain {

        @Test
        @DisplayName("Cycle 8: TRAP in EX flushes IF, SW writes memory[0x1C]=42, WB writes R3=42")
        void cycle8_trapFlush() throws InterruptedException {
            stepTo(8);
            final var snap = PipelineTest.this.cpu.getPipelineSnapshot();

            // IF/ID: bubble — trap flush suppressed IF
            assertSame(IfIdLatch.BUBBLE, snap.ifId());

            // ID/EX: bubble — data word at 0x14 has invalid opcode, caught by trapFlush safety
            assertSame(IdExLatch.BUBBLE, snap.idEx());

            // EX/MEM: TRAP executed
            final var exMem = snap.exMem();
            assertTrue(exMem.ctrl().trap());
            assertEquals(0, exMem.rd());

            // MEM/WB: SW completed — memory write happened this cycle
            final var memWb = snap.memWb();
            assertTrue(memWb.ctrl().memory().memWrite());
            assertFalse(memWb.ctrl().regWrite());

            // Verify side effects: R3=42 written by WB, memory[0x1C]=42 written by MEM
            assertEquals(42, PipelineTest.this.cpu.getRegisters().read(3));
            assertEquals(42, PipelineTest.this.cpu.getMemory().loadWord(0x1C));

            // PC did not advance (trap flush held it)
            assertEquals(0x18, PipelineTest.this.cpu.getProgramCounter());
            assertEquals(8, PipelineTest.this.cpu.getCycles());
            assertFalse(PipelineTest.this.cpu.isHalted());
        }

        @Test
        @DisplayName("Cycle 9: TRAP drains through MEM, pipeline nearly empty")
        void cycle9_trapDrains() throws InterruptedException {
            stepTo(9);
            final var snap = PipelineTest.this.cpu.getPipelineSnapshot();

            // IF/ID: data word b=32 fetched from 0x18 (IF resumed since no trapFlush this cycle)
            assertEquals(0x18, snap.ifId().pc());
            assertEquals(0x00000020, snap.ifId().instructionWord());

            // ID/EX: bubble
            assertSame(IdExLatch.BUBBLE, snap.idEx());

            // EX/MEM: bubble
            assertEquals(ExMemLatch.BUBBLE, snap.exMem());

            // MEM/WB: TRAP passed through MEM — halted is now true
            final var memWb = snap.memWb();
            assertTrue(memWb.ctrl().trap());
            assertEquals(0, memWb.immediate());

            assertEquals(9, PipelineTest.this.cpu.getCycles());
            assertTrue(PipelineTest.this.cpu.isHalted(), "CPU halted when TRAP exits MEM");
        }

        @Test
        @DisplayName("Halted CPU does not advance on further step calls")
        void haltedCpuIsNoOp() throws InterruptedException {
            stepTo(9);
            assertTrue(PipelineTest.this.cpu.isHalted());

            final var snapBefore = PipelineTest.this.cpu.getPipelineSnapshot();
            final var cyclesBefore = PipelineTest.this.cpu.getCycles();

            PipelineTest.this.cpu.step(); // should be a no-op

            assertEquals(cyclesBefore, PipelineTest.this.cpu.getCycles());
            assertEquals(snapBefore, PipelineTest.this.cpu.getPipelineSnapshot());
        }
    }

    @Nested
    @DisplayName("Final program results")
    class FinalResults {

        @Test
        @DisplayName("Program completes in 9 cycles with correct register and memory state")
        void programCompletesCorrectly() throws InterruptedException {
            PipelineTest.this.cpu.run();

            assertTrue(PipelineTest.this.cpu.isHalted());
            assertEquals(9, PipelineTest.this.cpu.getCycles());

            // Registers
            assertEquals(10, PipelineTest.this.cpu.getRegisters().read(1));
            assertEquals(32, PipelineTest.this.cpu.getRegisters().read(2));
            assertEquals(42, PipelineTest.this.cpu.getRegisters().read(3));

            // Memory: res at address 0x1C (28) = 42
            assertEquals(42, PipelineTest.this.cpu.getMemory().loadWord(0x1C));
        }
    }

    @Nested
    @DisplayName("Instruction formatter")
    class InstructionFormatting {

        @Test
        @DisplayName("Formats all instructions in the example program")
        void formatsExampleInstructions() {
            assertEquals("NOP", InstructionFormatter.format(NOP));
            assertEquals("LW R1, 20(R0)", InstructionFormatter.format(LW_R1));
            assertEquals("LW R2, 24(R0)", InstructionFormatter.format(LW_R2));
            assertEquals("ADD R3, R1, R2", InstructionFormatter.format(ADD_R3));
            assertEquals("SW 28(R0), R3", InstructionFormatter.format(SW_RES));
            assertEquals("TRAP #0x00", InstructionFormatter.format(TRAP_0));
        }
    }

    @Nested
    @DisplayName("Cycle includes pipeline snapshot")
    class CycleSnapshot {

        @Test
        @DisplayName("cycleEnd delivers post-commit pipeline snapshot matching CPU state")
        void listenerReceivesSnapshot() throws InterruptedException {
            // cycleEnd fires AFTER commitCycle(), so it delivers the post-cycle
            // state. Run one cycle, capture the cycle snapshot, then compare it
            // against a snapshot taken directly from the CPU after the step.
            final var holder = new CycleListener.Cycle[1];
            PipelineTest.this.cpu.addCycleListener(cycle -> holder[0] = cycle);

            PipelineTest.this.cpu.step(); // cycle 1 — fills IF/ID with LW R1

            final var expectedSnapshot = PipelineTest.this.cpu.getPipelineSnapshot();
            final var expectedCycles = PipelineTest.this.cpu.getCycles();
            final var expectedPc = PipelineTest.this.cpu.getProgramCounter();

            final var cycle = holder[0];
            assertEquals(expectedCycles, cycle.cycles());
            assertEquals(expectedPc, cycle.programCounter());
            assertEquals(expectedSnapshot, cycle.pipeline());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void stepTo(final int targetCycle) throws InterruptedException {
        while (this.cpu.getCycles() < targetCycle) {
            this.cpu.step();
        }
    }

    private static CompiledProgram compile(final String source) {
        final var tokenized = new Lexer(LexerMode.ASSEMBLER).tokenize(UUID.randomUUID(), source);
        final var parsed = new Parser().parse(tokenized);
        return new Compiler().compile(parsed);
    }
}
