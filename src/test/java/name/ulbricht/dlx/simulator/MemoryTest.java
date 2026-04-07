package name.ulbricht.dlx.simulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Memory")
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

    @Nested
    @DisplayName("Byte access")
    class ByteAccess {

        @Test
        void storeAndLoadByte() {
            MemoryTest.this.mem.storeByte(0, 0x7F);
            assertEquals(0x7F, MemoryTest.this.mem.loadByte(0));
            assertEquals(0x7F, MemoryTest.this.mem.loadByteU(0));
        }

        @Test
        void loadByteSignExtends() {
            MemoryTest.this.mem.storeByte(0, 0xFF); // -1 as signed byte
            assertEquals(-1, MemoryTest.this.mem.loadByte(0));
        }

        @Test
        void loadByteUZeroExtends() {
            MemoryTest.this.mem.storeByte(0, 0xFF);
            assertEquals(255, MemoryTest.this.mem.loadByteU(0));
        }

        @Test
        void storeByteKeepsOnlyLowerEightBits() {
            MemoryTest.this.mem.storeByte(0, 0x1AB); // only 0xAB should be stored
            assertEquals(0xAB, MemoryTest.this.mem.loadByteU(0));
        }
    }

    @Nested
    @DisplayName("Half-word access")
    class HalfWordAccess {

        @Test
        void storeAndLoadHalfWordBigEndian() {
            MemoryTest.this.mem.storeHalfWord(4, 0x1234);
            assertEquals(0x12, MemoryTest.this.mem.loadByteU(4));
            assertEquals(0x34, MemoryTest.this.mem.loadByteU(5));
        }

        @Test
        void loadHalfWordSignExtends() {
            MemoryTest.this.mem.storeHalfWord(0, 0x8000); // negative in 16-bit two's complement
            assertEquals((short) 0x8000, MemoryTest.this.mem.loadHalfWord(0)); // -32768
        }

        @Test
        void loadHalfWordUZeroExtends() {
            MemoryTest.this.mem.storeHalfWord(0, 0x8000);
            assertEquals(0x8000, MemoryTest.this.mem.loadHalfWordU(0)); // 32768
        }

        @Test
        void storeHalfWordKeepsOnlyLowerSixteenBits() {
            MemoryTest.this.mem.storeHalfWord(0, 0xABCD);
            assertEquals(0xABCD, MemoryTest.this.mem.loadHalfWordU(0));
        }
    }

    @Nested
    @DisplayName("Word access")
    class WordAccess {

        @Test
        void storeAndLoadWordBigEndian() {
            MemoryTest.this.mem.storeWord(0, 0x12345678);
            assertEquals(0x12, MemoryTest.this.mem.loadByteU(0));
            assertEquals(0x34, MemoryTest.this.mem.loadByteU(1));
            assertEquals(0x56, MemoryTest.this.mem.loadByteU(2));
            assertEquals(0x78, MemoryTest.this.mem.loadByteU(3));
        }

        @Test
        void storeAndLoadWordRoundTrip() {
            MemoryTest.this.mem.storeWord(8, 0xDEADBEEF);
            assertEquals(0xDEADBEEF, MemoryTest.this.mem.loadWord(8));
        }

        @Test
        void storeWordNegativeValue() {
            MemoryTest.this.mem.storeWord(0, -1);
            assertEquals(-1, MemoryTest.this.mem.loadWord(0));
        }

        @Test
        void storeWordMinAndMax() {
            MemoryTest.this.mem.storeWord(0, Integer.MIN_VALUE);
            MemoryTest.this.mem.storeWord(4, Integer.MAX_VALUE);
            assertEquals(Integer.MIN_VALUE, MemoryTest.this.mem.loadWord(0));
            assertEquals(Integer.MAX_VALUE, MemoryTest.this.mem.loadWord(4));
        }
    }

    @Nested
    @DisplayName("Program loading")
    class ProgramLoading {

        @Test
        void loadProgramWritesWordsSequentially() {
            final byte[] program = {
                    0x11, 0x11, 0x11, 0x11,
                    0x22, 0x22, 0x22, 0x22,
                    0x33, 0x33, 0x33, 0x33
            };
            MemoryTest.this.mem.loadProgram(program, 0);
            assertEquals(0x11111111, MemoryTest.this.mem.loadWord(0));
            assertEquals(0x22222222, MemoryTest.this.mem.loadWord(4));
            assertEquals(0x33333333, MemoryTest.this.mem.loadWord(8));
        }

        @Test
        void loadProgramAtNonZeroAddress() {
            final byte[] program = { (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x01 };
            MemoryTest.this.mem.loadProgram(program, 16);
            assertEquals(0xABCDEF01, MemoryTest.this.mem.loadWord(16));
        }
    }

    @Nested
    @DisplayName("Boundary checks")
    class BoundaryChecks {

        @Test
        void loadByteAtLastValidAddress() {
            MemoryTest.this.mem.storeByte(63, 42);
            assertEquals(42, MemoryTest.this.mem.loadByte(63));
        }

        @Test
        void loadByteOutOfBoundsThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.loadByte(64));
        }

        @Test
        void loadByteNegativeAddressThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.loadByte(-1));
        }

        @Test
        void loadHalfWordAtLastValidAddress() {
            MemoryTest.this.mem.storeHalfWord(62, 0x1234);
            assertEquals(0x1234, MemoryTest.this.mem.loadHalfWordU(62));
        }

        @Test
        void loadHalfWordOneByteBeforeEndThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.loadHalfWord(63));
        }

        @Test
        void loadWordAtLastValidAddress() {
            MemoryTest.this.mem.storeWord(60, 0x0ABCDEF0);
            assertEquals(0x0ABCDEF0, MemoryTest.this.mem.loadWord(60));
        }

        @Test
        void loadWordOneByteBeforeEndThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.loadWord(61));
        }

        @Test
        void storeByteOutOfBoundsThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.storeByte(64, 0));
        }

        @Test
        void storeHalfWordOutOfBoundsThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.storeHalfWord(63, 0));
        }

        @Test
        void storeWordOutOfBoundsThrows() {
            assertThrows(IllegalArgumentException.class, () -> MemoryTest.this.mem.storeWord(61, 0));
        }
    }
}
