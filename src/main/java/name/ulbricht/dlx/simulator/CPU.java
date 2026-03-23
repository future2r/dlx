package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

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
/// ### Control hazards (branch / jump)
/// Branch conditions and jump targets are resolved at the end of EX. When a
/// redirect is required:
///
/// - The PC is set to the target address.
/// - The IF/ID and ID/EX latches are flushed with bubbles (2-cycle penalty).
///
/// ## HALT
/// The simulator-specific `HALT` instruction (opcode `0x3F`) sets
/// [#isHalted()] to `true` once it reaches the WB stage. At that point the
/// entire pipeline has drained past the instruction, ensuring that all preceding
/// stores and register writes have committed.
public final class CPU {

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

    /// The program counter - the byte address of the **next** instruction to be
    /// fetched by the IF stage. Updated at the end of every `step()`.
    private int pc = 0;

    /// Set to `true` once a HALT instruction retires through WB. [#step()] becomes
    /// a no-op after this point.
    private boolean halted = false;

    /// Total number of clock cycles executed since the last [#loadProgram].
    private long cycles = 0;

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
        this.memory = new Memory(1024 * 2); // Default memory size: 2 KB
    }

    /// Creates a new CPU with a freshly allocated zero-initialised memory.
    ///
    /// @param memorySizeBytes the total size of the simulated memory in bytes; must
    ///                        be large enough to hold the program and any data
    ///                        it accesses
    public CPU(final int memorySizeBytes) {
        this.memory = new Memory(memorySizeBytes);
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
    /// The encoded data and instructions are written to memory starting at address
    /// 0, the PC is set to `entryPoint`, the cycle counter is reset, and all four
    /// pipeline latches are flushed to their bubble values. The memory outside the
    /// program area retains its previous contents (or zero on first load).
    ///
    /// @param program    array of bytes representing the program; must not be `null`
    ///                   `program[0]` is placed at address 0
    /// @param entryPoint the initial PC value
    public void loadProgram(final byte[] program, final int entryPoint) {
        requireNonNull(program, "program must not be null");
        // Write the program bytes into memory.
        this.memory.loadProgram(program, 0);

        // Reset all mutable CPU state.
        this.pc = entryPoint;
        this.halted = false;
        this.cycles = 0;

        // Flush the pipeline - no instructions are in flight.
        this.ifId = IfIdLatch.BUBBLE;
        this.idEx = IdExLatch.BUBBLE;
        this.exMem = ExMemLatch.BUBBLE;
        this.memWb = MemWbLatch.BUBBLE;
    }

    // -------------------------------------------------------------------------
    // Execution
    // -------------------------------------------------------------------------

    /// Advances the simulation by exactly one clock cycle.
    ///
    /// If the CPU is already halted this method returns immediately without
    /// modifying any state.
    ///
    /// The five stages are evaluated in reverse pipeline order (WB first, IF last)
    /// so that each stage's output is available as a forwarding source for earlier
    /// stages within the same cycle.
    public void step() {
        // A halted CPU does nothing.
        if (this.halted)
            return;

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
        // newMemWb is passed as the MEM/WB forwarding source so that
        // load-to-ALU forwarding across two cycles works correctly.
        // -----------------------------------------------------------------
        final var exResult = ExecuteStage.execute(this.idEx, this.exMem, newMemWb, this.alu);
        final var newExMem = exResult.exMem();

        // -----------------------------------------------------------------
        // ID stage: decode and read registers - suppressed during a stall.
        // A stall injects a bubble instead of advancing the next instruction.
        // -----------------------------------------------------------------
        // newIdEx cannot use 'var' here because it is assigned in a branch.
        // It cannot be 'final' because it may be reassigned in the IF block.
        IdExLatch newIdEx;
        if (stall) {
            // Hold the current instruction in ID; inject a NOP into EX.
            newIdEx = IdExLatch.BUBBLE;
        } else {
            newIdEx = InstructionDecodeStage.execute(this.ifId, this.registers);
        }

        // -----------------------------------------------------------------
        // IF stage: fetch the next instruction - or apply flush / freeze.
        // -----------------------------------------------------------------
        // newIfId and newPc are each assigned exactly once across all branches
        // of the if-else-if-else below, so they can be final.
        final IfIdLatch newIfId;
        final int newPc;

        if (exResult.pcRedirect()) {
            // A branch was taken or a jump executed: discard the two
            // incorrectly fetched instructions and redirect the PC.
            newIfId = IfIdLatch.BUBBLE;
            newIdEx = IdExLatch.BUBBLE; // also flush the instruction in ID
            newPc = exResult.newPc();
        } else if (stall) {
            // Load-use stall: freeze the PC and the IF/ID latch so that the
            // same instruction is presented to ID again next cycle.
            newIfId = this.ifId;
            newPc = this.pc;
        } else {
            // Normal operation: fetch the next word and advance the PC.
            newIfId = InstructionFetchStage.execute(this.pc, this.memory);
            newPc = this.pc + 4;
        }

        // -----------------------------------------------------------------
        // Commit: write all new latch values atomically (clock edge).
        // -----------------------------------------------------------------
        this.ifId = newIfId;
        this.idEx = newIdEx;
        this.exMem = newExMem;
        this.memWb = newMemWb;
        this.pc = newPc;
        this.cycles++;

        // -----------------------------------------------------------------
        // Halt check: the HALT instruction has now retired through WB.
        // -----------------------------------------------------------------
        if (newMemWb.ctrl().halt()) {
            this.halted = true;
        }
    }

    /// Runs the simulation until a HALT instruction retires or `maxCycles` clock
    /// cycles have been executed.
    ///
    /// Use [Long#MAX_VALUE] for `maxCycles` to run without a cycle limit, but be
    /// aware that a program without a HALT instruction will then loop forever.
    ///
    /// @param maxCycles the maximum number of cycles to execute before throwing;
    ///                  must be positive
    /// @throws IllegalStateException if the cycle limit is reached before a HALT
    ///                               instruction retires
    public void run(final long maxCycles) {
        while (!this.halted && this.cycles < maxCycles) {
            step();
        }
        if (!this.halted) {
            throw new IllegalStateException(
                    "Simulation did not halt within " + maxCycles + " cycles");
        }
    }

    /// Runs the simulation until a HALT instruction retires, with no cycle limit.
    ///
    /// Equivalent to `run(Long.MAX_VALUE)`. A program that never executes a HALT
    /// instruction will cause this method to loop indefinitely.
    public void run() {
        run(Long.MAX_VALUE);
    }

    // -------------------------------------------------------------------------
    // Observability
    // -------------------------------------------------------------------------

    /// Returns `true` once a HALT instruction has retired through the WB stage.
    /// [#step()] is a no-op when `isHalted()` returns `true`.
    ///
    /// @return `true` if the simulation has completed
    public boolean isHalted() {
        return this.halted;
    }

    /// Returns the current program counter - the address of the instruction that
    /// will be fetched on the **next** call to [#step()].
    ///
    /// After a HALT retires this value points to the instruction immediately
    /// following the HALT.
    ///
    /// @return the PC as a byte address
    public int getPc() {
        return this.pc;
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
            requireNonNull(ifId, "ifId must not be null");
            requireNonNull(idEx, "idEx must not be null");
            requireNonNull(exMem, "exMem must not be null");
            requireNonNull(memWb, "memWb must not be null");
        }
    }
}
