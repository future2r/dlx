package name.ulbricht.dlx.ui.scene.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings("static-method")
final class IndexTest {

    @Test
    @DisplayName("Initial value is zero")
    void initialValue() {
        final var index = new Index();
        assertEquals(0, index.getCurr());
    }

    @Test
    @DisplayName("Increment increases value by one")
    void increment() {
        final var index = new Index();

        assertEquals(1, index.getNext());
        assertEquals(1, index.getCurr());
    }
}
