package name.ulbricht.dlx.asm;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import name.ulbricht.dlx.simulator.FunctionCode;
import name.ulbricht.dlx.simulator.OperationCode;

/// All DLX assembly instructions with their mnemonic, operand format,
/// machine-code mapping, and a human-readable description.
///
/// This enum is the single source of truth for instruction metadata used by the
/// lexer (token classification), parser (operand format dispatch), and compiler
/// (opcode/function-code encoding). The [#description] field can also serve as
/// the basis for in-application help.
///
/// Use [#fromMnemonic(String)] for an O(1) lookup by lowercase mnemonic, or
/// [#isKnown(String)] for a fast membership test.
public enum Instruction {

    // -------------------------------------------------------------------------
    // R-format - arithmetic
    // -------------------------------------------------------------------------

    /// Signed addition: `rd = rs1 + rs2`.
    ADD("add", OperandFormat.R, FunctionCode.ADD, "Signed addition: rd = rs1 + rs2"),

    /// Unsigned addition: `rd = rs1 + rs2` (no overflow detection).
    ADDU("addu", OperandFormat.R, FunctionCode.ADDU, "Unsigned addition: rd = rs1 + rs2"),

    /// Signed subtraction: `rd = rs1 - rs2`.
    SUB("sub", OperandFormat.R, FunctionCode.SUB, "Signed subtraction: rd = rs1 - rs2"),

    /// Unsigned subtraction: `rd = rs1 - rs2` (no overflow detection).
    SUBU("subu", OperandFormat.R, FunctionCode.SUBU, "Unsigned subtraction: rd = rs1 - rs2"),

    // -------------------------------------------------------------------------
    // R-format - logic
    // -------------------------------------------------------------------------

    /// Bitwise AND: `rd = rs1 & rs2`.
    AND("and", OperandFormat.R, FunctionCode.AND, "Bitwise AND: rd = rs1 & rs2"),

    /// Bitwise OR: `rd = rs1 | rs2`.
    OR("or", OperandFormat.R, FunctionCode.OR, "Bitwise OR: rd = rs1 | rs2"),

    /// Bitwise XOR: `rd = rs1 ^ rs2`.
    XOR("xor", OperandFormat.R, FunctionCode.XOR, "Bitwise XOR: rd = rs1 ^ rs2"),

    // -------------------------------------------------------------------------
    // R-format - shifts
    // -------------------------------------------------------------------------

    /// Shift left logical: `rd = rs1 << (rs2 & 0x1F)`.
    SLL("sll", OperandFormat.R, FunctionCode.SLL, "Shift left logical: rd = rs1 << (rs2 & 0x1F)"),

    /// Shift right logical: `rd = rs1 >>> (rs2 & 0x1F)`.
    SRL("srl", OperandFormat.R, FunctionCode.SRL, "Shift right logical: rd = rs1 >>> (rs2 & 0x1F)"),

    /// Shift right arithmetic: `rd = rs1 >> (rs2 & 0x1F)`.
    SRA("sra", OperandFormat.R, FunctionCode.SRA, "Shift right arithmetic: rd = rs1 >> (rs2 & 0x1F)"),

    // -------------------------------------------------------------------------
    // R-format - set-if comparisons
    // -------------------------------------------------------------------------

    /// Set if less than: `rd = (rs1 < rs2) ? 1 : 0`.
    SLT("slt", OperandFormat.R, FunctionCode.SLT, "Set if less than: rd = (rs1 < rs2) ? 1 : 0"),

    /// Set if less than or equal: `rd = (rs1 <= rs2) ? 1 : 0`.
    SLE("sle", OperandFormat.R, FunctionCode.SLE, "Set if less than or equal: rd = (rs1 <= rs2) ? 1 : 0"),

    /// Set if equal: `rd = (rs1 == rs2) ? 1 : 0`.
    SEQ("seq", OperandFormat.R, FunctionCode.SEQ, "Set if equal: rd = (rs1 == rs2) ? 1 : 0"),

    /// Set if greater than: `rd = (rs1 > rs2) ? 1 : 0`.
    SGT("sgt", OperandFormat.R, FunctionCode.SGT, "Set if greater than: rd = (rs1 > rs2) ? 1 : 0"),

    /// Set if greater than or equal: `rd = (rs1 >= rs2) ? 1 : 0`.
    SGE("sge", OperandFormat.R, FunctionCode.SGE, "Set if greater than or equal: rd = (rs1 >= rs2) ? 1 : 0"),

