package name.ulbricht.dlx.asm.lexer;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import name.ulbricht.dlx.asm.Diagnostic;
import name.ulbricht.dlx.util.TextPosition;

/// The lexer for the DLX assembler.
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
    private final List<Diagnostic> errors = new ArrayList<>();

    private String src; // current line text
    private int line; // 0-based line index
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
    /// @param lineNumber 0-based line index (used as [TextPosition#line]).
    /// @return List of tokens found in the line. In [LexerMode#ASSEMBLER] mode, this
    ///         will exclude whitespace and comments.
    public List<Token> tokenizeLine(final String source, final int lineNumber) {
        this.src = requireNonNull(source);
        this.line = lineNumber;
        this.pos = 0;
        this.tokens.clear();
        this.errors.clear();

        scanLine();
        return List.copyOf(this.tokens);
    }

    /// Tokenizes an entire source file split into lines.
    ///
    /// Errors from all lines are accumulated and returned in
    /// [TokenizedProgram#errors()].
    ///
    /// @param lines List of source lines.
    /// @return [TokenizedProgram] containing the flat token list and any errors. In
    ///         [LexerMode#ASSEMBLER] mode, whitespace and comments are excluded from
    ///         the token list.
    public TokenizedProgram tokenize(final List<String> lines) {
        requireNonNull(lines);

        final var allTokens = new ArrayList<Token>();
        final var allErrors = new ArrayList<Diagnostic>();
        for (int i = 0; i < lines.size(); i++) {
            allTokens.addAll(tokenizeLine(lines.get(i), i));
            allErrors.addAll(this.errors); // collect before next call clears them
        }
        return new TokenizedProgram(List.copyOf(allTokens), List.copyOf(allErrors));
    }

    private void scanLine() {
        while (this.pos < this.src.length()) {
            final int start = this.pos;
            final char c = this.src.charAt(this.pos);

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
            if (isSign(c) && hasDigitAhead(this.pos + 1)) {
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
            emit(new UnknownToken(new TextPosition(this.line, start), String.valueOf(c)));
            this.pos++;
        }

        // End of line marker — always emitted in both modes
        emit(new EOLToken(new TextPosition(this.line, this.pos)));
    }

    private void scanWhitespace(final int start) {
        while (this.pos < this.src.length() && isWhitespace(this.src.charAt(this.pos)))
            this.pos++;
        if (this.mode == LexerMode.HIGHLIGHTING)
            emit(new WhitespaceToken(new TextPosition(this.line, start), this.src.substring(start, this.pos)));
        // ASSEMBLER mode: silently skip
    }

    private void scanComment(final int start) {
        // Consume from ';' to end of line
        this.pos = this.src.length();
        final var raw = this.src.substring(start);
        if (this.mode == LexerMode.HIGHLIGHTING)
            emit(new CommentToken(new TextPosition(this.line, start), raw));
        // ASSEMBLER mode: silently skip
    }

    private void scanComma() {
        final int start = this.pos++;
        emit(new CommaToken(new TextPosition(this.line, start)));
    }

    private void scanLParen() {
        final int start = this.pos++;
        emit(new LeftParenToken(new TextPosition(this.line, start)));
    }

    private void scanRParen() {
        final int start = this.pos++;
        emit(new RightParenToken(new TextPosition(this.line, start)));
    }

    private void scanDirective(final int start) {
        this.pos++; // consume '.'
        while (this.pos < this.src.length() && isIdentChar(this.src.charAt(this.pos)))
            this.pos++;
        final var raw = this.src.substring(start, this.pos);
        final var name = raw.substring(1).toLowerCase(); // strip dot, normalise

        if (DIRECTIVES.contains(name)) {
            emit(new DirectiveToken(new TextPosition(this.line, start), raw, name));
        } else {
            addError("Unknown directive '" + raw + "'", start, raw.length());
            emit(new UnknownToken(new TextPosition(this.line, start), raw));
        }
    }

    private void scanNumber(final int start) {
        // Consume optional sign
        if (this.pos < this.src.length() && isSign(this.src.charAt(this.pos)))
            this.pos++;

        boolean hex = false;
        if (this.pos + 1 < this.src.length()
                && this.src.charAt(this.pos) == '0'
                && Character.toLowerCase(this.src.charAt(this.pos + 1)) == 'x') {
            hex = true;
            this.pos += 2;
            while (this.pos < this.src.length() && isHexDigit(this.src.charAt(this.pos)))
                this.pos++;
        } else {
            while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos)))
                this.pos++;
        }

        final var raw = this.src.substring(start, this.pos);

        try {
            final var value = hex
                    ? Integer.parseInt(raw.substring(2), 16) // strip 0x
                    : Integer.parseInt(raw);
            emit(new IntLiteralToken(new TextPosition(this.line, start), raw, value));
        } catch (final NumberFormatException _) {
            addError("Invalid number '" + raw + "'", start, raw.length());
            emit(new UnknownToken(new TextPosition(this.line, start), raw));
        }
    }

    private void scanString(final int start) {
        this.pos++; // consume opening '"'
        final var value = new StringBuilder();
        boolean closed = false;

        while (this.pos < this.src.length()) {
            final char c = this.src.charAt(this.pos++);
            if (c == '"') {
                closed = true;
                break;
            }
            if (c == '\\') {
                if (this.pos >= this.src.length())
                    break;
                final char esc = this.src.charAt(this.pos++);
                value.append(switch (esc) {
                    case 'n' -> '\n';
                    case 't' -> '\t';
                    case 'r' -> '\r';
                    case '\\' -> '\\';
                    case '"' -> '"';
                    case '0' -> '\0';
                    default -> {
                        addError("Unknown escape '\\" + esc + "'", this.pos - 2, 2);
                        yield esc;
                    }
                });
            } else {
                value.append(c);
            }
        }

        final var raw = this.src.substring(start, this.pos);
        if (!closed)
            addError("Unterminated string literal", start, raw.length());

        emit(new StringLiteralToken(new TextPosition(this.line, start), raw, value.toString()));
    }

    private void scanWord(final int start) {
        while (this.pos < this.src.length() && isIdentChar(this.src.charAt(this.pos)))
            this.pos++;

        final var raw = this.src.substring(start, this.pos);
        final var lower = raw.toLowerCase();
        final var isLabel = this.pos < this.src.length() && this.src.charAt(this.pos) == ':';

        if (isLabel) {
            this.pos++; // consume ':'
            final var rawWithColon = this.src.substring(start, this.pos);
            emit(new LabelDefinitionToken(new TextPosition(this.line, start), rawWithColon, lower));
            return;
        }

        // Register? r0..r31
        if (lower.length() >= 2 && lower.charAt(0) == 'r'
                && isAllDigits(lower, 1)) {
            try {
                final var num = Integer.parseInt(lower.substring(1));
                if (num >= 0 && num <= 31) {
                    emit(new RegisterToken(new TextPosition(this.line, start), raw, num));
                    return;
                }
            } catch (final NumberFormatException _) {
                // Shouldn't happen since we checked isAllDigits, but just in case
            }
        }

        // Instruction?
        if (INSTRUCTIONS.contains(lower)) {
            emit(new InstructionToken(new TextPosition(this.line, start), raw, lower));
            return;
        }

        // Otherwise: label reference (branch target, data symbol)
        emit(new LabelReferenceToken(new TextPosition(this.line, start), raw, lower));
    }

    private void emit(final Token t) {
        this.tokens.add(t);
    }

    private void addError(final String msg, final int col, final int length) {
        this.errors.add(new Diagnostic(Diagnostic.Stage.LEXING, new TextPosition(this.line, col, length), msg));
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
        return p < this.src.length() && Character.isDigit(this.src.charAt(p));
    }
}
