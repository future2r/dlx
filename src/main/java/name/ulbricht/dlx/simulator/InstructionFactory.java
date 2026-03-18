package name.ulbricht.dlx.simulator;

/// Type-safe factory for encoding DLX instructions into 32-bit machine words.
///
/// Each DLX mnemonic has a dedicated static method that accepts only the
/// operands valid for that instruction and returns the encoded 32-bit word. The
/// words can be collected into a program array with [#program(int...)] and
/// loaded via [CPU#loadProgram(int[])].
///
/// ## Example
/// ```java
/// import static name.ulbricht.dlx.simulator.InstructionFactory.*;
///
/// int[] prog = program(
///     addi(1, 0, 10),   // R1 = 10
///     addi(2, 0, 20),   // R2 = 20
///     add(3, 1, 2),     // R3 = R1 + R2
///     halt()
/// );
/// cpu.loadProgram(prog);
/// ```
///
/// @see InstructionDecoder
public final class InstructionFactory {

    private InstructionFactory() {
    }

    // =========================================================================
    // Program builder
    // =========================================================================

    /// Collects encoded instruction words into a program array.
    ///
    /// @param words the encoded 32-bit instruction words
    /// @return the same words as an `int[]` suitable for [CPU#loadProgram(int[])]
    public static int[] program(final int... words) {
        return words;
    }

    // =========================================================================
    // R-format instructions
    // =========================================================================

    /// Encodes a NOP (no-operation).
    ///
    /// Canonical encoding: `SLL R0, R0, R0` with func = NOP (0x00).
    ///
    /// @return the encoded 32-bit instruction word
    public static int nop() {
        return encodeR(FunctionCode.NOP, 0, 0, 0);
    }

