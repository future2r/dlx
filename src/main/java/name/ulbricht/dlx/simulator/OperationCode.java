package name.ulbricht.dlx.simulator;

/// All DLX opcodes encoded in the upper 6 bits (bits 31–26) of every
/// 32-bit instruction word.
///
/// The DLX ISA uses three instruction formats that share this 6-bit opcode
/// field:
///
/// - **R-format** - opcode is always [#SPECIAL] (0x00); the actual
///   operation is encoded separately in the 6-bit `func` field
///   (see [FunctionCode]).
/// - **I-format** - all remaining opcodes that combine one or two register
///   operands with a 16-bit immediate value.
/// - **J-format** - [#J] and [#JAL], which encode a signed 26-bit branch
///   distance.
///
/// The [#fromCode(int)] factory method performs an O(1) array lookup so the
/// instruction decoder does not need a linear scan through all constants.
public enum OperationCode {

    // -------------------------------------------------------------------------
    // R-format
    // -------------------------------------------------------------------------

    /// R-format marker opcode (0x00).
    ///
    /// Whenever the upper 6 bits of an instruction word are zero the
    /// instruction is R-format. The actual register-to-register operation
    /// is encoded in the lower 6-bit `func` field - see [FunctionCode].
    SPECIAL(0x00),

    // -------------------------------------------------------------------------
    // J-format
    // -------------------------------------------------------------------------

    /// Unconditional jump (J-format, 0x02).
    ///
    /// The sign-extended 26-bit distance is added to the address of this
    /// instruction to obtain the jump target. The processor does **not**
    /// save a return address.
    J(0x02),

    /// Jump and link (J-format, 0x03).
    ///
    /// Behaves like [#J] but additionally saves `PC + 4` (the address of
    /// the instruction following this one) in R31 as the return address.
    /// Used to implement subroutine calls.
    JAL(0x03),

    // -------------------------------------------------------------------------
    // I-format - conditional branches
    // -------------------------------------------------------------------------

    /// Branch if equal to zero (I-format, 0x04).
    ///
    /// If `rs1 == 0` the branch is taken; the target address is
    /// `PC + sign_extend(imm16)`. No register is written.
    BEQZ(0x04),

    /// Branch if not equal to zero (I-format, 0x05).
    ///
    /// If `rs1 != 0` the branch is taken; the target address is
    /// `PC + sign_extend(imm16)`. No register is written.
    BNEZ(0x05),

    // -------------------------------------------------------------------------
    // I-format - arithmetic immediate
    // -------------------------------------------------------------------------

    /// Add immediate, signed (I-format, 0x08).
    ///
    /// `rd = rs1 + sign_extend(imm16)`. Overflow detection is active.
    ADDI(0x08),

    /// Add immediate, unsigned (I-format, 0x09).
    ///
    /// `rd = rs1 + sign_extend(imm16)`. No overflow detection.
    ADDUI(0x09),

    /// Subtract immediate, signed (I-format, 0x0A).
    ///
    /// `rd = rs1 - sign_extend(imm16)`. Overflow detection is active.
    SUBI(0x0A),

    /// Subtract immediate, unsigned (I-format, 0x0B).
    ///
    /// `rd = rs1 - sign_extend(imm16)`. No overflow detection.
    SUBUI(0x0B),

    // -------------------------------------------------------------------------
    // I-format - logic immediate
    // -------------------------------------------------------------------------

    /// Bitwise AND immediate (I-format, 0x0C).
    ///
    /// `rd = rs1 & sign_extend(imm16)`.
    ANDI(0x0C),

    /// Bitwise OR immediate (I-format, 0x0D).
    ///
    /// `rd = rs1 | sign_extend(imm16)`.
    ORI(0x0D),

    /// Bitwise XOR immediate (I-format, 0x0E).
    ///
    /// `rd = rs1 ^ sign_extend(imm16)`.
    XORI(0x0E),

    // -------------------------------------------------------------------------
    // I-format - load high immediate
    // -------------------------------------------------------------------------

    /// Load high immediate (I-format, 0x0F).
    ///
    /// `rd = imm16 << 16`. The lower 16 bits of `rd` are set to zero.
    /// Typically paired with [#ORI] to load a full 32-bit constant:
    ///
    /// ```asm
    /// LHI  R1, #0x1234    ; R1 = 0x12340000
    /// ORI  R1, R1, #0x5678 ; R1 = 0x12345678
    /// ```
    LHI(0x0F),

    // -------------------------------------------------------------------------
    // I-format - jump register
    // -------------------------------------------------------------------------

    /// Jump register (I-format, 0x10).
    ///
    /// `PC = rs1`. Used to return from subroutines via `JR R31`.
    /// No register is written.
    JR(0x10),

    /// Jump and link register (I-format, 0x11).
    ///
    /// `PC = rs1`, `R31 = PC + 4`. Used for indirect calls through a
    /// function pointer stored in a register.
    JALR(0x11),

    // -------------------------------------------------------------------------
    // I-format - shift immediate
    // -------------------------------------------------------------------------

    /// Shift left logical immediate (I-format, 0x14).
    ///
    /// `rd = rs1 << (imm16 & 0x1F)`.
    SLLI(0x14),

