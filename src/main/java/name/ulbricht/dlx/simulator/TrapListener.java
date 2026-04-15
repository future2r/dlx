package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener for non-halt trap instructions that retire through the WB stage.
///
/// Implementations receive a [TrapEvent] with the trap number and read-only
/// access to the register file and memory. The callback is invoked synchronously
/// on the simulation thread.
@FunctionalInterface
public interface TrapListener {

    /// Event fired when a non-halt trap retires.
    ///
    /// @param trapNumber the immediate value of the trap instruction
    /// @param registers  read-only view of the register file
    /// @param memory     read-only view of the data memory
    record TrapEvent(int trapNumber, ReadOnlyRegisters registers, ReadOnlyMemory memory) {

        /// Creates a new trap event.
        public TrapEvent {
            requireNonNull(registers);
            requireNonNull(memory);
        }
    }

    /// Called when a non-halt trap instruction retires through WB.
    ///
    /// @param event the trap event
    void trapRetired(TrapEvent event);
}
