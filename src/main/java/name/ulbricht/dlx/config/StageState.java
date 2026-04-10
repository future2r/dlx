package name.ulbricht.dlx.config;

/// Represents the saved state of a stage.
///
/// @param maximized whether the stage was maximized
/// @param x         the x-coordinate of the stage, or [StageState#UNDEFINED] if
///                  the stage was maximized
/// @param y         the y-coordinate of the stage, or [StageState#UNDEFINED] if
///                  the stage was maximized
/// @param width     the width of the stage, or [StageState#UNDEFINED] if the
///                  stage was maximized
/// @param height    the height of the stage, or [StageState#UNDEFINED] if the
///                  stage was maximized
public record StageState(boolean maximized, int x, int y, int width, int height) {

    /// Creates a stage state representing a maximized stage.
    /// 
    /// @return a stage state representing a maximized stage
    public static StageState ofMaximized() {
        return new StageState(true, UNDEFINED, UNDEFINED, UNDEFINED, UNDEFINED);
    }

    /// Creates a stage state representing a normal (not maximized) stage with the
    /// given position and size.
    /// 
    /// @param x      the x-coordinate of the stage
    /// @param y      the y-coordinate of the stage
    /// @param width  the width of the stage
    /// @param height the height of the stage
    /// @return a stage state representing a normal stage with the given position
    ///         and size
    public static StageState ofNormal(final int x, final int y, final int width, final int height) {
        return new StageState(false, x, y, width, height);
    }

    /// A constant representing an undefined coordinate or dimension, used when the
    /// stage is maximized and the position and size are not applicable.
    public static final int UNDEFINED = Integer.MIN_VALUE;
}
