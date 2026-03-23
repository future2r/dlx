package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/// DLX Lexer.
public final class Lexer {

    private static final Set<String> DIRECTIVES = Set.of(
            "data", "text",
            "word", "half", "byte", "float", "double",
            "ascii", "asciiz", "space", "align");

    private static final Set<String> INSTRUCTIONS = Set.of(
            "lb", "lh", "lw", "lbu", "lhu",
            "sb", "sh", "sw",
            "lhi",
            "add", "sub", "addu", "subu",
            "addi", "subi", "addui", "subui",
            "and", "or", "xor",
            "andi", "ori", "xori",
            "sll", "srl", "sra",
            "slli", "srli", "srai",
            "slt", "sle", "seq",
            "slti", "slei", "seqi",
            "sgt", "sge", "sne",
            "sgti", "sgei", "snei",
            "beqz", "bnez",
            "j", "jr", "jal", "jalr",
            "halt");

    private final LexerMode mode;
    private final List<Token> tokens = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    private String src; // current line text
    private int line; // 1-based line number
    private int pos; // current character position in src

    /// Creates a new Lexer with the given mode.
    /// 
    /// @param mode Determines which tokens are emitted and which are skipped.
    public Lexer(final LexerMode mode) {
        this.mode = requireNonNull(mode);
    }

    /// Tokenises one source line.
    /// 
    /// @param source     The line text.
    /// @param lineNumber 1-based line number (for error reporting).
    /// @return List of tokens found in the line. In [LexerMode#ASSEMBLER] mode, this
    ///         will exclude whitespace and comments.
    public List<Token> tokenizeLine(final String source, final int lineNumber) {
        this.src = requireNonNull(source);
        this.line = lineNumber;
        this.pos = 0;
        tokens.clear();
        errors.clear();

        scanLine();
        return List.copyOf(tokens);
    }

    /// Tokenizes an entire source file split into lines.
    /// 
    /// @param lines List of source lines.
    /// @return List of tokens found in the source. In [LexerMode#ASSEMBLER] mode,
    ///         this will exclude whitespace and comments.
    public List<Token> tokenize(final List<String> lines) {
        requireNonNull(lines);

        final var all = new ArrayList<Token>();
        for (int i = 0; i < lines.size(); i++)
            all.addAll(tokenizeLine(lines.get(i), i + 1));
        return List.copyOf(all);
    }

    /// Errors accumulated across all `tokenizeLine()` calls.
    /// 
    /// @return List of error messages.
    public List<String> errors() {
        return List.copyOf(errors);
    }

    private void scanLine() {
        while (pos < src.length()) {
            final int start = pos;
            final char c = src.charAt(pos);

            if (isWhitespace(c)) {
                scanWhitespace(start);
                continue;
            }
            if (c == ';') {
                scanComment(start);
                continue;
            }
            if (c == ',') {
                scanComma();
                continue;
            }
            if (c == '(') {
                scanLParen();
                continue;
            }
            if (c == ')') {
                scanRParen();
                continue;
            }
            if (c == '"') {
                scanString(start);
                continue;
            }
            if (c == '.') {
                scanDirective(start);
                continue;
            }
            if (isSign(c) && hasDigitAhead(pos + 1)) {
                scanNumber(start);
                continue;
            }
            if (Character.isDigit(c)) {
                scanNumber(start);
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                scanWord(start);
                continue;
            }

            // Unrecognised character
            addError("Unexpected character '" + c + "'", start, 1);
            emit(new UnknownToken(line, start, String.valueOf(c)));
            pos++;
        }

        // End of line marker — always emitted in both modes
        emit(new EOLToken(line, pos));
    }

    private void scanWhitespace(final int start) {
        while (pos < src.length() && isWhitespace(src.charAt(pos)))
            pos++;
        if (mode == LexerMode.HIGHLIGHTING)
            emit(new WhitespaceToken(line, start, src.substring(start, pos)));
        // ASSEMBLER mode: silently skip
    }

    private void scanComment(final int start) {
        // Consume from ';' to end of line
        pos = src.length();
        final String raw = src.substring(start);
        if (mode == LexerMode.HIGHLIGHTING)
            emit(new CommentToken(line, start, raw));
        // ASSEMBLER mode: silently skip
    }

    private void scanComma() {
        final int start = pos++;
        emit(new CommaToken(line, start));
    }

    private void scanLParen() {
        final int start = pos++;
        emit(new LeftParenToken(line, start));
    }

    private void scanRParen() {
        final int start = pos++;
        emit(new RightParenToken(line, start));
    }

