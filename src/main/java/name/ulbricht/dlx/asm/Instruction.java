package name.ulbricht.dlx.asm;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import name.ulbricht.dlx.simulator.FunctionCode;
import name.ulbricht.dlx.simulator.OperationCode;

/// All DLX assembly instructions with their mnemonic, operand format, and
/// machine-code mapping.
///
/// This enum is the single source of truth for instruction metadata used by the
/// lexer (token classification), parser (operand format dispatch), and compiler
/// (opcode/function-code encoding).
///
/// Use [#fromMnemonic(String)] for an O(1) lookup by lowercase mnemonic, or
/// [#isKnown(String)] for a fast membership test.
public enum Instruction {

    // -------------------------------------------------------------------------
    // R-format - arithmetic
    // -------------------------------------------------------------------------

    /// Signed addition: `rd = rs1 + rs2`.
    ADD("add", OperandFormat.R, FunctionCode.ADD),

    /// Unsigned addition: `rd = rs1 + rs2` (no overflow detection).
    ADDU("addu", OperandFormat.R, FunctionCode.ADDU),

    /// Signed subtraction: `rd = rs1 - rs2`.
    SUB("sub", OperandFormat.R, FunctionCode.SUB),

    /// Unsigned subtraction: `rd = rs1 - rs2` (no overflow detection).
    SUBU("subu", OperandFormat.R, FunctionCode.SUBU),

    // -------------------------------------------------------------------------
    // R-format - logic
    // -------------------------------------------------------------------------

    /// Bitwise AND: `rd = rs1 & rs2`.
    AND("and", OperandFormat.R, FunctionCode.AND),

    /// Bitwise OR: `rd = rs1 | rs2`.
    OR("or", OperandFormat.R, FunctionCode.OR),

    /// Bitwise XOR: `rd = rs1 ^ rs2`.
    XOR("xor", OperandFormat.R, FunctionCode.XOR),

    // -------------------------------------------------------------------------
    // R-format - shifts
    // -------------------------------------------------------------------------

    /// Shift left logical: `rd = rs1 << (rs2 & 0x1F)`.
    SLL("sll", OperandFormat.R, FunctionCode.SLL),

    /// Shift right logical: `rd = rs1 >>> (rs2 & 0x1F)`.
    SRL("srl", OperandFormat.R, FunctionCode.SRL),

    /// Shift right arithmetic: `rd = rs1 >> (rs2 & 0x1F)`.
    SRA("sra", OperandFormat.R, FunctionCode.SRA),

    // -------------------------------------------------------------------------
    // R-format - set-if comparisons
    // -------------------------------------------------------------------------

    /// Set if less than: `rd = (rs1 < rs2) ? 1 : 0`.
    SLT("slt", OperandFormat.R, FunctionCode.SLT),

    /// Set if less than or equal: `rd = (rs1 <= rs2) ? 1 : 0`.
    SLE("sle", OperandFormat.R, FunctionCode.SLE),

    /// Set if equal: `rd = (rs1 == rs2) ? 1 : 0`.
    SEQ("seq", OperandFormat.R, FunctionCode.SEQ),

    /// Set if greater than: `rd = (rs1 > rs2) ? 1 : 0`.
    SGT("sgt", OperandFormat.R, FunctionCode.SGT),

    /// Set if greater than or equal: `rd = (rs1 >= rs2) ? 1 : 0`.
    SGE("sge", OperandFormat.R, FunctionCode.SGE),

    /// Set if not equal: `rd = (rs1 != rs2) ? 1 : 0`.
    SNE("sne", OperandFormat.R, FunctionCode.SNE),

    // -------------------------------------------------------------------------
    // I-format - arithmetic immediate
    // -------------------------------------------------------------------------

    /// Add immediate, signed: `rd = rs1 + sign_extend(imm16)`.
    ADDI("addi", OperandFormat.I_ARITH, OperationCode.ADDI),

    /// Add immediate, unsigned: `rd = rs1 + sign_extend(imm16)`.
    ADDUI("addui", OperandFormat.I_ARITH, OperationCode.ADDUI),

