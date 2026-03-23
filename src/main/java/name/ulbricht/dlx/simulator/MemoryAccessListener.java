package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener for access to memory.
@FunctionalInterface
public interface MemoryAccessListener {

    /// Event describing an access to memory.
    /// 
    /// @param type    the type of access (read or write)
    /// @param address the starting address of the access
    /// @param value   the array of accessed bytes
    public record MemoryAccess(Access type, int address, byte[] value) {

        /// Constructs a new memory access.
        public MemoryAccess {
            requireNonNull(type, "type must not be null");
            requireNonNull(value, "value array must not be null or empty");
        }
    }

    /// Called when an access to memory occurs.
    /// 
    /// @param access the event describing the access to memory
    void memoryAccessed(MemoryAccess access);
}
