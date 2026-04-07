package name.ulbricht.dlx.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Registers")
final class RegistersTest {

    private Registers regs;

    @BeforeEach
    void setUp() {
        this.regs = new Registers();
    }

    @Test
    void allRegistersZeroOnCreation() {
        for (var i = 0; i < 32; i++)
            assertEquals(0, this.regs.read(i));
    }

    @Test
    void writeToR0IsIgnored() {
        this.regs.write(0, 42);
        assertEquals(0, this.regs.read(0));
    }

    @Test
    void writeAndReadAllNonZeroRegisters() {
        for (var i = 1; i < 32; i++)
            this.regs.write(i, i * 7);
        for (var i = 1; i < 32; i++)
            assertEquals(i * 7, this.regs.read(i));
    }

    @Test
    void overwriteRegister() {
        this.regs.write(5, 111);
        this.regs.write(5, 222);
        assertEquals(222, this.regs.read(5));
    }

    @Test
    void writeNegativeValue() {
        this.regs.write(3, -1);
        assertEquals(-1, this.regs.read(3));
    }

    @Test
    void writeMinAndMax() {
        this.regs.write(1, Integer.MIN_VALUE);
        this.regs.write(2, Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, this.regs.read(1));
        assertEquals(Integer.MAX_VALUE, this.regs.read(2));
    }

    @Test
    void readIndexMaskedToFiveBits() {
        // index 32 maps to R0
        assertEquals(0, this.regs.read(32));

        // index 33 maps to R1
        this.regs.write(1, 77);
        assertEquals(77, this.regs.read(33));
    }

    @Test
    void writeIndexMaskedToFiveBits() {
        // index 33 maps to R1
        this.regs.write(33, 55);
        assertEquals(55, this.regs.read(1));
    }

    @Test
    void writeToMaskedR0IsIgnored() {
        // index 32 maps to R0, write must be ignored
        this.regs.write(32, 999);
        assertEquals(0, this.regs.read(0));
    }

    @Test
    void snapshotReflectsCurrentState() {
        this.regs.write(7, 42);
        this.regs.write(15, -7);

        final var snapshot = this.regs.snapshot();
        assertEquals(42, snapshot[7]);
        assertEquals(-7, snapshot[15]);
        assertEquals(0, snapshot[0]);
    }

    @Test
    void snapshotIsCopy() {
        this.regs.write(4, 10);
        final var snapshot = this.regs.snapshot();

        snapshot[4] = 9999; // mutate the copy
        assertEquals(10, this.regs.read(4)); // original must be unchanged
    }
}
