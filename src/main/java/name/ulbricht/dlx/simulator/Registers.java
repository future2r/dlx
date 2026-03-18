package name.ulbricht.dlx.simulator;

/// A bank of 32 general-purpose 32-bit registers.
///
/// ## Register conventions
/// The DLX architecture defines the following register roles:
///
/// | Register(s) | Role |
/// |-------------|------|
/// | R0          | Hardwired zero - always reads as 0; writes are ignored |
/// | R1          | Reserved for the assembler |
/// | R2–R3       | Function return values |
/// | R4–R7       | Function parameters |
/// | R8–R15      | General purpose |
/// | R16–R23     | Register variables |
/// | R24–R25     | General purpose |
/// | R26–R27     | Reserved for the operating system |
/// | R28         | Global pointer |
/// | R29         | Stack pointer |
/// | R30         | Register variable |
/// | R31         | Return address (link register) |
///
/// Only the R0 semantics are enforced here; all other conventions are the
/// responsibility of the programmer or assembler.
final class Registers {

    /// The backing storage for the 32 register values.
    private final int[] regs = new int[32];

    private ReadOnlyRegisters readOnlyView;

    /// {@return the current 32-bit value of the register at `index`}
    ///
    /// The index is masked to 5 bits (`& 0x1F`) so callers need not guard against
    /// out-of-range values. Register 0 always returns `0`.
    ///
    /// @param index the register number (0–31; higher bits are masked away)
    public int read(final int index) {
        return this.regs[index & 0x1F];
    }

    /// Writes `value` to the register at `index`.
    ///
    /// A write to register 0 is silently discarded to preserve the hardwired-zero
    /// semantics. The index is masked to 5 bits before the write.
    ///
    /// @param index the register number (0–31; higher bits are masked away)
    /// @param value the 32-bit value to store
    public void write(final int index, final int value) {
        final var idx = index & 0x1F;
        if (idx != 0)
            this.regs[idx] = value;
    }

    /// {@returns a copy of all 32 register values}
    ///
    /// Modifying the array does not affect the registers. Use this method when the
    /// contents need to be displayed or captured at a particular point in time.
    public int[] snapshot() {
        return this.regs.clone();
    }

    /// {@returns a read-only view of this registers}
    public ReadOnlyRegisters asReadOnly() {
        if (this.readOnlyView == null)
            this.readOnlyView = ReadOnlyRegisters.of(this);
        return this.readOnlyView;
    }
}
