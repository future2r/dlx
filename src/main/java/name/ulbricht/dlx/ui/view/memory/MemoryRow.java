package name.ulbricht.dlx.ui.view.memory;

import name.ulbricht.dlx.simulator.Access;

/// Represents a single row in the hex viewer table.
///
/// @param baseAddress the starting byte address of this row (always a multiple
///                    of 16)
/// @param shadow      the shadow byte array maintained by the view model
/// @param accessState the per-byte access state array maintained by the
///                    view model
public record MemoryRow(int baseAddress, byte[] shadow, Access[] accessState) {

    /// The number of bytes displayed per row in the hex viewer.
    public static final int BYTES_PER_ROW = 16;

    /// Returns the unsigned byte value at the given column index (0–15).
    ///
    /// @param col the column index within this row (0–15)
    /// @return the unsigned byte value (0–255)
    public int getByteValue(final int col) {
        return this.shadow[this.baseAddress + col] & 0xFF;
    }

    /// Returns the last access type for the byte at the given column index.
    ///
    /// @param col the column index within this row (0–15)
    /// @return the access type, or `null` if the byte has not been accessed
    public Access getByteAccess(final int col) {
        return this.accessState[this.baseAddress + col];
    }

    /// Builds the 16-character ASCII representation of this row.
    ///
    /// @return a 16-character string with printable characters or dots
    public String asciiText() {
        final var sb = new StringBuilder(BYTES_PER_ROW);
        for (var col = 0; col < BYTES_PER_ROW; col++) {
            sb.append(getAsciiChar(col));
        }
        return sb.toString();
    }

    private char getAsciiChar(final int col) {
        final var b = this.shadow[this.baseAddress + col];
        return (b >= 0x20 && b <= 0x7E) ? (char) b : '.';
    }
}
