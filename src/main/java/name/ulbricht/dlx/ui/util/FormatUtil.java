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

    /// Formats the given value as a binary string with the format `XXXXXXXX
    /// XXXXXXXX XXXXXXXX XXXXXXXX`.
    ///
    /// @param value the value to format
    /// @return the formatted binary string
    public static String binary(final int value) {
        return "%s %s %s %s".formatted( //
                "%8s".formatted(Integer.toBinaryString((value >> 24) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString((value >> 16) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString((value >> 8) & 0xFF)).replace(' ', '0'), //
                "%8s".formatted(Integer.toBinaryString(value & 0xFF)).replace(' ', '0'));
    }

    /// Private constructor to prevent instantiation.
    private FormatUtil() {
    }
}
