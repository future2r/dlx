package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/// DLX CPU with a classic 5-stage in-order pipeline: **IF → ID → EX → MEM →
/// WB**.
///
/// ## Pipeline model
/// Each call to [#step()] models one complete clock cycle. The method follows
/// the *snapshot-then-commit* protocol:
///
/// 1. All five stages are evaluated against the **current** (pre-commit) latch
///    values - simulating the combinational logic that runs between two clock
///    edges.
/// 2. At the end of `step()` every new latch value is written atomically -
///    simulating the rising clock edge that captures all flip-flop outputs
///    simultaneously.
///
/// This prevents mid-cycle data races: a value computed by EX this cycle cannot
/// leak into MEM within the same cycle.
///
/// ## Stage evaluation order inside `step()`
/// Although the stages are conceptually parallel, the Java implementation must
/// call them sequentially. The order is chosen so that later-stage results (MEM,
/// WB) are computed first and are available as forwarding sources when EX runs:
///
/// ```
/// WB  → MEM → EX → ID → IF
/// ```
///
/// This means `newMemWb` (computed by MEM) is passed directly to EX as the
/// MEM/WB forwarding source, reflecting the behaviour of real hardware where the
/// MEM stage completes slightly before EX needs its result.
///
/// ## Hazard handling
///
/// ### Load-use hazard (stall)
/// Detected by [HazardDetectionUnit] before any stage runs. When a load
/// instruction is in EX and the immediately following instruction (in ID) reads
/// the loaded register:
///
/// - A [IdExLatch#BUBBLE] is injected into the ID/EX latch (the dependent
///   instruction does not advance to EX this cycle).
/// - The IF/ID latch is frozen (the same instruction will be re-decoded next
///   cycle).
/// - The PC is not incremented.
///
/// ### Data hazards (forwarding)
/// All other RAW hazards are resolved transparently by [Forwarding] inside
/// [ExecuteStage]. No stall is needed.
///
/// ### Trap flush
/// When a trap reaches the EX stage, the IF/ID and ID/EX latches are flushed
/// with bubbles. This prevents instructions fetched from beyond the program end
/// from advancing into MEM, where a spurious load or store (decoded from
/// uninitialised memory) could cause out-of-bounds access or data corruption.
///
/// ### Control hazards (branch / jump)
/// Branch conditions and jump targets are resolved at the end of EX. When a
/// redirect is required:
///
/// - The PC is set to the target address.
/// - The IF/ID and ID/EX latches are flushed with bubbles (2-cycle penalty).
///
/// ## Trap
/// The `trap` instruction (opcode `0x3F`) with immediate `0` sets
/// [#isHalted()] to `true` once it reaches the WB stage. At that point the
/// entire pipeline has drained past the instruction, ensuring that all preceding
/// stores and register writes have committed.
public final class CPU {

    private static final int DEFAULT_MEMORY_SIZE = 1024;
    private static final String ERROR_IF_ID_NULL = "ifId must not be null";
    private static final String ERROR_ID_EX_NULL = "idEx must not be null";

    /// The register file containing R0–R31.
    ///
    /// R0 is hardwired to zero. Callers may read any register via
    /// [Registers#read] or inspect all registers at once with [Registers#snapshot].
    private final Registers registers = new Registers();

    /// The flat byte-addressable data/instruction memory.
    ///
    /// Programs are loaded here by [#loadProgram] and fetched by the IF stage
    /// each cycle.
    private final Memory memory;

    /// The ALU used by the EX stage.
    private final ALU alu = new ALU();

    /// The time a stage will take. Usually, a stage is very fast. For testing and
    /// demonstration purposes, this will slow down the simulation in order to
    /// observe the pipeline behaviour more clearly.
    private Duration stageDuration = Duration.ZERO;

    /// Listeners for cycle start and end events.
    private final List<CycleListener> cycleListeners = new ArrayList<>();

