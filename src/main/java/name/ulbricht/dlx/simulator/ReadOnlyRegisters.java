package name.ulbricht.dlx.simulator;

/// A read-only view of the registers.
public interface ReadOnlyRegisters {

    /// Creates a read-only wrapper around the given registers.
    ///
    /// @param registers the registers to wrap
    /// @return a read-only view of `registers`
    static ReadOnlyRegisters of(final Registers registers) {
        return new ReadOnlyRegisters() {

            @Override
            public int read(final int index) {
                return registers.read(index);
            }

            @Override
            public int[] snapshot() {
                return registers.snapshot();
            }

            @Override
            public void addAccessListener(final RegisterAccessListener listener) {
                registers.addAccessListener(listener);
            }

            @Override
            public void removeAccessListener(final RegisterAccessListener listener) {
                registers.removeAccessListener(listener);
            }
        };
    }

    /// {@return the current 32-bit value of the register at `index`}
    /// 
    /// @param index the register number (0–31; higher bits are masked away)
    int read(final int index);

    /// {@return a copy of all 32 register values}
    int[] snapshot();

    /// Adds a listener for register access events.
    ///
    /// @param listener the listener to add
    void addAccessListener(RegisterAccessListener listener);

    /// Removes a listener for register access events.
    ///
    /// @param listener the listener to remove
    void removeAccessListener(RegisterAccessListener listener);

}
