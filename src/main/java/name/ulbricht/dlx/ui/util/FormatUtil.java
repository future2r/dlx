package name.ulbricht.dlx.ui.util;

/// Utility class for formatting values in the UI.
public final class FormatUtil {

    /// Formats the given value as a decimal string.
    ///
    /// @param value the value to format
    /// @return the formatted decimal string
    public static String decimal(final int value) {
        return Integer.toString(value);
    }

    /// Formats the given value as a decimal string.
    ///
    /// @param value the value to format
    /// @return the formatted decimal string
    public static String decimal(final long value) {
        return Long.toString(value);
    }

    /// Formats the given value as a hexadecimal string with the format `XX XX XX
    /// XX`.
    /// 
    /// @param value the value to format
    /// @return the formatted hexadecimal string
    public static String hexadecimal(final int value) {
        return "%02X %02X %02X %02X".formatted(
                Integer.valueOf((value >> 24) & 0xFF),
                Integer.valueOf((value >> 16) & 0xFF),
                Integer.valueOf((value >> 8) & 0xFF),
                Integer.valueOf(value & 0xFF));
    }

    /// Formats the given byte array as a hexadecimal string.
    /// 
    /// @param value the byte array to format
    /// @return the formatted hexadecimal string
    public static String hexadecimalBytes(final byte[] value) {
        final var sb = new StringBuilder();
        for (int i = 0; i < value.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append("%02X".formatted(Integer.valueOf(value[i] & 0xFF)));
        }
        return sb.toString();
    }

    /// Formats the given value as a binary string with the format `XXXXXXXX
    /// XXXXXXXX XXXXXXXX XXXXXXXX`.
    ///
    /// @param value the value to format
    /// @return the formatted binary string
    public static String binaryWord(final int value) {
        return "%s %s %s %s".formatted( //
                "%8s".formatted(Integer.toBinaryString((value >> 24) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString((value >> 16) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString((value >> 8) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
    }

    /// Formats a byte count as a human-readable string (e.g., "1 KB" or "512 Bytes").
    ///
    /// @param bytes the byte count to format
    /// @return the formatted string
    public static String formatBytes(final int bytes) {
        if (bytes >= 1024 && bytes % 1024 == 0) {
            return (bytes / 1024) + " KB";
        }
        return bytes + " Bytes";
    }

    /// Private constructor to prevent instantiation.
    private FormatUtil() {
    }
}
