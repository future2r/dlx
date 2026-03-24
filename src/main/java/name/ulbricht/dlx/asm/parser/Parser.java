package name.ulbricht.dlx.asm.parser;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.asm.lexer.CommaToken;
import name.ulbricht.dlx.asm.lexer.DirectiveToken;
import name.ulbricht.dlx.asm.lexer.EOLToken;
import name.ulbricht.dlx.asm.lexer.InstructionToken;
import name.ulbricht.dlx.asm.lexer.IntLiteralToken;
import name.ulbricht.dlx.asm.lexer.LabelDefinitionToken;
import name.ulbricht.dlx.asm.lexer.LabelReferenceToken;
import name.ulbricht.dlx.asm.lexer.LeftParenToken;
import name.ulbricht.dlx.asm.lexer.RegisterToken;
import name.ulbricht.dlx.asm.lexer.RightParenToken;
import name.ulbricht.dlx.asm.lexer.StringLiteralToken;
import name.ulbricht.dlx.asm.lexer.Token;
import name.ulbricht.dlx.asm.lexer.TokenizedProgram;
import name.ulbricht.dlx.util.TextPosition;

/// DLX assembler parser.
///
/// Consumes a flat token list produced by [name.ulbricht.dlx.asm.lexer.Lexer] in
/// [name.ulbricht.dlx.asm.lexer.LexerMode#ASSEMBLER] mode and builds a
/// [ParsedProgram] that groups all data declarations and all instructions into
/// separate merged sections.
///
/// Multiple `.data` and `.text` segment switches are handled transparently;
/// their contents are merged into the respective section lists. Content that
/// appears before any segment directive is treated as data.
///
/// Error recovery is line-oriented: when a parse error occurs on a line the
/// remainder of that line is discarded and parsing continues from the next line.
public final class Parser {

    /// Creates a new Parser.
    public Parser() {
    }

    /// R-format: Rd, Rs1, Rs2
    private static final Set<String> FMT_R = Set.of(
            "add", "sub", "addu", "subu",
            "and", "or", "xor",
            "sll", "srl", "sra",
            "slt", "sle", "seq", "sgt", "sge", "sne");

    /// I-format: Rd, Rs1, Imm
    private static final Set<String> FMT_RI = Set.of(
            "addi", "subi", "addui", "subui",
            "andi", "ori", "xori",
            "slli", "srli", "srai",
            "slti", "slei", "seqi", "sgti", "sgei", "snei");

    /// Load: Rd, mem(Rs)
    private static final Set<String> FMT_LOAD = Set.of("lb", "lh", "lw", "lbu", "lhu");

    /// Store: mem(Rs), Rsrc
    private static final Set<String> FMT_STORE = Set.of("sb", "sh", "sw");

    private List<ParsedDataDeclaration> data;
    private List<ParsedInstruction> code;
    private List<Diagnostic> errors;
    private String pendingLabel;

    /// Parses the token stream into a [ParsedProgram].
    ///
    /// The [TokenizedProgram] must come from [name.ulbricht.dlx.asm.lexer.Lexer] in
    /// [name.ulbricht.dlx.asm.lexer.LexerMode#ASSEMBLER] mode (whitespace and
    /// comments already stripped). [EOLToken]s act as line boundaries.
    ///
    /// Lexer errors carried in `tokenized` are prepended to the parser's own errors;
    /// the combined list is returned in [ParsedProgram#errors()].
    ///
    /// @param tokenized the lexer output to parse
    /// @return the parsed program with merged data and code sections and all errors
    public ParsedProgram parse(final TokenizedProgram tokenized) {
        requireNonNull(tokenized);

        this.data = new ArrayList<>();
        this.code = new ArrayList<>();
        this.errors = new ArrayList<>(tokenized.errors()); // start with lexer errors
        this.pendingLabel = null;

        for (final var line : splitLines(tokenized.tokens())) {
            if (!line.isEmpty()) {
                parseLine(line);
            }
        }

        return new ParsedProgram(List.copyOf(this.data), List.copyOf(this.code), List.copyOf(this.errors));
    }