    private void scanDirective(final int start) {
        pos++; // consume '.'
        while (pos < src.length() && isIdentChar(src.charAt(pos)))
            pos++;
        final String raw = src.substring(start, pos);
        final String name = raw.substring(1).toLowerCase(); // strip dot, normalise

        if (DIRECTIVES.contains(name)) {
            emit(new DirectiveToken(line, start, raw, name));
        } else {
            addError("Unknown directive '" + raw + "'", start, raw.length());
            emit(new UnknownToken(line, start, raw));
        }
    }

    private void scanNumber(final int start) {
        // Consume optional sign
        if (pos < src.length() && isSign(src.charAt(pos)))
            pos++;

        boolean hex = false;
        if (pos + 1 < src.length()
                && src.charAt(pos) == '0'
                && Character.toLowerCase(src.charAt(pos + 1)) == 'x') {
            hex = true;
            pos += 2;
            while (pos < src.length() && isHexDigit(src.charAt(pos)))
                pos++;
        } else {
            while (pos < src.length() && Character.isDigit(src.charAt(pos)))
                pos++;
        }

        final String raw = src.substring(start, pos);

        try {
            final int value = hex
                    ? Integer.parseInt(raw.substring(2), 16) // strip 0x
                    : Integer.parseInt(raw);
            emit(new IntLiteralToken(line, start, raw, value));
        } catch (final NumberFormatException e) {
            addError("Invalid number '" + raw + "'", start, raw.length());
            emit(new UnknownToken(line, start, raw));
        }
    }

    private void scanString(final int start) {
        pos++; // consume opening '"'
        final var value = new StringBuilder();
        boolean closed = false;

        while (pos < src.length()) {
            final char c = src.charAt(pos++);
            if (c == '"') {
                closed = true;
                break;
            }
            if (c == '\\') {
                if (pos >= src.length())
                    break;
                final char esc = src.charAt(pos++);
                value.append(switch (esc) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case '\\' -> '\\';
                    case '"' -> '"';
                    case '0' -> '\0';
                    default -> {
                        addError("Unknown escape '\\" + esc + "'", pos - 2, 2);
                        yield esc;
                    }
                });
            } else {
                value.append(c);
            }
        }

        final String raw = src.substring(start, pos);
        if (!closed)
            addError("Unterminated string literal", start, raw.length());

        emit(new StringLiteralToken(line, start, raw, value.toString()));
    }

    private void scanWord(final int start) {
        while (pos < src.length() && isIdentChar(src.charAt(pos)))
            pos++;

        final String raw = src.substring(start, pos);
        final String lower = raw.toLowerCase();
        final boolean isLabel = pos < src.length() && src.charAt(pos) == ':';

        if (isLabel) {
            pos++; // consume ':'
            final String rawWithColon = src.substring(start, pos);
            emit(new LabelDefinitionToken(line, start, rawWithColon, lower));
            return;
        }

        // Register? r0..r31
        if (lower.length() >= 2 && lower.charAt(0) == 'r'
                && isAllDigits(lower, 1)) {
            try {
                final int num = Integer.parseInt(lower.substring(1));
                if (num >= 0 && num <= 31) {
                    emit(new RegisterToken(line, start, raw, num));
                    return;
                }
            } catch (final NumberFormatException ignored) {
            }
        }

        // Instruction?
        if (INSTRUCTIONS.contains(lower)) {
            emit(new InstructionToken(line, start, raw, lower));
            return;
        }

        // Otherwise: label reference (branch target, data symbol)
        emit(new LabelReferenceToken(line, start, raw, lower));
    }

    private void emit(final Token t) {
        tokens.add(t);
    }

    private void addError(final String msg, final int col, final int length) {
        errors.add("Line " + line + ", col " + col
                + " (len " + length + "): " + msg);
    }

    private static boolean isWhitespace(final char c) {
        return c == ' ' || c == '\t';
    }

    private static boolean isSign(final char c) {
        return c == '-' || c == '+';
    }

    private static boolean isHexDigit(final char c) {
        return Character.isDigit(c)
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    private static boolean isIdentChar(final char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private static boolean isAllDigits(final String s, final int from) {
        if (from >= s.length())
            return false;
        for (int i = from; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i)))
                return false;
        return true;
    }

    /// Returns `true` if `src[pos]` looks like the start of a digit sequence.
    private boolean hasDigitAhead(final int p) {
        return p < src.length() && Character.isDigit(src.charAt(p));
    }
}
