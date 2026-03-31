package name.ulbricht.dlx.ui.stage;

import javafx.geometry.Rectangle2D;

/// Represents the saved state of a window — either a position and size, or the
/// maximized flag.
///
/// @param bounds    the position and size of the window, or `null` if maximized
/// @param maximized whether the window was maximized
public record WindowState(Rectangle2D bounds, boolean maximized) {
}