    private static List<List<Token>> splitLines(final List<Token> tokens) {
        final var lines = new ArrayList<List<Token>>();
        var current = new ArrayList<Token>();
        for (final var token : tokens) {
            if (token instanceof EOLToken) {
                lines.add(current);
                current = new ArrayList<>();
            } else {
                current.add(token);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current);
        }
        return lines;
    }

    private void parseLine(final List<Token> line) {
        var cursor = 0;
        String label = null;

        // Optional leading label definition
        if (line.get(cursor) instanceof final LabelDefinitionToken ldt) {
            label = ldt.name();
            cursor++;
        }

        // Label-only line: defer the label to the next real line
        if (cursor == line.size()) {
            this.pendingLabel = label;
            return;
        }

        // Merge with any pending label from a prior label-only line
        final var effectiveLabel = label != null ? label : this.pendingLabel;
        this.pendingLabel = null;

        final var token = line.get(cursor);
        final var rest = line.subList(cursor + 1, line.size());

        if (token instanceof final DirectiveToken dt) {
            parseDirective(effectiveLabel, dt, rest);
        } else if (token instanceof final InstructionToken it) {
            parseInstruction(effectiveLabel, it, rest);
        } else {
            addError("Unexpected token", token, token.raw().length());
        }
    }

    private void parseDirective(final String label, final DirectiveToken dt, final List<Token> rest) {
        switch (dt.name()) {
            case "data", "text" -> {
                // Segment switches: no operands expected
                if (!rest.isEmpty()) {
                    addError("Unexpected tokens after ." + dt.name(), rest.get(0), 0);
                }
            }
            case "word", "half", "byte" -> {
                final var values = parseIntList(rest, dt);
                if (values != null) {
                    this.data.add(new ParsedDataDeclaration(dt.pos(), label, dt.name(), values));
                }
            }
            case "ascii" -> {
                final var str = parseSingleString(rest, dt);
                if (str != null) {
                    this.data.add(new ParsedDataDeclaration(dt.pos(), label, "ascii", List.of(str)));
                }
            }
            case "asciiz" -> {
                final var str = parseSingleString(rest, dt);
                if (str != null) {
                    this.data.add(new ParsedDataDeclaration(dt.pos(), label, "asciiz", List.of(str)));
                }
            }
            case "space", "align" -> {
                final var value = parseSingleInt(rest, dt);
                if (value != null) {
                    this.data.add(new ParsedDataDeclaration(dt.pos(), label, dt.name(), List.of(value)));
                }
            }
            default -> addError("Directive not supported: ." + dt.name(), dt, dt.raw().length());
        }
    }

    private void parseInstruction(final String label, final InstructionToken it, final List<Token> rest) {
        final String op = it.name();
        final List<Operand> operands;

        if (FMT_R.contains(op)) {
            operands = parseRdRs1Rs2(rest, it);
        } else if (FMT_RI.contains(op)) {
            operands = parseRdRs1Imm(rest, it);
        } else if (FMT_LOAD.contains(op)) {
            operands = parseRdMem(rest, it);
        } else if (FMT_STORE.contains(op)) {
            operands = parseRsrcMem(rest, it);
        } else {
            operands = switch (op) {
                case "lhi" -> parseRdImm(rest, it);
                case "beqz", "bnez" -> parseRsLabel(rest, it);
                case "j", "jal" -> parseLabel(rest, it);
                case "jr", "jalr" -> parseRs(rest, it);
                case "halt" -> parseNone(rest);
                default -> {
                    addError("Unknown instruction: " + op, it, it.raw().length());
                    yield null;
                }
            };
        }

        if (operands != null) {
            this.code.add(new ParsedInstruction(it.pos(), label, op, operands));
        }
    }

