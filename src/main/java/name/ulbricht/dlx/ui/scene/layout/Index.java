package name.ulbricht.dlx.ui.scene.layout;

import javafx.beans.NamedArg;

/// Container class that holds an index. This can be used as an utility to
/// increment an index in FXML files, e.g. for the row or column index of
/// a GridPane.
public final class Index {

    private final int max;
    private int value;

    /// Creates a new instance with maximum value of [Integer#MAX_VALUE].
    public Index() {
        this(Integer.MAX_VALUE);
    }

    /// Creates a new instance.
    ///
    /// @param max the maximum value for the index, must not be negative
    public Index(@NamedArg("max") final int max) {
        if (max < 0)
            throw new IllegalArgumentException("max must not be negative");
        this.max = max;
    }

    /// {@return always `0` and resets the current index to it}
    public int getFirst() {
        this.value = 0;
        return this.value;
    }

    /// {@return always `max + 1`, but never larger than [Integer#MAX_VALUE]}
    public int getCount() {
        return this.max < Integer.MAX_VALUE ? this.max + 1 : Integer.MAX_VALUE;
    }

    /// {@return the current index} The initial value is `0`.
    public int getCurr() {
        return this.value;
    }

    /// {@return increments the index by `1` and returns the new index} If the index
    /// reaches the maximum, it is no longer incremented.
    public int getNext() {
        return this.value < this.max ? ++this.value : this.value;
    }
}
