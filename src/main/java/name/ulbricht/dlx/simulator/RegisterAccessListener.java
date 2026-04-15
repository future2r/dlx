package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

/// Listener for access to registers.
@FunctionalInterface
public interface RegisterAccessListener {

    /// Event describing an access to a register. For a [Access#READ] access the
    /// value contains the current value of the register, for a [Access#WRITE] access
    /// the value contains the new value of the register.
    /// 
    /// @param type  the type of access (read or write)
    /// @param index the index of the accessed register
    /// @param value the value involved in the access
    record RegisterAccess(Access type, int index, int value) {

        /// Create a new register access event.
        public RegisterAccess {
            requireNonNull(type, "type must not be null");
        }
    }

    /// Called when an access to a register occurs.
    ///
    /// @param access the event describing the access to the register
    void registerAccessed(RegisterAccess access);
}
