package name.ulbricht.dlx.ui.stage;

import javafx.geometry.Rectangle2D;

/// Represents the saved state of a window, including bounds and maximized state.
///
/// @param bounds    the position and size of the window
/// @param maximized whether the window was maximized
public record WindowState(Rectangle2D bounds, boolean maximized) {
}
