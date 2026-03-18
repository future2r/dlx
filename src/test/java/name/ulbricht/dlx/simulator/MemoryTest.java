package name.ulbricht.dlx.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class MemoryTest {

    private Memory mem;

    @BeforeEach
    void setUp() {
        this.mem = new Memory(64);
    }

    @Test
    void sizeReturnsConstructorArgument() {
        assertEquals(64, this.mem.size());
        assertEquals(256, new Memory(256).size());
    }

    @Test
    void memoryIsZeroInitialised() {
        for (var addr = 0; addr < this.mem.size(); addr++)
            assertEquals(0, this.mem.loadByteU(addr));
    }

    @Test
    void storeAndLoadByte() {
        this.mem.storeByte(0, 0x7F);
        assertEquals(0x7F, this.mem.loadByte(0));
        assertEquals(0x7F, this.mem.loadByteU(0));
    }

    @Test
    void loadByteSignExtends() {
        this.mem.storeByte(0, 0xFF); // -1 as signed byte
        assertEquals(-1, this.mem.loadByte(0));
    }

    @Test
    void loadByteUZeroExtends() {
        this.mem.storeByte(0, 0xFF);
        assertEquals(255, this.mem.loadByteU(0));
    }

    @Test
    void storeByteKeepsOnlyLowerEightBits() {
        this.mem.storeByte(0, 0x1AB); // only 0xAB should be stored
        assertEquals(0xAB, this.mem.loadByteU(0));
    }

    @Test
    void storeAndLoadHalfWordBigEndian() {
        this.mem.storeHalfWord(4, 0x1234);
        assertEquals(0x12, this.mem.loadByteU(4));
        assertEquals(0x34, this.mem.loadByteU(5));
    }

    @Test
    void loadHalfWordSignExtends() {
        this.mem.storeHalfWord(0, 0x8000); // negative in 16-bit two's complement
        assertEquals((int) (short) 0x8000, this.mem.loadHalfWord(0)); // -32768
    }

    @Test
    void loadHalfWordUZeroExtends() {
        this.mem.storeHalfWord(0, 0x8000);
        assertEquals(0x8000, this.mem.loadHalfWordU(0)); // 32768
    }

    @Test
    void storeHalfWordKeepsOnlyLowerSixteenBits() {
        this.mem.storeHalfWord(0, 0xABCD);
        assertEquals(0xABCD, this.mem.loadHalfWordU(0));
    }

    @Test
    void storeAndLoadWordBigEndian() {
        this.mem.storeWord(0, 0x12345678);
        assertEquals(0x12, this.mem.loadByteU(0));
        assertEquals(0x34, this.mem.loadByteU(1));
        assertEquals(0x56, this.mem.loadByteU(2));
        assertEquals(0x78, this.mem.loadByteU(3));
    }

    @Test
    void storeAndLoadWordRoundTrip() {
        this.mem.storeWord(8, 0xDEADBEEF);
        assertEquals(0xDEADBEEF, this.mem.loadWord(8));
    }

    @Test
    void storeWordNegativeValue() {
        this.mem.storeWord(0, -1);
        assertEquals(-1, this.mem.loadWord(0));
    }

    @Test
    void storeWordMinAndMax() {
        this.mem.storeWord(0, Integer.MIN_VALUE);
        this.mem.storeWord(4, Integer.MAX_VALUE);
        assertEquals(Integer.MIN_VALUE, this.mem.loadWord(0));
        assertEquals(Integer.MAX_VALUE, this.mem.loadWord(4));
    }

    @Test
    void loadProgramWritesWordsSequentially() {
        int[] program = { 0x11111111, 0x22222222, 0x33333333 };
        this.mem.loadProgram(program, 0);
        assertEquals(0x11111111, this.mem.loadWord(0));
        assertEquals(0x22222222, this.mem.loadWord(4));
        assertEquals(0x33333333, this.mem.loadWord(8));
    }

    @Test
    void loadProgramAtNonZeroAddress() {
        int[] program = { 0xABCDEF01 };
        this.mem.loadProgram(program, 16);
        assertEquals(0xABCDEF01, this.mem.loadWord(16));
    }

    @Test
    void loadByteAtLastValidAddress() {
        this.mem.storeByte(63, 42);
        assertEquals(42, this.mem.loadByte(63));
    }

    @Test
    void loadByteOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.loadByte(64));
    }

    @Test
    void loadByteNegativeAddressThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.loadByte(-1));
    }

    @Test
    void loadHalfWordAtLastValidAddress() {
        this.mem.storeHalfWord(62, 0x1234);
        assertEquals(0x1234, this.mem.loadHalfWordU(62));
    }

    @Test
    void loadHalfWordOneByteBeforeEndThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.loadHalfWord(63));
    }

    @Test
    void loadWordAtLastValidAddress() {
        this.mem.storeWord(60, 0x0ABCDEF0);
        assertEquals(0x0ABCDEF0, this.mem.loadWord(60));
    }

    @Test
    void loadWordOneByteBeforeEndThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.loadWord(61));
    }

    @Test
    void storeByteOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.storeByte(64, 0));
    }

    @Test
    void storeHalfWordOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.storeHalfWord(63, 0));
    }

    @Test
    void storeWordOutOfBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> this.mem.storeWord(61, 0));
    }
}
