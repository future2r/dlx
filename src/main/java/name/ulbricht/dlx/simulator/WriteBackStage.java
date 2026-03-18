package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// **WB** (Write Back) stage.
///
/// This is the fifth and final pipeline stage. Its sole responsibility is to
/// write the instruction's result into the destination register, if the
/// instruction produces one.
///
/// ## When WB writes a register
/// WB writes the register file only when `ctrl.regWrite()` is `true`. The value
/// to write is selected by `ctrl.memToReg()`:
///
/// | `memToReg` | Value written | Instructions |
/// |------------|---------------|--------------|
/// | `false`    | `aluResult`   | arithmetic, logic, shift, set-if, JAL/JALR |
/// | `true`     | `memData`     | load instructions (LB, LH, LW, LBU, LHU) |
///
/// ## When WB does nothing
/// Instructions with `ctrl.regWrite() == false` pass through silently: stores,
/// branches, unconditional jumps without link, NOP bubbles, and HALT.
///
/// ## Ordering inside a clock cycle
/// The [CPU][name.ulbricht.dlx.simulator.core.CPU] calls `execute` as the very
/// first stage operation each cycle (before IF, ID, EX, MEM are computed). This
/// means WB reads the MEM/WB latch snapshot from the **previous** cycle and its
/// write is visible to the register file only for future cycles - consistent
/// with the edge-triggered flip-flop model used throughout the simulator.
final class WriteBackStage {

    /// Private constructor - this class is not instantiable.
    private WriteBackStage() {
    }

    /// Writes the instruction result to the register file, if applicable.
    ///
    /// @param memWb the MEM/WB latch produced by the MEM stage in the previous cycle
    /// @param regs  the register file to write into; a write to R0 is silently
    ///              ignored by [Registers#write]
    static void execute(final MemWbLatch memWb, final Registers regs) {
        requireNonNull(memWb, "memWb must not be null");
        requireNonNull(regs, "regs must not be null");
        // Skip silently if this instruction does not write a register.
        if (!memWb.ctrl().regWrite())
            return;

        // Choose between the memory-load value and the ALU result.
        final var value = memWb.ctrl().memToReg() ? memWb.memData() : memWb.aluResult();

        // Write to the destination register (R0 writes are suppressed by
        // Registers itself).
        regs.write(memWb.rd(), value);
    }
}