    /// Listeners for non-halt trap retirement.
    private final List<TrapListener> trapListeners = new ArrayList<>();

    /// The program counter - the byte address of the **next** instruction to be
    /// fetched by the IF stage. Updated at the end of every `step()`.
    private int programCounter;

    /// Set to `true` once a `trap 0` instruction retires through WB.
    /// [#step()] becomes a no-op after this point.
    private boolean halted;

    /// Total number of clock cycles executed since the last [#loadProgram].
    private long cycles;

    // Inter-stage pipeline latches - all initialised to their bubble values
    // so the pipeline starts in a clean, empty state.

    /// Latch between IF and ID stages.
    private IfIdLatch ifId = IfIdLatch.BUBBLE;

    /// Latch between ID and EX stages.
    private IdExLatch idEx = IdExLatch.BUBBLE;

    /// Latch between EX and MEM stages.
    private ExMemLatch exMem = ExMemLatch.BUBBLE;

    /// Latch between MEM and WB stages.
    private MemWbLatch memWb = MemWbLatch.BUBBLE;

    /// Creates a new CPU with a freshly allocated zero-initialised memory of a
    /// default size.
    public CPU() {
        this(DEFAULT_MEMORY_SIZE);
    }

    /// Creates a new CPU with a freshly allocated zero-initialised memory.
    ///
    /// @param memorySizeBytes the total size of the simulated memory in bytes; must
    ///                        be large enough to hold the program and any data
    ///                        it accesses
    public CPU(final int memorySizeBytes) {
        this.memory = new Memory(memorySizeBytes);
    }

    /// Returns the delay inserted after each pipeline cycle. Defaults to
    /// [Duration#ZERO] (no delay). A non-zero value slows down the simulation for
    /// better observability in a UI.
    ///
    /// @return the current stage duration
    public Duration getStageDuration() {
        return this.stageDuration;
    }

    /// Sets the delay inserted after each pipeline cycle.
    ///
    /// @param stageDuration the duration to sleep after each cycle; must not
    ///                      be `null`
    public void setStageDuration(final Duration stageDuration) {
        this.stageDuration = requireNonNull(stageDuration, "stageDuration must not be null");
    }

    /// Returns the registers, which can be read by callers but not modified
    /// directly.
    ///
    /// @return a read-only view of the register file
    public ReadOnlyRegisters getRegisters() {
        return this.registers.asReadOnly();
    }

    /// Returns the memory, which can be read by callers but not modified directly.
    ///
    /// @return a read-only view of the memory
    public ReadOnlyMemory getMemory() {
        return this.memory.asReadOnly();
    }

    /// Loads a program and resets all CPU state to its initial values.
    ///
    /// The program bytes are written to memory starting at address 0 and the PC is
    /// set to 0 (the first instruction). The cycle counter is reset and all four
    /// pipeline latches are flushed to their bubble values. Memory outside the
    /// program area retains its previous contents (or zero on first load).
    ///
    /// @param program array of bytes representing the program; must not be `null`;
    ///                `program[0]` is placed at address 0 and executed first
    public void loadProgram(final byte[] program) {
        requireNonNull(program, "program must not be null");

        // Reset all mutable CPU state.
        this.programCounter = 0;
        this.halted = false;
        this.cycles = 0;

        // Flush the pipeline - no instructions are in flight.
        this.ifId = IfIdLatch.BUBBLE;
        this.idEx = IdExLatch.BUBBLE;
        this.exMem = ExMemLatch.BUBBLE;
        this.memWb = MemWbLatch.BUBBLE;

        notifyCycleListeners(CycleListener.CycleState.END);

        // Write the program bytes into memory.
        this.memory.storeProgram(program);
    }

