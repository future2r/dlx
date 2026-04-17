package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener notified at the start and end of every simulation cycle.
///
/// `cycleStart()` fires **before** any pipeline stage executes, so it is the
/// correct place to clear per-cycle highlights or other transient display state.
/// `cycleEnd()` fires **after** all latches have been committed and carries the
/// post-cycle snapshot — use it to update pipeline, cycle-counter, and PC
/// displays.
///
/// Only `cycleEnd()` is abstract; `cycleStart()` provides a no-op default so
/// listeners that do not need it do not have to override it.
@FunctionalInterface
public interface CycleListener {

    /// The state of the cycle
    enum CycleState{

        /// The cycle is starting, before any pipeline stage executes.
        START,

        /// The cycle is ending, after all latches have been committed.
        END
    }

    /// Immutable snapshot of one completed cycle delivered to [#onCycle].
    ///
    /// @param state          the state of the cycle
    /// @param cycles         the number of cycles at the end of this cycle
    /// @param programCounter the program counter after this cycle
    /// @param halted         whether the CPU halted as a result of this cycle
    /// @param pipeline       immutable snapshot of all four pipeline latches
    record Cycle(CycleState state, long cycles, int programCounter, boolean halted,
            CPU.PipelineSnapshot pipeline) {

        /// Creates a new record and validates the components.
        public Cycle {
            requireNonNull(state);
            requireNonNull(pipeline, "pipeline must not be null");
        }
    }


    /// Called when a cycle is processed.
    ///
    /// @param cycle the snapshot of the completed cycle
    void onCycle( Cycle cycle);
}