    /// Encodes `SLL rd, rs1, rs2` — shift left logical.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sll(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SLL, rs1, rs2, rd);
    }

    /// Encodes `SRL rd, rs1, rs2` — shift right logical (zero-fill).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int srl(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SRL, rs1, rs2, rd);
    }

    /// Encodes `SRA rd, rs1, rs2` — shift right arithmetic (sign-fill).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sra(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SRA, rs1, rs2, rd);
    }

    /// Encodes `ADD rd, rs1, rs2` — signed addition.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int add(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.ADD, rs1, rs2, rd);
    }

    /// Encodes `ADDU rd, rs1, rs2` — unsigned addition.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int addu(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.ADDU, rs1, rs2, rd);
    }

    /// Encodes `SUB rd, rs1, rs2` — signed subtraction.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sub(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SUB, rs1, rs2, rd);
    }

    /// Encodes `SUBU rd, rs1, rs2` — unsigned subtraction.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int subu(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SUBU, rs1, rs2, rd);
    }

    /// Encodes `AND rd, rs1, rs2` — bitwise AND.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int and(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.AND, rs1, rs2, rd);
    }

    /// Encodes `OR rd, rs1, rs2` — bitwise OR.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int or(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.OR, rs1, rs2, rd);
    }

    /// Encodes `XOR rd, rs1, rs2` — bitwise XOR.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int xor(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.XOR, rs1, rs2, rd);
    }

    /// Encodes `SEQ rd, rs1, rs2` — set if equal.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int seq(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SEQ, rs1, rs2, rd);
    }

    /// Encodes `SNE rd, rs1, rs2` — set if not equal.
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sne(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SNE, rs1, rs2, rd);
    }

    /// Encodes `SLT rd, rs1, rs2` — set if less than (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int slt(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SLT, rs1, rs2, rd);
    }

    /// Encodes `SGT rd, rs1, rs2` — set if greater than (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sgt(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SGT, rs1, rs2, rd);
    }

    /// Encodes `SLE rd, rs1, rs2` — set if less than or equal (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sle(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SLE, rs1, rs2, rd);
    }

    /// Encodes `SGE rd, rs1, rs2` — set if greater than or equal (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 first source register
    /// @param rs2 second source register
    /// @return the encoded 32-bit instruction word
    public static int sge(final int rd, final int rs1, final int rs2) {
        return encodeR(FunctionCode.SGE, rs1, rs2, rd);
    }

    // =========================================================================
    // I-format — arithmetic / logic immediate
    // =========================================================================

    /// Encodes `ADDI rd, rs1, imm` — add immediate, signed.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int addi(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.ADDI, rs1, rd, imm);
    }

    /// Encodes `ADDUI rd, rs1, imm` — add immediate, unsigned.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int addui(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.ADDUI, rs1, rd, imm);
    }

    /// Encodes `SUBI rd, rs1, imm` — subtract immediate, signed.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int subi(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SUBI, rs1, rd, imm);
    }

    /// Encodes `SUBUI rd, rs1, imm` — subtract immediate, unsigned.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int subui(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SUBUI, rs1, rd, imm);
    }

    /// Encodes `ANDI rd, rs1, imm` — bitwise AND immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int andi(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.ANDI, rs1, rd, imm);
    }

    /// Encodes `ORI rd, rs1, imm` — bitwise OR immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int ori(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.ORI, rs1, rd, imm);
    }

    /// Encodes `XORI rd, rs1, imm` — bitwise XOR immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int xori(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.XORI, rs1, rd, imm);
    }

    /// Encodes `LHI rd, imm` — load high immediate.
    ///
    /// Sets `rd = imm << 16`. The `rs1` field is implicitly zero.
    ///
    /// @param rd  destination register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int lhi(final int rd, final int imm) {
        return encodeI(OperationCode.LHI, 0, rd, imm);
    }

    // =========================================================================
    // I-format — shift immediate
    // =========================================================================

    /// Encodes `SLLI rd, rs1, imm` — shift left logical immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int slli(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SLLI, rs1, rd, imm);
    }

    /// Encodes `SRLI rd, rs1, imm` — shift right logical immediate (zero-fill).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int srli(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SRLI, rs1, rd, imm);
    }

    /// Encodes `SRAI rd, rs1, imm` — shift right arithmetic immediate (sign-fill).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int srai(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SRAI, rs1, rd, imm);
    }

    // =========================================================================
    // I-format — set immediate
    // =========================================================================

    /// Encodes `SEQI rd, rs1, imm` — set if equal immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int seqi(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SEQI, rs1, rd, imm);
    }

    /// Encodes `SNEI rd, rs1, imm` — set if not equal immediate.
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int snei(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SNEI, rs1, rd, imm);
    }

    /// Encodes `SLTI rd, rs1, imm` — set if less than immediate (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int slti(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SLTI, rs1, rd, imm);
    }

    /// Encodes `SGTI rd, rs1, imm` — set if greater than immediate (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int sgti(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SGTI, rs1, rd, imm);
    }

    /// Encodes `SLEI rd, rs1, imm` — set if less than or equal immediate (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int slei(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SLEI, rs1, rd, imm);
    }

    /// Encodes `SGEI rd, rs1, imm` — set if greater than or equal
    /// immediate (signed).
    ///
    /// @param rd  destination register
    /// @param rs1 source register
    /// @param imm signed 16-bit immediate value
    /// @return the encoded 32-bit instruction word
    public static int sgei(final int rd, final int rs1, final int imm) {
        return encodeI(OperationCode.SGEI, rs1, rd, imm);
    }

    // =========================================================================
    // I-format — branches
    // =========================================================================

    /// Encodes `BEQZ rs1, offset` — branch if rs1 equals zero.
    ///
    /// @param rs1    the register to test
    /// @param offset signed 16-bit byte offset from this instruction's PC
    /// @return the encoded 32-bit instruction word
    public static int beqz(final int rs1, final int offset) {
        return encodeI(OperationCode.BEQZ, rs1, 0, offset);
    }

    /// Encodes `BNEZ rs1, offset` — branch if rs1 is not zero.
    ///
    /// @param rs1    the register to test
    /// @param offset signed 16-bit byte offset from this instruction's PC
    /// @return the encoded 32-bit instruction word
    public static int bnez(final int rs1, final int offset) {
        return encodeI(OperationCode.BNEZ, rs1, 0, offset);
    }

    // =========================================================================
    // I-format — loads
    // =========================================================================

    /// Encodes `LB rd, offset(rs1)` — load byte, sign-extended.
    ///
    /// @param rd     destination register
    /// @param rs1    base address register
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int lb(final int rd, final int rs1, final int offset) {
        return encodeI(OperationCode.LB, rs1, rd, offset);
    }

    /// Encodes `LH rd, offset(rs1)` — load half-word, sign-extended.
    ///
    /// @param rd     destination register
    /// @param rs1    base address register
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int lh(final int rd, final int rs1, final int offset) {
        return encodeI(OperationCode.LH, rs1, rd, offset);
    }

    /// Encodes `LW rd, offset(rs1)` — load word.
    ///
    /// @param rd     destination register
    /// @param rs1    base address register
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int lw(final int rd, final int rs1, final int offset) {
        return encodeI(OperationCode.LW, rs1, rd, offset);
    }

    /// Encodes `LBU rd, offset(rs1)` — load byte, zero-extended.
    ///
    /// @param rd     destination register
    /// @param rs1    base address register
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int lbu(final int rd, final int rs1, final int offset) {
        return encodeI(OperationCode.LBU, rs1, rd, offset);
    }

    /// Encodes `LHU rd, offset(rs1)` — load half-word, zero-extended.
    ///
    /// @param rd     destination register
    /// @param rs1    base address register
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int lhu(final int rd, final int rs1, final int offset) {
        return encodeI(OperationCode.LHU, rs1, rd, offset);
    }

    // =========================================================================
    // I-format — stores
    // =========================================================================

    /// Encodes `SB offset(rs1), rd` — store byte.
    ///
    /// @param rs1    base address register
    /// @param rd     the register whose low byte is stored (source data)
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int sb(final int rs1, final int rd, final int offset) {
        return encodeI(OperationCode.SB, rs1, rd, offset);
    }

    /// Encodes `SH offset(rs1), rd` — store half-word.
    ///
    /// @param rs1    base address register
    /// @param rd     the register whose low half-word is stored (source data)
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int sh(final int rs1, final int rd, final int offset) {
        return encodeI(OperationCode.SH, rs1, rd, offset);
    }

    /// Encodes `SW offset(rs1), rd` — store word.
    ///
    /// @param rs1    base address register
    /// @param rd     the register whose value is stored (source data)
    /// @param offset signed 16-bit byte offset
    /// @return the encoded 32-bit instruction word
    public static int sw(final int rs1, final int rd, final int offset) {
        return encodeI(OperationCode.SW, rs1, rd, offset);
    }

    // =========================================================================
    // I-format — jump register
    // =========================================================================

    /// Encodes `JR rs1` — jump to address in register.
    ///
    /// @param rs1 the register containing the jump target address
    /// @return the encoded 32-bit instruction word
    public static int jr(final int rs1) {
        return encodeI(OperationCode.JR, rs1, 0, 0);
    }

    /// Encodes `JALR rs1` — jump and link register.
    ///
    /// Saves `PC + 4` in R31 and jumps to the address in `rs1`.
    ///
    /// @param rs1 the register containing the jump target address
    /// @return the encoded 32-bit instruction word
    public static int jalr(final int rs1) {
        return encodeI(OperationCode.JALR, rs1, 0, 0);
    }

    // =========================================================================
    // I-format — simulator
    // =========================================================================

    /// Encodes `HALT` — stop the simulator.
    ///
    /// @return the encoded 32-bit instruction word
    public static int halt() {
        return encodeI(OperationCode.HALT, 0, 0, 0);
    }

    // =========================================================================
    // J-format
    // =========================================================================

    /// Encodes `J distance` — unconditional jump.
    ///
    /// @param distance signed 26-bit byte offset from this instruction's PC
    /// @return the encoded 32-bit instruction word
    public static int j(final int distance) {
        return encodeJ(OperationCode.J, distance);
    }

    /// Encodes `JAL distance` — jump and link.
    ///
    /// Saves `PC + 4` in R31 and jumps to `PC + distance`.
    ///
    /// @param distance signed 26-bit byte offset from this instruction's PC
    /// @return the encoded 32-bit instruction word
    public static int jal(final int distance) {
        return encodeJ(OperationCode.JAL, distance);
    }

    // =========================================================================
    // Encoding helpers
    // =========================================================================

    /// Encodes an R-format instruction word.
    ///
    /// ```
    /// | SPECIAL (6) | rs1 (5) | rs2 (5) | rd (5) | 0 (5) | func (6) |
    /// ```
    ///
    private static int encodeR(final FunctionCode func, final int rs1, final int rs2, final int rd) {
        checkRegister(rs1, "rs1");
        checkRegister(rs2, "rs2");
        checkRegister(rd, "rd");

        return (OperationCode.SPECIAL.code << 26)
                | (rs1 << 21)
                | (rs2 << 16)
                | (rd << 11)
                | func.code;
    }

    /// Encodes an I-format instruction word.
    ///
    /// ```
    /// | opcode (6) | rs1 (5) | rd (5) | immediate (16) |
    /// ```
    ///
    private static int encodeI(final OperationCode op, final int rs1, final int rd, final int imm) {
        checkRegister(rs1, "rs1");
        checkRegister(rd, "rd");
        checkImmediate16(imm);

        return (op.code << 26)
                | (rs1 << 21)
                | (rd << 16)
                | (imm & 0xFFFF);
    }

    /// Encodes a J-format instruction word.
    ///
    /// ```
    /// | opcode (6) | distance (26) |
    /// ```
    ///
    private static int encodeJ(final OperationCode op, final int distance) {
        checkDistance26(distance);

        return (op.code << 26)
                | (distance & 0x03FF_FFFF);
    }

    // =========================================================================
    // Validation
    // =========================================================================

    private static void checkRegister(final int reg, final String name) {
        if (reg < 0 || reg > 31) {
            throw new IllegalArgumentException(
                    name + " must be in range 0..31, got: " + reg);
        }
    }

    private static void checkImmediate16(final int imm) {
        if (imm < -32_768 || imm > 32_767) {
            throw new IllegalArgumentException(
                    "Immediate must be in range -32768..32767, got: " + imm);
        }
    }

    private static void checkDistance26(final int distance) {
        final int min = -(1 << 25); // -33_554_432
        final int max = (1 << 25) - 1; // 33_554_431
        if (distance < min || distance > max) {
            throw new IllegalArgumentException(
                    "Distance must be in range " + min + ".." + max + ", got: " + distance);
        }
    }
}