    /// Advances the simulation by exactly one clock cycle.
    ///
    /// If the CPU is already halted this method returns immediately without
    /// modifying any state.
    ///
    /// The five stages are evaluated in reverse pipeline order (WB first, IF last)
    /// so that each stage's output is available as a forwarding source for earlier
    /// stages within the same cycle.
    /// 
    /// @throws InterruptedException if the thread is interrupted while sleeping to
    ///                              simulate stage processing time
    public void step() throws InterruptedException {

        // A halted CPU does nothing.
        if (this.halted)
            return;

        notifyCycleListeners(CycleListener.CycleState.START);

        // -----------------------------------------------------------------
        // Pre-stage: detect load-use hazard using the current latch values.
        // The result governs whether IF and ID are frozen this cycle.
        // -----------------------------------------------------------------
        final var stall = HazardDetectionUnit.detectLoadUseHazard(this.idEx, this.ifId);

        // -----------------------------------------------------------------
        // WB stage: write the result from the previous MEM stage to the
        // register file. Uses the current (pre-commit) memWb snapshot.
        // -----------------------------------------------------------------
        WriteBackStage.execute(this.memWb, this.registers);

        // -----------------------------------------------------------------
        // MEM stage: perform the memory access (load or store) for the
        // instruction that just left EX. Produces the new MEM/WB latch.
        // -----------------------------------------------------------------
        final var newMemWb = MemoryAccessStage.execute(this.exMem, this.memory);

        // -----------------------------------------------------------------
        // EX stage: apply forwarding, run the ALU, evaluate branch/jump.
        // this.memWb is the pre-commit snapshot representing the instruction
        // currently in WB (2 cycles ahead of the consumer in EX). It is
        // used as the MEM/WB forwarding source.
        // -----------------------------------------------------------------
        final var exResult = ExecuteStage.execute(this.idEx, this.exMem, this.memWb, this.alu);
        final var newExMem = exResult.exMem();

        // -----------------------------------------------------------------
        // Trap / fetch-suppress flag: when a trap is in EX, IF must not issue
        // another fetch. The already-fetched next instruction in IF/ID is
        // still allowed to advance so sequences like `trap 1` / `trap 0`
        // continue to work.
        // -----------------------------------------------------------------
        final var trapFlush = this.idEx.ctrl().trap();
        final var newIdEx = decodeNextIdEx(stall, trapFlush);
        final var ifStageDecision = decideIfStage(stall, exResult, trapFlush, newIdEx);

        commitCycle(ifStageDecision, newExMem, newMemWb);
        notifyCycleListeners(CycleListener.CycleState.END);
        handleRetiredTrap(newMemWb);

        // Wait to simulate processing time
        Thread.sleep(this.stageDuration);
    }

    private IdExLatch decodeNextIdEx(final boolean stall, final boolean trapFlush) {
        if (stall)
            return IdExLatch.BUBBLE;
        if (trapFlush) {
            // The trap flush allows the already-fetched instruction in IF/ID to advance
            // (needed for trap 1 / trap 0 sequences). However, if IF/ID holds a data word
            // rather than a valid instruction (possible with code-first memory layout), the
            // decoder will throw. Treat any such word as a bubble — it will never commit.
            try {
                return InstructionDecodeStage.execute(this.ifId, this.registers);
            } catch (final IllegalArgumentException _) {
                return IdExLatch.BUBBLE;
            }
        }
        return InstructionDecodeStage.execute(this.ifId, this.registers);
    }

    private IfStageDecision decideIfStage(
            final boolean stall,
            final ExecuteResult exResult,
            final boolean trapFlush,
            final IdExLatch decodedIdEx) {
        requireNonNull(exResult, "exResult must not be null");
        requireNonNull(decodedIdEx, "decodedIdEx must not be null");

        if (exResult.pcRedirect()) {
            return new IfStageDecision(IfIdLatch.BUBBLE, IdExLatch.BUBBLE, exResult.newPc());
        }
        if (trapFlush) {
            return new IfStageDecision(IfIdLatch.BUBBLE, decodedIdEx, this.programCounter);
        }
        if (stall) {
            return new IfStageDecision(this.ifId, decodedIdEx, this.programCounter);
        }

        final var fetchedIfId = InstructionFetchStage.execute(this.programCounter, this.memory);
        return new IfStageDecision(fetchedIfId, decodedIdEx, this.programCounter + 4);
    }

