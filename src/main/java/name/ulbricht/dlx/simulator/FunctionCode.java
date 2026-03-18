package name.ulbricht.dlx.simulator;

/// R-format function codes, encoded in the lower 6 bits (bits 5–0) of an
/// R-format instruction word.
///
/// When the opcode field of an instruction word is [OperationCode#SPECIAL] (0x00)
/// the instruction is R-format. The remaining 26 bits are split as:
///
/// ```
/// rs1(5) | rs2(5) | rd(5) | unused(5) | func(6)
/// ```
///
/// The `func` field selects the register-to-register operation. The
/// [#fromCode(int)] factory performs an O(1) lookup so the decoder does
/// not need a linear scan.
///
/// ## NOP encoding
/// `func = 0x00` together with all register fields set to zero produces the
/// canonical NOP:  `SLL R0, R0, 0`. Because R0 is hardwired to zero, the
/// shift has no observable effect. [#NOP] is therefore defined as a
/// dedicated constant for clarity, even though it shares its numeric value
/// with `SLL`.
public enum FunctionCode {

    /// No-operation (0x00).
    ///
    /// Encoded as `SLL R0, R0, 0` - a shift of zero into the immutable
    /// zero register. The control unit maps this to a bubble
    /// ([ControlSignals#NOP][name.ulbricht.dlx.simulator.ControlSignals#NOP])
    /// with `regWrite = false` so no register is written.
    NOP(0x00),

    // -------------------------------------------------------------------------
    // Shifts
    // -------------------------------------------------------------------------

    /// Shift left logical (0x04): `rd = rs1 << (rs2 & 0x1F)`.
    SLL(0x04),

    /// Shift right logical, zero-fill (0x06): `rd = rs1 >>> (rs2 & 0x1F)`.
    SRL(0x06),

    /// Shift right arithmetic, sign-fill (0x07): `rd = rs1 >> (rs2 & 0x1F)`.
    SRA(0x07),

    // -------------------------------------------------------------------------
    // Arithmetic
    // -------------------------------------------------------------------------

    /// Signed addition (0x20): `rd = rs1 + rs2`. Overflow is detected.
    ADD(0x20),

    /// Unsigned addition (0x21): `rd = rs1 + rs2`. No overflow detection.
    ADDU(0x21),

    /// Signed subtraction (0x22): `rd = rs1 - rs2`. Overflow is detected.
    SUB(0x22),

    /// Unsigned subtraction (0x23): `rd = rs1 - rs2`. No overflow detection.
    SUBU(0x23),

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    /// Bitwise AND (0x24): `rd = rs1 & rs2`.
    AND(0x24),

    /// Bitwise OR (0x25): `rd = rs1 | rs2`.
    OR(0x25),

    /// Bitwise XOR (0x26): `rd = rs1 ^ rs2`.
    XOR(0x26),

    // -------------------------------------------------------------------------
    // Set-if comparisons
    // -------------------------------------------------------------------------

    /// Set if equal (0x28): `rd = (rs1 == rs2) ? 1 : 0`.
    SEQ(0x28),

    /// Set if not equal (0x29): `rd = (rs1 != rs2) ? 1 : 0`.
    SNE(0x29),

    /// Set if less than, signed (0x2A): `rd = (rs1 < rs2) ? 1 : 0`.
    SLT(0x2A),

    /// Set if greater than, signed (0x2B): `rd = (rs1 > rs2) ? 1 : 0`.
    SGT(0x2B),

    /// Set if less than or equal, signed (0x2C): `rd = (rs1 <= rs2) ? 1 : 0`.
    SLE(0x2C),

    /// Set if greater than or equal, signed (0x2D): `rd = (rs1 >= rs2) ? 1 : 0`.
    SGE(0x2D);

    // -------------------------------------------------------------------------
    // Fields and factory
    // -------------------------------------------------------------------------

    /// The 6-bit numeric function code as it appears in bits 5–0 of the
    /// instruction word.
    public final int code;

    /// Constructs a `FunctionCode` constant with the given numeric code.
    ///
    /// @param code the 6-bit function code value
    FunctionCode(final int code) {
        this.code = code;
    }

    /// Fast O(1) lookup table, indexed directly by the 6-bit func value.
    /// Entries for unassigned codes remain `null`.
    private static final FunctionCode[] BY_CODE = new FunctionCode[64];

    static {
        // Populate the lookup table once at class-load time.
        for (final var f : values()) {
            BY_CODE[f.code] = f;
        }
    }

    /// Returns the `FunctionCode` whose [#code] equals the lower 6 bits of the
    /// argument.
    ///
    /// @param code a raw value; only the lower 6 bits are examined
    /// @return the matching `FunctionCode` constant
    /// @throws IllegalArgumentException if no function code is assigned to
    ///                                  that value
    public static FunctionCode fromCode(final int code) {
        final var f = BY_CODE[code & 0x3F];
        if (f == null) {
            throw new IllegalArgumentException(
                    "Unknown func code: 0x" + Integer.toHexString(code & 0x3F));
        }
        return f;
    }
}
