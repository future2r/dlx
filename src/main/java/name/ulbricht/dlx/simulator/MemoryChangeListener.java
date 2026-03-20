package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener for changes in memory.
@FunctionalInterface
public interface MemoryChangeListener {

    /// Event describing a change in memory.
    /// 
    /// @param address the starting address of the change
    /// @param changed the array of changed bytes
    public record MemoryChange(int address, byte[] changed) {

        /// Constructs a new memory change.
        public MemoryChange {
            if (address < 0)
                throw new IllegalArgumentException("Address must be non-negative: " + address);
            requireNonNull(changed, "Changed array must not be null or empty");
        }
    }

    /// Called when a change in memory occurs.
    /// 
    /// @param change the event describing the change in memory
    void changed(MemoryChange change);
}
