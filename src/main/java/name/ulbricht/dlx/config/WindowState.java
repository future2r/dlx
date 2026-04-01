package name.ulbricht.dlx.config;

/// Represents the saved state of a window — either a position and size, or the
/// maximized flag.
///
/// @param maximized whether the window was maximized
/// @param x         the x-coordinate of the window
/// @param y         the y-coordinate of the window
/// @param width     the width of the window
/// @param height    the height of the window
public record WindowState(boolean maximized, double x, double y, double width, double height) {
}
