package name.ulbricht.dlx.asm.compiler;

import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.Instruction;
import name.ulbricht.dlx.asm.parser.ImmediateOperand;
import name.ulbricht.dlx.asm.parser.LabelImmediateOperand;
import name.ulbricht.dlx.asm.parser.LabelMemoryOperand;
import name.ulbricht.dlx.asm.parser.LabelOperand;
import name.ulbricht.dlx.asm.parser.MemoryOperand;
import name.ulbricht.dlx.asm.parser.ParsedDataDeclaration;
import name.ulbricht.dlx.asm.parser.ParsedElement;
import name.ulbricht.dlx.asm.parser.ParsedInstruction;
import name.ulbricht.dlx.asm.parser.ParsedProgram;
import name.ulbricht.dlx.asm.parser.RegisterOperand;
import name.ulbricht.dlx.simulator.FunctionCode;
import name.ulbricht.dlx.simulator.OperationCode;

/// DLX assembler compiler.
///
/// Translates a [ParsedProgram] into a flat byte array that can be loaded into
/// the CPU. The compiler uses a two-pass approach: pass 1 collects labels and
/// computes section sizes, pass 2 emits the binary.
public final class Compiler {

    private static final System.Logger log = System.getLogger(Compiler.class.getName());

    private final List<Diagnostic> diagnostics = new ArrayList<>();
    private final Map<String, Integer> symbols = new LinkedHashMap<>();

    /// Creates a new instance.
    public Compiler() {
    }

    /// Compiles the given parsed program into a compiled program.
    ///
    /// @param parsed the parsed program to compile
    /// @return the compiled program
    public CompiledProgram compile(final ParsedProgram parsed) {
        requireNonNull(parsed);

        this.diagnostics.clear();
        this.symbols.clear();

        // --- Pass 1: measure sizes and collect labels ---

        var dataSize = 0;
        for (final var decl : parsed.data()) {
            registerLabel(decl.label(), decl, dataSize);
            dataSize = advanceDataAddress(dataSize, decl);
        }

        final var codeSize = parsed.code().size() * 4;
        var codeAddr = dataSize;
        for (final var instr : parsed.code()) {
            registerLabel(instr.label(), instr, codeAddr);
            codeAddr += 4;
        }

        // --- Pass 2: emit bytes ---

        final var program = new byte[dataSize + codeSize];

        var offset = 0;
        for (final var decl : parsed.data()) {
            offset = emitData(program, offset, decl);
        }

        var instrAddr = dataSize;
        for (final var instr : parsed.code()) {
            final var word = encodeInstruction(instr, instrAddr);
            emitWord(program, instrAddr, word);
            instrAddr += 4;
        }

        if (hasErrors())
            return new CompiledProgram(parsed.id(), new byte[0], 0, this.diagnostics);

        return new CompiledProgram(parsed.id(), program, dataSize, this.diagnostics);
    }

    private void registerLabel(final String label, final ParsedElement element, final int address) {
        if (label != null) {
            if (this.symbols.containsKey(label)) {
                addError("Duplicate label '" + label + "'", element);
            } else {
                this.symbols.put(label, Integer.valueOf(address));
            }
        }
    }

    private int advanceDataAddress(final int address, final ParsedDataDeclaration decl) {
        return switch (decl.directive()) {
            case "word" -> address + 4 * decl.values().size();
            case "half" -> address + 2 * decl.values().size();
            case "byte" -> address + decl.values().size();
            case "ascii" -> address + stringValue(decl).getBytes(StandardCharsets.UTF_8).length;
            case "asciiz" -> address + stringValue(decl).getBytes(StandardCharsets.UTF_8).length + 1;
            case "space" -> address + intValue(decl, 0);
            case "align" -> {
                final var n = intValue(decl, 0);
                if (validateAlignment(n, decl) && n > 0) {
                    final var alignment = 1 << n; // 2^n
                    final var rem = address % alignment;
                    yield rem == 0 ? address : address + alignment - rem;
                }
                yield address;
            }
            default -> {
                addError("Unknown directive '." + decl.directive() + "'", decl);
                yield address;
            }
        };
    }

