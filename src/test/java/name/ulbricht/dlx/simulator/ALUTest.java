package name.ulbricht.dlx.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ALU")
final class ALUTest {

    private ALU alu;

    @BeforeEach
    void setUp() {
        this.alu = new ALU();
    }

    private ALU.Result exec(final ALU.Operation op, final int a, final int b) {
        return this.alu.execute(op, a, b);
    }

    @Test
    void bitwiseAndShiftOpsNeverSetOverflow() {
        for (final var op : new ALU.Operation[] {
                ALU.Operation.AND, ALU.Operation.OR, ALU.Operation.XOR,
                ALU.Operation.SLL, ALU.Operation.SRL, ALU.Operation.SRA,
                ALU.Operation.SEQ, ALU.Operation.SNE,
                ALU.Operation.SLT, ALU.Operation.SGT,
                ALU.Operation.SLE, ALU.Operation.SGE,
                ALU.Operation.PASS_B
        }) {
            assertFalse(exec(op, Integer.MAX_VALUE, Integer.MAX_VALUE).overflow(),
                    op + " must never set overflow");
        }
    }

    @Nested
    @DisplayName("Arithmetic operations")
    class Arithmetic {

        @Test
        void addTwoPositives() {
            final var r = exec(ALU.Operation.ADD, 3, 4);
            assertEquals(7, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void addPositiveAndNegative() {
            final var r = exec(ALU.Operation.ADD, 10, -3);
            assertEquals(7, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void addPositiveOverflow() {
            // MAX_VALUE + 1 wraps to MIN_VALUE
            final var r = exec(ALU.Operation.ADD, Integer.MAX_VALUE, 1);
            assertEquals(Integer.MIN_VALUE, r.value());
            assertTrue(r.overflow());
        }

        @Test
        void addNegativeOverflow() {
            // MIN_VALUE + (-1) wraps to MAX_VALUE
            final var r = exec(ALU.Operation.ADD, Integer.MIN_VALUE, -1);
            assertEquals(Integer.MAX_VALUE, r.value());
            assertTrue(r.overflow());
        }

        @Test
        void addNoOverflowWhenSignsDiffer() {
            final var r = exec(ALU.Operation.ADD, Integer.MAX_VALUE, -1);
            assertFalse(r.overflow());
        }

        @Test
        void adduSameBitPatternAsAdd() {
            final var r = exec(ALU.Operation.ADDU, Integer.MAX_VALUE, 1);
            assertEquals(Integer.MIN_VALUE, r.value()); // same wrap
            assertFalse(r.overflow()); // never sets flag
        }

        @Test
        void adduNeverSetsOverflow() {
            final var r = exec(ALU.Operation.ADDU, Integer.MAX_VALUE, Integer.MAX_VALUE);
            assertFalse(r.overflow());
        }

        @Test
        void subTwoPositives() {
            final var r = exec(ALU.Operation.SUB, 10, 3);
            assertEquals(7, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void subProducesNegativeResult() {
            final var r = exec(ALU.Operation.SUB, 3, 10);
            assertEquals(-7, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void subOverflowMinValueMinusPositive() {
            // MIN_VALUE - 1 wraps to MAX_VALUE
            final var r = exec(ALU.Operation.SUB, Integer.MIN_VALUE, 1);
            assertEquals(Integer.MAX_VALUE, r.value());
            assertTrue(r.overflow());
        }

        @Test
        void subOverflowMaxValueMinusNegative() {
            // MAX_VALUE - (-1) wraps to MIN_VALUE
            final var r = exec(ALU.Operation.SUB, Integer.MAX_VALUE, -1);
            assertEquals(Integer.MIN_VALUE, r.value());
            assertTrue(r.overflow());
        }

        @Test
        void subNoOverflowWhenSameSigns() {
            final var r = exec(ALU.Operation.SUB, 5, 5);
            assertEquals(0, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void subuSameBitPatternAsSub() {
            final var r = exec(ALU.Operation.SUBU, Integer.MIN_VALUE, 1);
            assertEquals(Integer.MAX_VALUE, r.value());
            assertFalse(r.overflow());
        }
    }

    @Nested
    @DisplayName("Bitwise operations")
    class Bitwise {

        @Test
        void andBasic() {
            final var r = exec(ALU.Operation.AND, 0xFF00FF00, 0x0F0F0F0F);
            assertEquals(0x0F000F00, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void andWithZero() {
            assertEquals(0, exec(ALU.Operation.AND, 0xDEADBEEF, 0).value());
        }

        @Test
        void andWithAllOnes() {
            assertEquals(0x12345678, exec(ALU.Operation.AND, 0x12345678, 0xFFFFFFFF).value());
        }

        @Test
        void orBasic() {
            final var r = exec(ALU.Operation.OR, 0xF0F0F0F0, 0x0F0F0F0F);
            assertEquals(0xFFFFFFFF, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void orWithZero() {
            assertEquals(0xABCD1234, exec(ALU.Operation.OR, 0xABCD1234, 0).value());
        }

        @Test
        void xorSameValueIsZero() {
            assertEquals(0, exec(ALU.Operation.XOR, 0xCAFEBABE, 0xCAFEBABE).value());
        }

        @Test
        void xorWithAllOnesIsComplement() {
            assertEquals(~0x12345678, exec(ALU.Operation.XOR, 0x12345678, 0xFFFFFFFF).value());
        }

        @Test
        void xorBasic() {
            final var r = exec(ALU.Operation.XOR, 0b1010, 0b1100);
            assertEquals(0b0110, r.value());
            assertFalse(r.overflow());
        }
    }

    @Nested
    @DisplayName("Shift operations")
    class Shifts {

        @Test
        void sllByOne() {
            assertEquals(2, exec(ALU.Operation.SLL, 1, 1).value());
        }

        @Test
        void sllBy16() {
            assertEquals(0x00010000, exec(ALU.Operation.SLL, 1, 16).value());
        }

        @Test
        void sllShiftAmountMaskedTo5Bits() {
            // shift by 33 is treated as shift by 1 (33 & 0x1F == 1)
            assertEquals(exec(ALU.Operation.SLL, 1, 1).value(),
                    exec(ALU.Operation.SLL, 1, 33).value());
        }

        @Test
        void sllByZeroIsIdentity() {
            assertEquals(0xABCDEF01, exec(ALU.Operation.SLL, 0xABCDEF01, 0).value());
        }

        @Test
        void srlByOne() {
            assertEquals(0x40000000, exec(ALU.Operation.SRL, 0x80000000, 1).value());
        }

        @Test
        void srlZeroFillsHighBits() {
            // Logical shift: high bit must be 0, not sign-extended
            final var r = exec(ALU.Operation.SRL, 0x80000000, 1);
            assertEquals(0x40000000, r.value());
        }

        @Test
        void srlShiftAmountMaskedTo5Bits() {
            assertEquals(exec(ALU.Operation.SRL, 0xFF, 2).value(),
                    exec(ALU.Operation.SRL, 0xFF, 34).value()); // 34 & 0x1F == 2
        }

        @Test
        void sraPreservesSignBit() {
            // Arithmetic shift: high bits filled with 1
            final var r = exec(ALU.Operation.SRA, 0x80000000, 1);
            assertEquals(0xC0000000, r.value());
        }

        @Test
        void sraPositiveValueFillsWithZero() {
            assertEquals(0x20000000, exec(ALU.Operation.SRA, 0x40000000, 1).value());
        }

        @Test
        void sraShiftAmountMaskedTo5Bits() {
            assertEquals(exec(ALU.Operation.SRA, -1, 4).value(),
                    exec(ALU.Operation.SRA, -1, 36).value()); // 36 & 0x1F == 4
        }
    }

    @Nested
    @DisplayName("Comparison operations")
    class Comparisons {

        @Test
        void seqEqualValues() {
            assertEquals(1, exec(ALU.Operation.SEQ, 42, 42).value());
        }

        @Test
        void seqUnequalValues() {
            assertEquals(0, exec(ALU.Operation.SEQ, 42, 43).value());
        }

        @Test
        void seqBothZero() {
            assertEquals(1, exec(ALU.Operation.SEQ, 0, 0).value());
        }

        @Test
        void sneUnequalValues() {
            assertEquals(1, exec(ALU.Operation.SNE, 1, 2).value());
        }

        @Test
        void sneEqualValues() {
            assertEquals(0, exec(ALU.Operation.SNE, 7, 7).value());
        }

        @Test
        void sltALessThanB() {
            assertEquals(1, exec(ALU.Operation.SLT, -1, 0).value());
        }

        @Test
        void sltAGreaterThanB() {
            assertEquals(0, exec(ALU.Operation.SLT, 5, 3).value());
        }

        @Test
        void sltEqual() {
            assertEquals(0, exec(ALU.Operation.SLT, 4, 4).value());
        }

        @Test
        void sltSignedComparisonMinVsMax() {
            assertEquals(1, exec(ALU.Operation.SLT, Integer.MIN_VALUE, Integer.MAX_VALUE).value());
        }

        @Test
        void sgtAGreaterThanB() {
            assertEquals(1, exec(ALU.Operation.SGT, 10, 5).value());
        }

        @Test
        void sgtALessThanB() {
            assertEquals(0, exec(ALU.Operation.SGT, -5, 0).value());
        }

        @Test
        void sgtEqual() {
            assertEquals(0, exec(ALU.Operation.SGT, 3, 3).value());
        }

        @Test
        void sleALessThanB() {
            assertEquals(1, exec(ALU.Operation.SLE, -1, 0).value());
        }

        @Test
        void sleEqual() {
            assertEquals(1, exec(ALU.Operation.SLE, 5, 5).value());
        }

        @Test
        void sleAGreaterThanB() {
            assertEquals(0, exec(ALU.Operation.SLE, 6, 5).value());
        }

        @Test
        void sgeAGreaterThanB() {
            assertEquals(1, exec(ALU.Operation.SGE, 1, 0).value());
        }

        @Test
        void sgeEqual() {
            assertEquals(1, exec(ALU.Operation.SGE, -3, -3).value());
        }

        @Test
        void sgeALessThanB() {
            assertEquals(0, exec(ALU.Operation.SGE, Integer.MIN_VALUE, 0).value());
        }

        @Test
        void passBReturnsBIgnoringA() {
            final var r = exec(ALU.Operation.PASS_B, 0xDEADBEEF, 0x12345678);
            assertEquals(0x12345678, r.value());
            assertFalse(r.overflow());
        }

        @Test
        void passBWithZeroB() {
            assertEquals(0, exec(ALU.Operation.PASS_B, 999, 0).value());
        }
    }
}
