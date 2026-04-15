package name.ulbricht.dlx.ui.view.reference;

/// Describes the conventional purpose of each DLX register or register group.
///
/// The DLX architecture defines 32 general-purpose registers (R0–R31). While
/// any register can technically hold any value, software conventions assign
/// specific roles to certain registers.
public enum RegisterConvention {

    /// R0: hardwired to zero.
    R0(0, 0),

    /// R1: reserved for the assembler.
    R1(1, 1),

    /// R2–R3: function return values.
    R2_R3(2, 3),

    /// R4–R7: function parameters.
    R4_R7(4, 7),

    /// R8–R15: general-purpose temporaries.
    R8_R15(8, 15),

    /// R16–R23: callee-saved register variables.
    R16_R23(16, 23),

    /// R24–R25: general-purpose temporaries.
    R24_R25(24, 25),

    /// R26–R27: reserved for the operating system.
    R26_R27(26, 27),

    /// R28: global pointer.
    R28(28, 28),

    /// R29: stack pointer.
    R29(29, 29),

    /// R30: callee-saved register variable.
    R30(30, 30),

    /// R31: return address (set by JAL/JALR).
    R31(31, 31);

    private final int startIndex;

    private final int endIndex;

    RegisterConvention(final int startIndex, final int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /// {@return the first register index in the group (inclusive)}
    public int startIndex() {
        return this.startIndex;
    }

    /// {@return the last register index in the group (inclusive)}
    public int endIndex() {
        return this.endIndex;
    }

    /// {@return a display name like `"R0"` or `"R2\u2013R3"`}
    public String displayName() {
        if (this.startIndex() == this.endIndex()) {
            return "R" + this.startIndex();
        }
        return "R" + this.startIndex() + "\u2013R" + this.endIndex();
    }
}