    private int emitData(final byte[] program, final int startOffset, final ParsedDataDeclaration decl) {
        var offset = startOffset;

        switch (decl.directive()) {
            case "word" -> {
                for (final var val : decl.values()) {
                    emitWord(program, offset, ((Integer) val).intValue());
                    offset += 4;
                }
            }
            case "half" -> {
                for (final var val : decl.values()) {
                    emitHalf(program, offset, ((Integer) val).intValue());
                    offset += 2;
                }
            }
            case "byte" -> {
                for (final var val : decl.values()) {
                    program[offset++] = (byte) ((Integer) val).intValue();
                }
            }
            case "ascii" -> {
                final var bytes = stringValue(decl).getBytes(StandardCharsets.UTF_8);
                System.arraycopy(bytes, 0, program, offset, bytes.length);
                offset += bytes.length;
            }
            case "asciiz" -> {
                final var bytes = stringValue(decl).getBytes(StandardCharsets.UTF_8);
                System.arraycopy(bytes, 0, program, offset, bytes.length);
                offset += bytes.length;
                program[offset++] = 0;
            }
            case "space" -> {
                final var n = intValue(decl, 0);
                // bytes are already zero-initialised
                offset += n;
            }
            case "align" -> {
                final var n = intValue(decl, 0);
                if (validateAlignment(n, decl) && n > 0) {
                    final var alignment = 1 << n; // 2^n
                    final var rem = offset % alignment;
                    if (rem != 0) {
                        offset += alignment - rem;
                    }
                }
            }
            default -> addError("Unknown directive '." + decl.directive() + "'", decl);
        }
        return offset;
    }

    private int encodeInstruction(final ParsedInstruction instr, final int addr) {
        final var def = Instruction.fromMnemonic(instr.opcode()).orElse(null);
        if (def == null) {
            addError("Unknown instruction '" + instr.opcode() + "'", instr);
            return 0;
        }
        return switch (def.format()) {
            case R -> encodeRFormat(instr, def.functionCode());
            case I_ARITH -> encodeIArith(instr, def.operationCode());
            case LOAD -> encodeILoad(instr, def.operationCode());
            case STORE -> encodeIStore(instr, def.operationCode());
            case RD_IMM -> encodeILhi(instr);
            case RS_LABEL -> encodeIBranch(instr, def.operationCode(), addr);
            case LABEL -> encodeJFormat(instr, def.operationCode(), addr);
            case RS -> encodeIJumpReg(instr, def.operationCode());
            case IMM -> encodeTrap(instr);
        };
    }

    private int encodeRFormat(final ParsedInstruction instr, final FunctionCode func) {
        if (!checkOperandCount(instr, 3))
            return 0;
        final var rd = expectRegister(instr, 0);
        final var rs1 = expectRegister(instr, 1);
        final var rs2 = expectRegister(instr, 2);
        if (rd < 0 || rs1 < 0 || rs2 < 0)
            return 0;
        return encodeR(rs1, rs2, rd, func.code());
    }

    private int encodeIArith(final ParsedInstruction instr, final OperationCode opcode) {
        if (!checkOperandCount(instr, 3))
            return 0;
        final var rd = expectRegister(instr, 0);
        final var rs1 = expectRegister(instr, 1);
        final var imm = resolveImmediate(instr, 2);
        if (rd < 0 || rs1 < 0)
            return 0;
        return encodeI(opcode.code(), rs1, rd, imm);
    }

    private int encodeILoad(final ParsedInstruction instr, final OperationCode opcode) {
        if (!checkOperandCount(instr, 2))
            return 0;
        final var rd = expectRegister(instr, 0);
        final var mem = resolveMemory(instr, 1);
        if (rd < 0 || mem == null)
            return 0;
        return encodeI(opcode.code(), mem[1], rd, mem[0]);
    }

    private int encodeIStore(final ParsedInstruction instr, final OperationCode opcode) {
        if (!checkOperandCount(instr, 2))
            return 0;
        final var mem = resolveMemory(instr, 0);
        final var dataReg = expectRegister(instr, 1);
        if (mem == null || dataReg < 0)
            return 0;
        return encodeI(opcode.code(), mem[1], dataReg, mem[0]);
    }