    /// Set if not equal: `rd = (rs1 != rs2) ? 1 : 0`.
    SNE("sne", OperandFormat.R, FunctionCode.SNE, "Set if not equal: rd = (rs1 != rs2) ? 1 : 0"),

    // -------------------------------------------------------------------------
    // I-format - arithmetic immediate
    // -------------------------------------------------------------------------

    /// Add immediate, signed: `rd = rs1 + sign_extend(imm16)`.
    ADDI("addi", OperandFormat.I_ARITH, OperationCode.ADDI, "Add immediate signed: rd = rs1 + imm"),

    /// Add immediate, unsigned: `rd = rs1 + sign_extend(imm16)`.
    ADDUI("addui", OperandFormat.I_ARITH, OperationCode.ADDUI, "Add immediate unsigned: rd = rs1 + imm"),

    /// Subtract immediate, signed: `rd = rs1 - sign_extend(imm16)`.
    SUBI("subi", OperandFormat.I_ARITH, OperationCode.SUBI, "Subtract immediate signed: rd = rs1 - imm"),

    /// Subtract immediate, unsigned: `rd = rs1 - sign_extend(imm16)`.
    SUBUI("subui", OperandFormat.I_ARITH, OperationCode.SUBUI, "Subtract immediate unsigned: rd = rs1 - imm"),

    // -------------------------------------------------------------------------
    // I-format - logic immediate
    // -------------------------------------------------------------------------

    /// Bitwise AND immediate: `rd = rs1 & sign_extend(imm16)`.
    ANDI("andi", OperandFormat.I_ARITH, OperationCode.ANDI, "Bitwise AND immediate: rd = rs1 & imm"),

    /// Bitwise OR immediate: `rd = rs1 | sign_extend(imm16)`.
    ORI("ori", OperandFormat.I_ARITH, OperationCode.ORI, "Bitwise OR immediate: rd = rs1 | imm"),

    /// Bitwise XOR immediate: `rd = rs1 ^ sign_extend(imm16)`.
    XORI("xori", OperandFormat.I_ARITH, OperationCode.XORI, "Bitwise XOR immediate: rd = rs1 ^ imm"),

    // -------------------------------------------------------------------------
    // I-format - shift immediate
    // -------------------------------------------------------------------------

    /// Shift left logical immediate: `rd = rs1 << (imm16 & 0x1F)`.
    SLLI("slli", OperandFormat.I_ARITH, OperationCode.SLLI, "Shift left logical immediate: rd = rs1 << imm"),

    /// Shift right logical immediate: `rd = rs1 >>> (imm16 & 0x1F)`.
    SRLI("srli", OperandFormat.I_ARITH, OperationCode.SRLI, "Shift right logical immediate: rd = rs1 >>> imm"),

    /// Shift right arithmetic immediate: `rd = rs1 >> (imm16 & 0x1F)`.
    SRAI("srai", OperandFormat.I_ARITH, OperationCode.SRAI, "Shift right arithmetic immediate: rd = rs1 >> imm"),

    // -------------------------------------------------------------------------
    // I-format - set-if immediate
    // -------------------------------------------------------------------------

    /// Set if less than immediate: `rd = (rs1 < imm) ? 1 : 0`.
    SLTI("slti", OperandFormat.I_ARITH, OperationCode.SLTI, "Set if less than immediate: rd = (rs1 < imm) ? 1 : 0"),

    /// Set if less than or equal immediate: `rd = (rs1 <= imm) ? 1 : 0`.
    SLEI("slei", OperandFormat.I_ARITH, OperationCode.SLEI, "Set if less than or equal immediate: rd = (rs1 <= imm) ? 1 : 0"),

    /// Set if equal immediate: `rd = (rs1 == imm) ? 1 : 0`.
    SEQI("seqi", OperandFormat.I_ARITH, OperationCode.SEQI, "Set if equal immediate: rd = (rs1 == imm) ? 1 : 0"),

    /// Set if greater than immediate: `rd = (rs1 > imm) ? 1 : 0`.
    SGTI("sgti", OperandFormat.I_ARITH, OperationCode.SGTI, "Set if greater than immediate: rd = (rs1 > imm) ? 1 : 0"),

    /// Set if greater than or equal immediate: `rd = (rs1 >= imm) ? 1 : 0`.
    SGEI("sgei", OperandFormat.I_ARITH, OperationCode.SGEI, "Set if greater than or equal immediate: rd = (rs1 >= imm) ? 1 : 0"),

    /// Set if not equal immediate: `rd = (rs1 != imm) ? 1 : 0`.
    SNEI("snei", OperandFormat.I_ARITH, OperationCode.SNEI, "Set if not equal immediate: rd = (rs1 != imm) ? 1 : 0"),

