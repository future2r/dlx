package name.ulbricht.dlx.util;

/// A position in the source text.
///
/// Both `line` and `col` are *0-based*: `line` is a direct index into a
/// `List<String>` of source lines; `col` is a direct character offset within
/// that line string.
///
/// Use [#displayLine()] and [#displayCol()] when showing the position to the
/// user (e.g. in error messages) — those return 1-based values.
///
/// @param line 0-based source line index
/// @param col  0-based character offset within the line
public record TextPosition(int line, int col) {

    /// Validates that line and col are non-negative.
    public TextPosition {
        if (line < 0)
            throw new IllegalArgumentException("line must be >= 0, got: " + line);
        if (col < 0)
            throw new IllegalArgumentException("col must be >= 0, got: " + col);
    }

    /// {@return 1-based line number for error messages and user-visible output}
    public int displayLine() {
        return this.line + 1;
    }

    /// {@return 1-based column number for error messages and user-visible output}
    public int displayCol() {
        return this.col + 1;
    }

    /// Returns a compact display string such as `"3:7"` (1-based line and column).
    @Override
    public String toString() {
        return displayLine() + ":" + displayCol();
    }
}
