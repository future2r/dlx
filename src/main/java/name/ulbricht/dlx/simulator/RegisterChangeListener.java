package name.ulbricht.dlx.simulator;

/// Listener for changes in registers.
@FunctionalInterface
public interface RegisterChangeListener {

    /// Event describing a change in a register.
    /// 
    /// @param registerIndex the index of the changed register
    /// @param newValue      the new value of the changed register
    public record RegisterChange(int registerIndex, int newValue) {
    }

    /// Called when a change in a register occurs.
    ///
    /// @param change the event describing the change in the register
    void changed(RegisterChange change);
}
