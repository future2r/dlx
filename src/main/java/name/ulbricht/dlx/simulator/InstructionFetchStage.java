package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// **IF** (Instruction Fetch) stage.
///
/// This is the first of the five pipeline stages. Its sole responsibility is to
/// read one 32-bit instruction word from memory at the current program counter
/// address and package it into the IF/ID latch for the ID stage to consume next
/// cycle.
///
/// The caller ([CPU][name.ulbricht.dlx.simulator.CPU]) is responsible for
/// incrementing the PC by 4 after calling [#execute]. The PC value passed in is
/// stored in the latch so that later stages can use it for branch-target and
/// link-address calculations.
///
/// ## When IF is suppressed
/// The CPU does **not** call this method when:
///
/// - A **branch or jump** was taken in EX: the CPU writes
///   [IfIdLatch#BUBBLE] directly into the latch to flush the incorrectly fetched
///   instruction.
/// - A **load-use stall** is active: the CPU holds the existing IF/ID latch
///   value unchanged (the same instruction is re-presented to ID next cycle).
final class InstructionFetchStage {

    /// Private constructor - this class is not instantiable.
    private InstructionFetchStage() {
    }

    /// Fetches the instruction at `pc` and returns the new IF/ID latch value.
    ///
    /// The method reads exactly four bytes (one word) from `memory` starting at `pc`
    /// using big-endian byte order. No bounds checking beyond what
    /// [Memory#loadWord][name.ulbricht.dlx.simulator.Memory#loadWord(int)] performs.
    ///
    /// @param pc     the byte address of the instruction to fetch; must be
    ///               word-aligned (multiple of 4)
    /// @param memory the simulated memory to read from
    /// @return a new [IfIdLatch] containing `pc` and the fetched instruction word
    /// @throws IllegalArgumentException if `pc` is outside the memory bounds
    static IfIdLatch execute(final int pc, final Memory memory) {
        requireNonNull(memory, "memory must not be null");
        // Read the 32-bit instruction word at the current PC address.
        final var word = memory.loadWord(pc);
        // Bundle the PC and the word into the latch for the ID stage.
        return new IfIdLatch(pc, word);
    }
}