    private void commitCycle(
            final IfStageDecision ifStageDecision,
            final ExMemLatch newExMem,
            final MemWbLatch newMemWb) {
        requireNonNull(ifStageDecision, "ifStageDecision must not be null");
        requireNonNull(newExMem, "newExMem must not be null");
        requireNonNull(newMemWb, "newMemWb must not be null");

        this.ifId = ifStageDecision.ifId();
        this.idEx = ifStageDecision.idEx();
        this.exMem = newExMem;
        this.memWb = newMemWb;
        this.programCounter = ifStageDecision.newPc();
        this.cycles++;
    }

    private void handleRetiredTrap(final MemWbLatch newMemWb) {
        requireNonNull(newMemWb, "newMemWb must not be null");

        if (newMemWb.ctrl().trap()) {
            final var trapNumber = newMemWb.immediate();
            if (trapNumber == 0) {
                this.halted = true;
            } else {
                notifyTrapListeners(trapNumber);
            }
        }
    }

    /// Runs the simulation until a `trap 0` instruction retires or `maxCycles`
    /// clock cycles have been executed.
    ///
    /// Use [Long#MAX_VALUE] for `maxCycles` to run without a cycle limit, but be
    /// aware that a program without a `trap 0` instruction will then loop forever.
    ///
    /// @param maxCycles the maximum number of cycles to execute before throwing;
    ///                  must be positive
    /// @throws IllegalStateException if the cycle limit is reached before a `trap 0`
    ///                               instruction retires
    /// @throws InterruptedException  if the thread is interrupted while sleeping to
    ///                               simulate stage processing time
    public void run(final long maxCycles) throws InterruptedException {
        while (!this.halted && this.cycles < maxCycles) {
            step();
        }
        if (!this.halted) {
            throw new IllegalStateException(
                    "Simulation did not halt within " + maxCycles + " cycles");
        }
    }

    /// Runs the simulation until a `trap 0` instruction retires, with no cycle
    /// limit.
    ///
    /// Equivalent to `run(Long.MAX_VALUE)`. A program that never executes a `trap 0`
    /// instruction will cause this method to loop indefinitely.
    /// 
    /// @throws InterruptedException if the thread is interrupted while sleeping to
    ///                              simulate stage processing time
    public void run() throws InterruptedException {
        run(Long.MAX_VALUE);
    }

    // -------------------------------------------------------------------------
    // Observability
    // -------------------------------------------------------------------------

    /// Returns `true` once a `trap 0` instruction has retired through the WB stage.
    /// [#step()] is a no-op when `isHalted()` returns `true`.
    ///
    /// @return `true` if the simulation has completed
    public boolean isHalted() {
        return this.halted;
    }

    /// Returns the current program counter - the address of the instruction that
    /// will be fetched on the **next** call to [#step()].
    ///
    /// After a trap retires this value points to the instruction immediately
    /// following the trap.
    ///
    /// @return the PC as a byte address
    public int getProgramCounter() {
        return this.programCounter;
    }

    /// Returns the total number of clock cycles that have been executed since the
    /// last [#loadProgram] call.
    ///
    /// Stall cycles count toward this total; bubble injections do not add extra
    /// cycles.
    ///
    /// @return the cycle count
    public long getCycles() {
        return this.cycles;
    }

    /// Registers a listener to be notified at the start and end of every cycle.
    ///
    /// @param listener the listener to register; must not be `null`
    public synchronized void addCycleListener(final CycleListener listener) {
        this.cycleListeners.add(listener);
    }

    /// Unregisters a previously registered cycle listener.
    ///
    /// @param listener the listener to unregister; must not be `null`
    public synchronized void removeCycleListener(final CycleListener listener) {
        this.cycleListeners.remove(listener);
    }

