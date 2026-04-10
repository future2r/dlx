package name.ulbricht.dlx.ui.event;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.UUID;

import javafx.event.Event;
import javafx.event.EventType;
import name.ulbricht.dlx.util.TextPosition;

/// An event that carries a `TextPosition` as its payload. This can be used to
/// signal that the text position in the editor should be changed, for example
/// when an item in the outline view is double-clicked.
public final class TextPositionEvent extends Event {

    @Serial
    private static final long serialVersionUID = 1L;

    /// The event type for `TextPositionEvent`. This can be used to register event
    /// handlers for this type of event.
    public static final EventType<TextPositionEvent> TEXT_POSITION_EVENT = new EventType<>(Event.ANY,
            "TEXT_POSITION_EVENT");

    private final transient TextPosition textPosition;
    private final transient UUID sourceId;

    /// Creates a new `TextPositionEvent` with the given text position.
    ///
    /// @param textPosition the text position to be carried by this event
    public TextPositionEvent(final TextPosition textPosition) {
        this(textPosition, null);
    }

    /// Creates a new `TextPositionEvent` with the given text position and source
    /// identifier.
    ///
    /// @param textPosition the text position to be carried by this event
    /// @param sourceId     the identifier of the source origin, or `null` if the
    ///                     active editor should be used
    public TextPositionEvent(final TextPosition textPosition, final UUID sourceId) {
        super(TEXT_POSITION_EVENT);
        this.textPosition = requireNonNull(textPosition);
        this.sourceId = sourceId;
    }

    /// {@return the text position carried by this event}
    public TextPosition getTextPosition() {
        return this.textPosition;
    }

    /// {@return the identifier of the source origin, or `null` if the active editor
    /// should be used}
    public UUID getSourceId() {
        return this.sourceId;
    }
}