    private int encodeIBranch(final ParsedInstruction instr, final OperationCode opcode, final int addr) {
        if (!checkOperandCount(instr, 2))
            return 0;
        final var rs1 = expectRegister(instr, 0);
        final var offset = resolveBranchTarget(instr, 1, addr);
        if (rs1 < 0)
            return 0;
        if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE) {
            addError("Branch offset out of 16-bit signed range", instr);
        }
        return encodeI(opcode.code(), rs1, 0, offset);
    }

    private int encodeIJumpReg(final ParsedInstruction instr, final OperationCode opcode) {
        if (!checkOperandCount(instr, 1))
            return 0;
        final var rs1 = expectRegister(instr, 0);
        if (rs1 < 0)
            return 0;
        return encodeI(opcode.code(), rs1, 0, 0);
    }

    private int encodeILhi(final ParsedInstruction instr) {
        if (!checkOperandCount(instr, 2))
            return 0;
        final var rd = expectRegister(instr, 0);
        final var imm = resolveImmediate(instr, 1);
        if (rd < 0)
            return 0;
        return encodeI(OperationCode.LHI.code(), 0, rd, imm);
    }

    private int encodeTrap(final ParsedInstruction instr) {
        if (!checkOperandCount(instr, 1))
            return 0;
        final var imm = resolveImmediate(instr, 0);
        return encodeI(OperationCode.TRAP.code(), 0, 0, imm);
    }

    private int encodeJFormat(final ParsedInstruction instr, final OperationCode opcode, final int addr) {
        if (!checkOperandCount(instr, 1))
            return 0;
        final var distance = resolveBranchTarget(instr, 0, addr);
        final var minDist = -(1 << 25);
        final var maxDist = (1 << 25) - 1;
        if (distance < minDist || distance > maxDist) {
            addError("Jump distance out of 26-bit signed range", instr);
        }
        return encodeJ(opcode.code(), distance);
    }

    private static int encodeR(final int rs1, final int rs2, final int rd, final int func) {
        return (OperationCode.SPECIAL.code() << 26) | (rs1 << 21) | (rs2 << 16) | (rd << 11) | func;
    }

    private static int encodeI(final int opcode, final int rs1, final int rd, final int imm16) {
        return (opcode << 26) | (rs1 << 21) | (rd << 16) | (imm16 & 0xFFFF);
    }

    private static int encodeJ(final int opcode, final int dist26) {
        return (opcode << 26) | (dist26 & 0x03FF_FFFF);
    }

    private int expectRegister(final ParsedInstruction instr, final int index) {
        final var op = instr.operands().get(index);
        if (op instanceof final RegisterOperand reg) {
            return reg.number();
        }
        addError("Expected register operand", instr);
        return -1;
    }

    private int resolveImmediate(final ParsedInstruction instr, final int index) {
        final var op = instr.operands().get(index);
        if (op instanceof final ImmediateOperand imm) {
            return imm.value();
        }
        if (op instanceof final LabelImmediateOperand labelImm) {
            return resolveLabel(instr, labelImm.name());
        }
        addError("Expected immediate or label operand", instr);
        return 0;
    }

    /// Resolves a memory operand at the given index. Returns `[offset, baseReg]` or
    /// `null` on error.
    private int[] resolveMemory(final ParsedInstruction instr, final int index) {
        final var op = instr.operands().get(index);
        if (op instanceof final MemoryOperand mem) {
            return new int[] { mem.offset(), mem.baseReg() };
        }
        if (op instanceof final LabelMemoryOperand labelMem) {
            final var addr = resolveLabel(instr, labelMem.offsetLabel());
            return new int[] { addr, labelMem.baseReg() };
        }
        addError("Expected memory operand", instr);
        return null;
    }

    private int resolveBranchTarget(final ParsedInstruction instr, final int index, final int instrAddr) {
        final var op = instr.operands().get(index);
        if (op instanceof final LabelOperand label) {
            final var targetAddr = resolveLabel(instr, label.name());
            return targetAddr - instrAddr;
        }
        addError("Expected label operand", instr);
        return 0;
    }

    private int resolveLabel(final ParsedInstruction instr, final String name) {
        final var addr = this.symbols.get(name);
        if (addr == null) {
            addError("Undefined label '" + name + "'", instr);
            return 0;
        }
        return addr.intValue();
    }

    private boolean checkOperandCount(final ParsedInstruction instr, final int expected) {
        if (instr.operands().size() != expected) {
            addError("Expected " + expected + " operand(s) for '" + instr.opcode()
                    + "', got " + instr.operands().size(), instr);
            return false;
        }
        return true;
    }

    private static void emitWord(final byte[] buf, final int offset, final int value) {
        buf[offset] = (byte) (value >>> 24);
        buf[offset + 1] = (byte) (value >>> 16);
        buf[offset + 2] = (byte) (value >>> 8);
        buf[offset + 3] = (byte) value;
    }

    private static void emitHalf(final byte[] buf, final int offset, final int value) {
        buf[offset] = (byte) (value >>> 8);
        buf[offset + 1] = (byte) value;
    }

    private static String stringValue(final ParsedDataDeclaration decl) {
        return (String) decl.values().getFirst();
    }

    private static int intValue(final ParsedDataDeclaration decl, final int index) {
        return ((Integer) decl.values().get(index)).intValue();
    }

    private boolean validateAlignment(final int n, final ParsedDataDeclaration decl) {
        if (n < 0 || n > 8) {
            addError("Alignment exponent must be between 0 and 8, got " + n, decl);
            return false;
        }
        return true;
    }

    private void addError(final String msg, final ParsedElement element) {
        addDiagnostic(Diagnostic.Severity.ERROR, msg, element);
    }

    private void addDiagnostic(final Diagnostic.Severity severity, final String msg, final ParsedElement element) {
        requireNonNull(severity);

        log.log(severity.toLogLevel(), msg);

        this.diagnostics.add(new Diagnostic(Diagnostic.Stage.COMPILING, severity, element.pos(), msg));
    }

    private boolean hasErrors() {
        return this.diagnostics.stream().anyMatch(d -> d.severity() == Diagnostic.Severity.ERROR);
    }
}