    /// Registers a listener to be notified when a non-halt trap retires.
    ///
    /// @param listener the listener to register; must not be `null`
    public synchronized void addTrapListener(final TrapListener listener) {
        this.trapListeners.add(listener);
    }

    /// Unregisters a previously registered trap listener.
    ///
    /// @param listener the listener to unregister; must not be `null`
    public synchronized void removeTrapListener(final TrapListener listener) {
        this.trapListeners.remove(listener);
    }

    private void notifyCycleListeners(final CycleListener.CycleState state) {
        final List<CycleListener> current;
        synchronized (this) {
            if (this.cycleListeners.isEmpty())
                return;
            current = List.copyOf(this.cycleListeners);
        }
        final var pipeline = new PipelineSnapshot(this.ifId, this.idEx, this.exMem, this.memWb);
        final var cycle = new CycleListener.Cycle(state, this.cycles, this.programCounter, this.halted, pipeline);
        current.forEach(listener -> listener.onCycle(cycle));
    }

    private void notifyTrapListeners(final int trapNumber) {
        final List<TrapListener> currentListeners;
        synchronized (this) {
            if (this.trapListeners.isEmpty())
                return;
            currentListeners = List.copyOf(this.trapListeners);
        }

        final var event = new TrapListener.TrapEvent(trapNumber, this.registers.asReadOnly(),
                this.memory.asReadOnly());
        currentListeners.forEach(listener -> listener.trapRetired(event));
    }

    /// Returns an immutable snapshot of all four pipeline latches at the current
    /// point in simulation time.
    ///
    /// The snapshot can be inspected or displayed without risk of it changing under
    /// the observer. Useful for a step-by-step UI or a test asserting on pipeline
    /// state mid-execution.
    ///
    /// @return a [PipelineSnapshot] capturing the current latch values
    public PipelineSnapshot getPipelineSnapshot() {
        return new PipelineSnapshot(this.ifId, this.idEx, this.exMem, this.memWb);
    }

    // -------------------------------------------------------------------------
    // Nested types
    // -------------------------------------------------------------------------

    /// Immutable decision for the next IF/ID latch, ID/EX latch, and PC.
    ///
    /// Used internally by [CPU#step()] to keep fetch/flush logic separate from the
    /// atomic commit phase.
    ///
    /// @param ifId  the next IF/ID latch value
    /// @param idEx  the next ID/EX latch value
    /// @param newPc the next program counter value
    private record IfStageDecision(
            IfIdLatch ifId,
            IdExLatch idEx,
            int newPc) {

        /// Validates that no latch parameter is `null`.
        ///
        /// @throws NullPointerException if any latch parameter is `null`
        private IfStageDecision {
            requireNonNull(ifId, ERROR_IF_ID_NULL);
            requireNonNull(idEx, ERROR_ID_EX_NULL);
        }
    }

    /// Immutable snapshot of all four inter-stage pipeline latches captured at a
    /// single point in simulation time.
    ///
    /// Returned by [CPU#getPipelineSnapshot()]; intended for display, debugging, and
    /// testing.
    ///
    /// @param ifId  the IF/ID latch value at the time of the snapshot
    /// @param idEx  the ID/EX latch value at the time of the snapshot
    /// @param exMem the EX/MEM latch value at the time of the snapshot
    /// @param memWb the MEM/WB latch value at the time of the snapshot
    public record PipelineSnapshot(
            IfIdLatch ifId,
            IdExLatch idEx,
            ExMemLatch exMem,
            MemWbLatch memWb) {

        /// Validates that no parameter is `null`.
        ///
        /// @throws NullPointerException if any parameter is `null`
        public PipelineSnapshot {
            requireNonNull(ifId, ERROR_IF_ID_NULL);
            requireNonNull(idEx, ERROR_ID_EX_NULL);
            requireNonNull(exMem, "exMem must not be null");
            requireNonNull(memWb, "memWb must not be null");
        }
    }
}