    /// Subtract immediate, signed: `rd = rs1 - sign_extend(imm16)`.
    SUBI("subi", OperandFormat.I_ARITH, OperationCode.SUBI),

    /// Subtract immediate, unsigned: `rd = rs1 - sign_extend(imm16)`.
    SUBUI("subui", OperandFormat.I_ARITH, OperationCode.SUBUI),

    // -------------------------------------------------------------------------
    // I-format - logic immediate
    // -------------------------------------------------------------------------

    /// Bitwise AND immediate: `rd = rs1 & sign_extend(imm16)`.
    ANDI("andi", OperandFormat.I_ARITH, OperationCode.ANDI),

    /// Bitwise OR immediate: `rd = rs1 | sign_extend(imm16)`.
    ORI("ori", OperandFormat.I_ARITH, OperationCode.ORI),

    /// Bitwise XOR immediate: `rd = rs1 ^ sign_extend(imm16)`.
    XORI("xori", OperandFormat.I_ARITH, OperationCode.XORI),

    // -------------------------------------------------------------------------
    // I-format - shift immediate
    // -------------------------------------------------------------------------

    /// Shift left logical immediate: `rd = rs1 << (imm16 & 0x1F)`.
    SLLI("slli", OperandFormat.I_ARITH, OperationCode.SLLI),

    /// Shift right logical immediate: `rd = rs1 >>> (imm16 & 0x1F)`.
    SRLI("srli", OperandFormat.I_ARITH, OperationCode.SRLI),

    /// Shift right arithmetic immediate: `rd = rs1 >> (imm16 & 0x1F)`.
    SRAI("srai", OperandFormat.I_ARITH, OperationCode.SRAI),

    // -------------------------------------------------------------------------
    // I-format - set-if immediate
    // -------------------------------------------------------------------------

    /// Set if less than immediate: `rd = (rs1 < imm) ? 1 : 0`.
    SLTI("slti", OperandFormat.I_ARITH, OperationCode.SLTI),

    /// Set if less than or equal immediate: `rd = (rs1 <= imm) ? 1 : 0`.
    SLEI("slei", OperandFormat.I_ARITH, OperationCode.SLEI),

    /// Set if equal immediate: `rd = (rs1 == imm) ? 1 : 0`.
    SEQI("seqi", OperandFormat.I_ARITH, OperationCode.SEQI),

    /// Set if greater than immediate: `rd = (rs1 > imm) ? 1 : 0`.
    SGTI("sgti", OperandFormat.I_ARITH, OperationCode.SGTI),

    /// Set if greater than or equal immediate: `rd = (rs1 >= imm) ? 1 : 0`.
    SGEI("sgei", OperandFormat.I_ARITH, OperationCode.SGEI),

    /// Set if not equal immediate: `rd = (rs1 != imm) ? 1 : 0`.
    SNEI("snei", OperandFormat.I_ARITH, OperationCode.SNEI),

    // -------------------------------------------------------------------------
    // I-format - loads
    // -------------------------------------------------------------------------

    /// Load byte, sign-extended: `rd = sign_extend(mem[rs1 + imm16])`.
    LB("lb", OperandFormat.LOAD, OperationCode.LB),

    /// Load half-word, sign-extended: `rd = sign_extend(mem[rs1 + imm16])`.
    LH("lh", OperandFormat.LOAD, OperationCode.LH),

    /// Load word: `rd = mem[rs1 + imm16]`.
    LW("lw", OperandFormat.LOAD, OperationCode.LW),

    /// Load byte, zero-extended: `rd = zero_extend(mem[rs1 + imm16])`.
    LBU("lbu", OperandFormat.LOAD, OperationCode.LBU),

    /// Load half-word, zero-extended: `rd = zero_extend(mem[rs1 + imm16])`.
    LHU("lhu", OperandFormat.LOAD, OperationCode.LHU),

    // -------------------------------------------------------------------------
    // I-format - stores
    // -------------------------------------------------------------------------

    /// Store byte: `mem[rs1 + imm16] = rsrc[7:0]`.
    SB("sb", OperandFormat.STORE, OperationCode.SB),

