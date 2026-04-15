package name.ulbricht.dlx.ui.view.reference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.BitSet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RegisterConvention")
final class RegisterConventionTest {

    @Nested
    @DisplayName("completeness")
    final class Completeness {

        @Test
        @DisplayName("covers all 32 registers without gaps or overlaps")
        void coversAllRegisters() {
            final var covered = new BitSet(32);
            for (final var convention : RegisterConvention.values()) {
                for (var i = convention.startIndex(); i <= convention.endIndex(); i++) {
                    assertTrue(i >= 0 && i < 32,
                            "Register index " + i + " out of range in " + convention);
                    assertTrue(!covered.get(i),
                            "Register R" + i + " covered by multiple conventions");
                    covered.set(i);
                }
            }
            assertEquals(32, covered.cardinality(), "Not all 32 registers are covered");
        }
    }

    @Nested
    @DisplayName("displayName")
    final class DisplayNameTests {

        @Test
        @DisplayName("single register shows Rn")
        void singleRegister() {
            assertEquals("R0", RegisterConvention.R0.displayName());
            assertEquals("R31", RegisterConvention.R31.displayName());
        }

        @Test
        @DisplayName("register range shows Rn\u2013Rm")
        void registerRange() {
            assertEquals("R2\u2013R3", RegisterConvention.R2_R3.displayName());
            assertEquals("R4\u2013R7", RegisterConvention.R4_R7.displayName());
        }
    }
}