    /// Shift right logical immediate, zero-fill (I-format, 0x16).
    ///
    /// `rd = rs1 >>> (imm16 & 0x1F)`.
    SRLI(0x16),

    /// Shift right arithmetic immediate, sign-fill (I-format, 0x17).
    ///
    /// `rd = rs1 >> (imm16 & 0x1F)`.
    SRAI(0x17),

    // -------------------------------------------------------------------------
    // I-format - set immediate
    // -------------------------------------------------------------------------

    /// Set if equal immediate (I-format, 0x18).
    ///
    /// `rd = (rs1 == sign_extend(imm16)) ? 1 : 0`.
    SEQI(0x18),

    /// Set if not equal immediate (I-format, 0x19).
    ///
    /// `rd = (rs1 != sign_extend(imm16)) ? 1 : 0`.
    SNEI(0x19),

    /// Set if less than immediate, signed (I-format, 0x1A).
    ///
    /// `rd = (rs1 < sign_extend(imm16)) ? 1 : 0`.
    SLTI(0x1A),

    /// Set if greater than immediate, signed (I-format, 0x1B).
    ///
    /// `rd = (rs1 > sign_extend(imm16)) ? 1 : 0`.
    SGTI(0x1B),

    /// Set if less than or equal immediate, signed (I-format, 0x1C).
    ///
    /// `rd = (rs1 <= sign_extend(imm16)) ? 1 : 0`.
    SLEI(0x1C),

    /// Set if greater than or equal immediate, signed (I-format, 0x1D).
    ///
    /// `rd = (rs1 >= sign_extend(imm16)) ? 1 : 0`.
    SGEI(0x1D),

    // -------------------------------------------------------------------------
    // I-format - loads
    // -------------------------------------------------------------------------

    /// Load byte, sign-extended (I-format, 0x20).
    ///
    /// `rd = sign_extend(mem[rs1 + imm16][7:0])`.
    LB(0x20),

    /// Load half-word, sign-extended (I-format, 0x21).
    ///
    /// `rd = sign_extend(mem[rs1 + imm16][15:0])`.
    LH(0x21),

    /// Load word (I-format, 0x23).
    ///
    /// `rd = mem[rs1 + imm16][31:0]`.
    LW(0x23),

    /// Load byte, zero-extended (I-format, 0x24).
    ///
    /// `rd = zero_extend(mem[rs1 + imm16][7:0])`.
    LBU(0x24),

    /// Load half-word, zero-extended (I-format, 0x25).
    ///
    /// `rd = zero_extend(mem[rs1 + imm16][15:0])`.
    LHU(0x25),

    // -------------------------------------------------------------------------
    // I-format - stores
    // -------------------------------------------------------------------------

    /// Store byte (I-format, 0x28).
    ///
    /// `mem[rs1 + imm16][7:0] = rd[7:0]`.
    /// Note: in the I-format encoding the `rd` field is the *source* of the
    /// data, not a destination register.
    SB(0x28),

    /// Store half-word (I-format, 0x29).
    ///
    /// `mem[rs1 + imm16][15:0] = rd[15:0]`.
    SH(0x29),

    /// Store word (I-format, 0x2B).
    ///
    /// `mem[rs1 + imm16][31:0] = rd`.
    SW(0x2B),

    // -------------------------------------------------------------------------
    // Simulator-specific
    // -------------------------------------------------------------------------

    /// Trap instruction (I-format, 0x3F).
    ///
    /// The 16-bit immediate selects the trap number. `trap 0` halts the
    /// simulator once the instruction retires through the WB stage. Other
    /// trap numbers are reserved for future use.
    TRAP(0x3F);

    // -------------------------------------------------------------------------
    // Fields and factory
    // -------------------------------------------------------------------------

    private final int code;

    /// Constructs an `OperationCode` constant with the given numeric code.
    ///
    /// @param code the 6-bit opcode value
    OperationCode(final int code) {
        this.code = code;
    }

    /// {@return the 6-bit numeric opcode value as it appears in bits 31–26 of the
    /// instruction word}
    public int code() {
        return this.code;
    }

    private static final int MAX_CODE = 0b11_1111; // 6 bits
    /// Fast O(1) lookup table, indexed directly by the 6-bit opcode value.
    /// Entries for unassigned codes remain `null`.
    private static final OperationCode[] BY_CODE = new OperationCode[MAX_CODE + 1];

    static {
        // Populate the lookup table once at class-load time.
        for (final var op : values()) {
            BY_CODE[op.code()] = op;
        }
    }

    /// Returns the `OperationCode` whose [#code()] equals the lower 6 bits of the
    /// argument.
    ///
    /// @param code a raw value; only the lower 6 bits are examined
    /// @return the matching `OperationCode` constant
    /// @throws IllegalArgumentException if no opcode is assigned to that value
    public static OperationCode fromCode(final int code) {
        final var op = BY_CODE[code & 0x3F];
        if (op == null) {
            throw new IllegalArgumentException(
                    "Unknown opcode: 0x" + Integer.toHexString(code & 0x3F));
        }
        return op;
    }
}