    // -------------------------------------------------------------------------
    // I-format - loads
    // -------------------------------------------------------------------------

    /// Load byte, sign-extended: `rd = sign_extend(mem[rs1 + imm16])`.
    LB("lb", OperandFormat.LOAD, OperationCode.LB, "Load byte, sign-extended"),

    /// Load half-word, sign-extended: `rd = sign_extend(mem[rs1 + imm16])`.
    LH("lh", OperandFormat.LOAD, OperationCode.LH, "Load half-word, sign-extended"),

    /// Load word: `rd = mem[rs1 + imm16]`.
    LW("lw", OperandFormat.LOAD, OperationCode.LW, "Load word"),

    /// Load byte, zero-extended: `rd = zero_extend(mem[rs1 + imm16])`.
    LBU("lbu", OperandFormat.LOAD, OperationCode.LBU, "Load byte, zero-extended"),

    /// Load half-word, zero-extended: `rd = zero_extend(mem[rs1 + imm16])`.
    LHU("lhu", OperandFormat.LOAD, OperationCode.LHU, "Load half-word, zero-extended"),

    // -------------------------------------------------------------------------
    // I-format - stores
    // -------------------------------------------------------------------------

    /// Store byte: `mem[rs1 + imm16] = rsrc[7:0]`.
    SB("sb", OperandFormat.STORE, OperationCode.SB, "Store byte"),

    /// Store half-word: `mem[rs1 + imm16] = rsrc[15:0]`.
    SH("sh", OperandFormat.STORE, OperationCode.SH, "Store half-word"),

    /// Store word: `mem[rs1 + imm16] = rsrc`.
    SW("sw", OperandFormat.STORE, OperationCode.SW, "Store word"),

    // -------------------------------------------------------------------------
    // I-format - load high immediate
    // -------------------------------------------------------------------------

    /// Load high immediate: `rd = imm16 << 16`.
    LHI("lhi", OperandFormat.RD_IMM, OperationCode.LHI, "Load high immediate: rd = imm << 16"),

    // -------------------------------------------------------------------------
    // I-format - conditional branches
    // -------------------------------------------------------------------------

    /// Branch if equal to zero: if `rs1 == 0` then `PC = PC + offset`.
    BEQZ("beqz", OperandFormat.RS_LABEL, OperationCode.BEQZ, "Branch if equal to zero"),

    /// Branch if not equal to zero: if `rs1 != 0` then `PC = PC + offset`.
    BNEZ("bnez", OperandFormat.RS_LABEL, OperationCode.BNEZ, "Branch if not equal to zero"),

    // -------------------------------------------------------------------------
    // J-format
    // -------------------------------------------------------------------------

    /// Unconditional jump: `PC = PC + offset`.
    J("j", OperandFormat.LABEL, OperationCode.J, "Unconditional jump"),

    /// Jump and link: `R31 = PC + 4; PC = PC + offset`.
    JAL("jal", OperandFormat.LABEL, OperationCode.JAL, "Jump and link (save return address in R31)"),

    // -------------------------------------------------------------------------
    // I-format - jump register
    // -------------------------------------------------------------------------

    /// Jump register: `PC = rs1`.
    JR("jr", OperandFormat.RS, OperationCode.JR, "Jump to address in register"),

    /// Jump and link register: `R31 = PC + 4; PC = rs1`.
    JALR("jalr", OperandFormat.RS, OperationCode.JALR, "Jump and link register"),

    // -------------------------------------------------------------------------
    // Simulator-specific
    // -------------------------------------------------------------------------

    /// Trap: `trap 0` halts the simulator.
    TRAP("trap", OperandFormat.IMM, OperationCode.TRAP, "Trap: trap 0 halts the simulator");

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

    /// A short human-readable description suitable for in-application help.
    public final String description;

    /// Constructs an R-format instruction constant (uses [FunctionCode]).
    Instruction(final String mnemonic, final OperandFormat format,
                final FunctionCode functionCode, final String description) {
        this.mnemonic = mnemonic;
        this.format = format;
        this.operationCode = null;
        this.functionCode = functionCode;
        this.description = description;
    }

    /// Constructs an I/J-format instruction constant (uses [OperationCode]).
    Instruction(final String mnemonic, final OperandFormat format,
                final OperationCode operationCode, final String description) {
        this.mnemonic = mnemonic;
        this.format = format;
        this.operationCode = operationCode;
        this.functionCode = null;
        this.description = description;
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