    /// Rd, Rs1, Rs2
    private List<Operand> parseRdRs1Rs2(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rd = expectRegister(cursor, ctx);
        if (rd == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var rs1 = expectRegister(cursor, ctx);
        if (rs1 == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var rs2 = expectRegister(cursor, ctx);
        if (rs2 == null)
            return null;
        expectEnd(cursor);
        return List.of(rd, rs1, rs2);
    }

    /// Rd, Rs1, Imm
    private List<Operand> parseRdRs1Imm(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rd = expectRegister(cursor, ctx);
        if (rd == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var rs1 = expectRegister(cursor, ctx);
        if (rs1 == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var imm = expectImmediate(cursor, ctx);
        if (imm == null)
            return null;
        expectEnd(cursor);
        return List.of(rd, rs1, imm);
    }

    /// Rd, mem(Rs)
    private List<Operand> parseRdMem(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rd = expectRegister(cursor, ctx);
        if (rd == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var mem = expectMemory(cursor, ctx);
        if (mem == null)
            return null;
        expectEnd(cursor);
        return List.of(rd, mem);
    }

    /// mem(Rs), Rsrc
    private List<Operand> parseRsrcMem(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var mem = expectMemory(cursor, ctx);
        if (mem == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var rsrc = expectRegister(cursor, ctx);
        if (rsrc == null)
            return null;
        expectEnd(cursor);
        return List.of(mem, rsrc);
    }

    /// Rd, Imm
    private List<Operand> parseRdImm(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rd = expectRegister(cursor, ctx);
        if (rd == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var imm = expectImmediate(cursor, ctx);
        if (imm == null)
            return null;
        expectEnd(cursor);
        return List.of(rd, imm);
    }

    /// Rs, Label
    private List<Operand> parseRsLabel(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rs = expectRegister(cursor, ctx);
        if (rs == null)
            return null;
        if (!expectComma(cursor, ctx))
            return null;
        final var lbl = expectLabelRef(cursor, ctx);
        if (lbl == null)
            return null;
        expectEnd(cursor);
        return List.of(rs, lbl);
    }

    /// Label
    private List<Operand> parseLabel(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var lbl = expectLabelRef(cursor, ctx);
        if (lbl == null)
            return null;
        expectEnd(cursor);
        return List.of(lbl);
    }

    /// Rs
    private List<Operand> parseRs(final List<Token> tokens, final Token ctx) {
        final var cursor = new Cursor(tokens);
        final var rs = expectRegister(cursor, ctx);
        if (rs == null)
            return null;
        expectEnd(cursor);
        return List.of(rs);
    }

    /// (none)
    private List<Operand> parseNone(final List<Token> tokens) {
        expectEnd(new Cursor(tokens));
        return List.of();
    }

    private RegisterOperand expectRegister(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected register", ctx, 0);
            return null;
        }
        final var token = cursor.peek();
        if (token instanceof final RegisterToken rt) {
            cursor.advance();
            return new RegisterOperand(rt.number());
        }
        addError("Expected register, got: " + token.raw(), token, token.raw().length());
        return null;
    }

    private ImmediateOperand expectImmediate(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected immediate value", ctx, 0);
            return null;
        }
        final var token = cursor.peek();
        if (token instanceof final IntLiteralToken il) {
            cursor.advance();
            return new ImmediateOperand(il.value());
        }
        addError("Expected immediate value, got: " + token.raw(), token, token.raw().length());
        return null;
    }

    private LabelOperand expectLabelRef(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected label", ctx, 0);
            return null;
        }
        final var token = cursor.peek();
        if (token instanceof final LabelReferenceToken lrt) {
            cursor.advance();
            return new LabelOperand(lrt.name());
        }
        addError("Expected label, got: " + token.raw(), token, token.raw().length());
        return null;
    }

    private Operand expectMemory(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected memory operand", ctx, 0);
            return null;
        }
        final var offsetToken = cursor.peek();

        if (offsetToken instanceof final IntLiteralToken ilt) {
            cursor.advance();
            return parseMemoryWithBase(cursor, ilt.value(), null, ctx);
        } else if (offsetToken instanceof final LabelReferenceToken lrt) {
            cursor.advance();
            return parseMemoryWithBase(cursor, 0, lrt.name(), ctx);
        } else {
            addError("Expected memory offset (integer or label), got: " + offsetToken.raw(),
                    offsetToken, offsetToken.raw().length());
            return null;
        }
    }

    private Operand parseMemoryWithBase(
            final Cursor cursor,
            final int intOffset,
            final String labelOffset,
            final Token ctx) {

        if (!cursor.hasNext() || !(cursor.peek() instanceof LeftParenToken)) {
            addError("Expected '(' after memory offset", ctx, 0);
            return null;
        }
        cursor.advance(); // consume '('

        final var baseToken = cursor.hasNext() ? cursor.peek() : null;
        if (!(baseToken instanceof RegisterToken)) {
            addError("Expected base register inside '(...)'", ctx, 0);
            return null;
        }
        final var baseReg = ((RegisterToken) baseToken).number();
        cursor.advance(); // consume register

        if (!cursor.hasNext() || !(cursor.peek() instanceof RightParenToken)) {
            addError("Expected ')' after base register", ctx, 0);
            return null;
        }
        cursor.advance(); // consume ')'

        return labelOffset != null
                ? new LabelMemoryOperand(labelOffset, baseReg)
                : new MemoryOperand(intOffset, baseReg);
    }

    private boolean expectComma(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected ','", ctx, 0);
            return false;
        }
        final var token = cursor.peek();
        if (token instanceof CommaToken) {
            cursor.advance();
            return true;
        }
        addError("Expected ',', got: " + token.raw(), token, token.raw().length());
        return false;
    }

    private void expectEnd(final Cursor cursor) {
        if (cursor.hasNext()) {
            final var token = cursor.peek();
            addError("Unexpected token: " + token.raw(), token, token.raw().length());
        }
    }

    private List<Object> parseIntList(final List<Token> tokens, final Token ctx) {
        if (tokens.isEmpty()) {
            addError("Expected integer value", ctx, 0);
            return null;
        }
        final List<Object> values = new ArrayList<>();
        final var cursor = new Cursor(tokens);

        final var first = expectIntLiteral(cursor, ctx);
        if (first == null)
            return null;
        values.add(first);

        while (cursor.hasNext()) {
            if (!(cursor.peek() instanceof CommaToken)) {
                addError("Expected ',' or end of line, got: " + cursor.peek().raw(),
                        cursor.peek(), cursor.peek().raw().length());
                return null;
            }
            cursor.advance(); // consume comma
            final var next = expectIntLiteral(cursor, ctx);
            if (next == null)
                return null;
            values.add(next);
        }
        return List.copyOf(values);
    }

    private Integer parseSingleInt(final List<Token> tokens, final Token ctx) {
        if (tokens.isEmpty()) {
            addError("Expected integer value", ctx, 0);
            return null;
        }
        final var cursor = new Cursor(tokens);
        final var value = expectIntLiteral(cursor, ctx);
        if (value == null)
            return null;
        expectEnd(cursor);
        return value;
    }

    private String parseSingleString(final List<Token> tokens, final Token ctx) {
        if (tokens.isEmpty()) {
            addError("Expected string literal", ctx, 0);
            return null;
        }
        final var cursor = new Cursor(tokens);
        final var token = cursor.peek();
        if (!(token instanceof final StringLiteralToken slt)) {
            addError("Expected string literal, got: " + token.raw(), token, token.raw().length());
            return null;
        }
        cursor.advance();
        expectEnd(cursor);
        return slt.value();
    }

    private Integer expectIntLiteral(final Cursor cursor, final Token ctx) {
        if (!cursor.hasNext()) {
            addError("Expected integer value", ctx, 0);
            return null;
        }
        final var token = cursor.peek();
        if (token instanceof final IntLiteralToken ilt) {
            cursor.advance();
            return Integer.valueOf(ilt.value());
        }
        addError("Expected integer value, got: " + token.raw(), token, token.raw().length());
        return null;
    }

    private void addError(final String msg, final Token token, final int len) {
        this.errors.add(new Diagnostic(Diagnostic.Stage.PARSING,
                new TextPosition(token.pos().line(), token.pos().column(), len), msg));
    }

    private static final class Cursor {
        private final List<Token> tokens;
        private int pos;

        Cursor(final List<Token> tokens) {
            this.tokens = tokens;
            this.pos = 0;
        }

        boolean hasNext() {
            return this.pos < this.tokens.size();
        }

        Token peek() {
            return this.tokens.get(this.pos);
        }

        void advance() {
            this.pos++;
        }
    }
}
