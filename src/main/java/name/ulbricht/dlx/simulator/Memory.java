package name.ulbricht.dlx.simulator;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import name.ulbricht.dlx.simulator.MemoryChangeListener.MemoryChange;

/// Flat, byte-addressable memory using **big-endian** byte order.
///
/// A single contiguous `byte[]` array backs all memory operations. The address
/// space starts at byte 0 and extends to `size - 1`. Instruction and data memory
/// share the same flat space; by convention programs are loaded at address 0.
///
/// ## Byte order
/// Multi-byte values are stored and loaded in big-endian order: the
/// most-significant byte is at the lowest address.
///
/// ## Bounds checking
/// Every access is range-checked. An [IllegalArgumentException] is thrown when
/// the requested range `[addr, addr + width)` falls outside `[0, size)`.
final class Memory {

    /// The backing byte array.
    private final byte[] data;

    /// Listeners for memory changes.
    private final List<MemoryChangeListener> changeListeners = new ArrayList<>();

    /// A cached read-only view of this memory, created on demand.
    private ReadOnlyMemory readOnlyView;

    /// Allocates a zero-initialised memory of `sizeBytes` bytes.
    ///
    /// @param sizeBytes the total number of addressable bytes; must be positive
    Memory(final int sizeBytes) {
        this.data = new byte[sizeBytes];
    }

    /// {@returns the total size of this memory in bytes}
    int size() {
        return this.data.length;
    }

    /// Loads a 32-bit word from `addr` (big-endian).
    ///
    /// @param addr the byte address of the most-significant byte; must be a valid
    ///             address with at least 4 bytes remaining
    /// @return the 32-bit value assembled from bytes `addr`…`addr+3`
    int loadWord(final int addr) {
        checkBounds(addr, 4);

        // Assemble four big-endian bytes into one 32-bit integer.
        // The `& 0xFF` masks prevent sign-extension of individual bytes.
        return (this.data[addr] << 24)
                | ((this.data[addr + 1] & 0xFF) << 16)
                | ((this.data[addr + 2] & 0xFF) << 8)
                | (this.data[addr + 3] & 0xFF);
    }

    /// Loads a **sign-extended** 16-bit half-word from `addr` (big-endian).
    ///
    /// The two bytes are assembled and cast to `short` so that Java's implicit
    /// widening sign-extends the value to 32 bits.
    ///
    /// @param addr the byte address of the most-significant byte; must be a valid
    ///             address with at least 2 bytes remaining
    /// @return the 32-bit sign-extended half-word value
    int loadHalfWord(final int addr) {
        checkBounds(addr, 2);

        // Cast to short first; the subsequent int promotion sign-extends.
        return (short) (((this.data[addr] & 0xFF) << 8)
                | (this.data[addr + 1] & 0xFF));
    }

    /// Loads a **zero-extended** 16-bit half-word from `addr` (big-endian).
    ///
    /// The result is always in the range `[0, 65535]`.
    ///
    /// @param addr the byte address; must have at least 2 bytes remaining
    /// @return the 32-bit zero-extended half-word value
    int loadHalfWordU(final int addr) {
        checkBounds(addr, 2);

        return ((this.data[addr] & 0xFF) << 8) | (this.data[addr + 1] & 0xFF);
    }

    /// Loads a **sign-extended** byte from `addr`.
    ///
    /// Java's `byte` type is signed, so the implicit `byte`→`int` promotion already
    /// sign-extends the value.
    ///
    /// @param addr the byte address
    /// @return the 32-bit sign-extended byte value (range `[-128, 127]`)
    int loadByte(final int addr) {
        checkBounds(addr, 1);

        return this.data[addr]; // implicit sign-extension by Java widening
    }

    /// Loads a **zero-extended** byte from `addr`.
    ///
    /// The result is always in the range `[0, 255]`.
    ///
    /// @param addr the byte address
    /// @return the 32-bit zero-extended byte value (range `[0, 255]`)
    int loadByteU(final int addr) {
        checkBounds(addr, 1);

        return this.data[addr] & 0xFF;
    }

    /// Stores a 32-bit word at `addr` in big-endian order.
    ///
    /// @param addr  the byte address of the most-significant byte
    /// @param value the 32-bit value to store
    void storeWord(final int addr, final int value) {
        checkBounds(addr, 4);

        // Write bytes from most-significant to least-significant.
        this.data[addr] = (byte) (value >>> 24);
        this.data[addr + 1] = (byte) (value >>> 16);
        this.data[addr + 2] = (byte) (value >>> 8);
        this.data[addr + 3] = (byte) value;

        notifyChangeListeners(addr, Arrays.copyOfRange(this.data, addr, addr + 4));
    }

    /// Stores the lower 16 bits of `value` at `addr` in big-endian order.
    ///
    /// @param addr  the byte address of the most-significant byte
    /// @param value the value whose lower 16 bits are stored
    void storeHalfWord(final int addr, final int value) {
        checkBounds(addr, 2);

        this.data[addr] = (byte) (value >>> 8);
        this.data[addr + 1] = (byte) value;

        notifyChangeListeners(addr, Arrays.copyOfRange(this.data, addr, addr + 2));
    }

    /// Stores the lower 8 bits of `value` at `addr`.
    ///
    /// @param addr  the byte address
    /// @param value the value whose lower 8 bits are stored
    void storeByte(final int addr, final int value) {
        checkBounds(addr, 1);

        this.data[addr] = (byte) value;

        notifyChangeListeners(addr, Arrays.copyOfRange(this.data, addr, addr + 1));
    }

    /// Writes an array of 32-bit instruction words into memory starting at
    /// `startAddr`, using big-endian byte order.
    ///
    /// Each element of `words` occupies exactly 4 bytes.
    ///
    /// @param words     the encoded instruction words to load; must not be `null`
    /// @param startAddr the byte address at which to begin writing; typically 0
    void loadProgram(final int[] words, final int startAddr) {
        requireNonNull(words, "words must not be null");

        for (var i = 0; i < words.length; i++) {
            storeWord(startAddr + i * 4, words[i]);
        }
    }

    /// Verifies that the range `[addr, addr + width)` lies entirely within `[0,
    /// data.length)`.
    ///
    /// @param addr  the starting byte address
    /// @param width the number of bytes to access
    /// @throws IllegalArgumentException if the range is invalid or out of bounds
    private void checkBounds(final int addr, final int width) {
        if (addr < 0 || addr + width > this.data.length) {
            throw new IllegalArgumentException(
                    "Memory access out of bounds: addr=0x" + Integer.toHexString(addr)
                            + " width=" + width + " memSize=" + this.data.length);
        }
    }

    void addMemoryChangeListener(final MemoryChangeListener listener) {
        this.changeListeners.add(listener);
    }

    void removeMemoryChangeListener(final MemoryChangeListener listener) {
        this.changeListeners.remove(listener);
    }

    private void notifyChangeListeners(final int address, final byte[] changed) {
        if (this.changeListeners.isEmpty())
            return;

        final var change = new MemoryChange(address, changed);
        List.copyOf(this.changeListeners).forEach(listener -> listener.changed(change));
    }

    /// {@returns a read-only view of this memory}
    ReadOnlyMemory asReadOnly() {
        if (this.readOnlyView == null)
            this.readOnlyView = ReadOnlyMemory.of(this);
        return this.readOnlyView;
    }
}
