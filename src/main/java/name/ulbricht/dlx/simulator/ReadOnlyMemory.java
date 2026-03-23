package name.ulbricht.dlx.simulator;

/// A read-only view of the memory.
public interface ReadOnlyMemory {

    /// Creates a read-only wrapper around the given memory.
    ///
    /// @param memory the memory to wrap
    /// @return a read-only view of `memory`
    static ReadOnlyMemory of(final Memory memory) {
        return new ReadOnlyMemory() {

            @Override
            public int size() {
                return memory.size();
            }

            @Override
            public int loadWord(final int addr) {
                return memory.loadWord(addr);
            }

            @Override
            public int loadHalfWord(final int addr) {
                return memory.loadHalfWord(addr);
            }

            @Override
            public void addAccessListener(final MemoryAccessListener listener) {
                memory.addAccessListener(listener);
            }

            @Override
            public void removeAccessListener(final MemoryAccessListener listener) {
                memory.removeAccessListener(listener);
            }
        };
    }

    /// {@return the total size of this memory in bytes}
    int size();

    /// {@return a 32-bit word from `addr`}
    ///
    /// @param addr the byte address of the most-significant byte; must be a valid
    ///             address with at least 4 bytes remaining
    int loadWord(final int addr);

    /// {@return a 32-bit sign-extended half-word from `addr`}
    ///
    /// The two bytes are assembled and cast to `short` so that Java's implicit
    /// widening sign-extends the value to 32 bits.
    ///
    /// @param addr the byte address of the most-significant byte; must be a valid
    ///             address with at least 2 bytes remaining
    int loadHalfWord(final int addr);

    /// Adds a listener for memory access.
    ///
    /// @param listener the listener to add
    void addAccessListener(MemoryAccessListener listener);

    /// Removes a listener for memory access.
    ///
    /// @param listener the listener to remove
    void removeAccessListener(MemoryAccessListener listener);
}