    /// Store half-word: `mem[rs1 + imm16] = rsrc[15:0]`.
    SH("sh", OperandFormat.STORE, OperationCode.SH),

    /// Store word: `mem[rs1 + imm16] = rsrc`.
    SW("sw", OperandFormat.STORE, OperationCode.SW),

    // -------------------------------------------------------------------------
    // I-format - load high immediate
    // -------------------------------------------------------------------------

    /// Load high immediate: `rd = imm16 << 16`.
    LHI("lhi", OperandFormat.RD_IMM, OperationCode.LHI),

    // -------------------------------------------------------------------------
    // I-format - conditional branches
    // -------------------------------------------------------------------------

    /// Branch if equal to zero: if `rs1 == 0` then `PC = PC + offset`.
    BEQZ("beqz", OperandFormat.RS_LABEL, OperationCode.BEQZ),

    /// Branch if not equal to zero: if `rs1 != 0` then `PC = PC + offset`.
    BNEZ("bnez", OperandFormat.RS_LABEL, OperationCode.BNEZ),

    // -------------------------------------------------------------------------
    // J-format
    // -------------------------------------------------------------------------

    /// Unconditional jump: `PC = PC + offset`.
    J("j", OperandFormat.LABEL, OperationCode.J),

    /// Jump and link: `R31 = PC + 4; PC = PC + offset`.
    JAL("jal", OperandFormat.LABEL, OperationCode.JAL),

    // -------------------------------------------------------------------------
    // I-format - jump register
    // -------------------------------------------------------------------------

    /// Jump register: `PC = rs1`.
    JR("jr", OperandFormat.RS, OperationCode.JR),

    /// Jump and link register: `R31 = PC + 4; PC = rs1`.
    JALR("jalr", OperandFormat.RS, OperationCode.JALR),

    // -------------------------------------------------------------------------
    // Simulator-specific
    // -------------------------------------------------------------------------

    /// Trap: `trap 0` halts the simulator.
    TRAP("trap", OperandFormat.IMM, OperationCode.TRAP);

    // -------------------------------------------------------------------------
    // Fields and lookup
    // -------------------------------------------------------------------------

    /// The lowercase mnemonic as it appears in assembly source code.
    public final String mnemonic;

    /// The operand format that determines how the parser reads this instruction.
    public final OperandFormat format;

    /// The I/J-format operation code, or `null` for R-format instructions.
    public final OperationCode operationCode;

    /// The R-format function code, or `null` for non-R-format instructions.
    public final FunctionCode functionCode;

    /// Constructs an R-format instruction constant (uses [FunctionCode]).
    Instruction(final String mnemonic, final OperandFormat format,
                final FunctionCode functionCode) {
        this.mnemonic = mnemonic;
        this.format = format;
        this.operationCode = null;
        this.functionCode = functionCode;
    }

    /// Constructs an I/J-format instruction constant (uses [OperationCode]).
    Instruction(final String mnemonic, final OperandFormat format,
                final OperationCode operationCode) {
        this.mnemonic = mnemonic;
        this.format = format;
        this.operationCode = operationCode;
        this.functionCode = null;
    }

    /// O(1) lookup table keyed by lowercase mnemonic.
    private static final Map<String, Instruction> BY_MNEMONIC =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(i -> i.mnemonic, i -> i));

    /// Returns the instruction whose [#mnemonic] equals the given string.
    ///
    /// @param mnemonic a lowercase mnemonic such as `"add"` or `"beqz"`
    /// @return the matching constant, or empty if unknown
    public static Optional<Instruction> fromMnemonic(final String mnemonic) {
        return Optional.ofNullable(BY_MNEMONIC.get(mnemonic));
    }

    /// Tests whether the given string is a known instruction mnemonic.
    ///
    /// @param mnemonic a lowercase mnemonic
    /// @return `true` if a matching [Instruction] constant exists
    public static boolean isKnown(final String mnemonic) {
        return BY_MNEMONIC.containsKey(mnemonic);
    }
}
