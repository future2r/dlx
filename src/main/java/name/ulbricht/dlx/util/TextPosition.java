package name.ulbricht.dlx.util;

/// A position in the source text.
///
/// Both `line` and `col` are *0-based*: `line` is a direct index into a
/// `List<String>` of source lines; `col` is a direct character offset within
/// that line string.
///
/// Use [#displayLine()] and [#displayColumn()] when showing the position to the
/// user (e.g. in error messages) — those return 1-based values.
///
/// @param line   0-based source line index
/// @param column 0-based character offset within the line
/// @param length 0-based length of the text span (0 = point position)
public record TextPosition(int line, int column, int length) {

    /// Validates that line and column are non-negative.
    /// 
    /// @param line   0-based source line index
    /// @param column 0-based character offset within the line
    public TextPosition(final int line, final int column) {
        this(line, column, 0);
    }

    /// Validates that line and column are non-negative.
    public TextPosition {
        if (line < 0)
            throw new IllegalArgumentException("line must be >= 0, got: " + line);
        if (column < 0)
            throw new IllegalArgumentException("column must be >= 0, got: " + column);
        if (length < 0)
            throw new IllegalArgumentException("length must be >= 0, got: " + length);
    }

    /// {@return 1-based line number for error messages and user-visible output}
    public int displayLine() {
        return this.line + 1;
    }

    /// {@return 1-based column number for error messages and user-visible output}
    public int displayColumn() {
        return this.column + 1;
    }
}
